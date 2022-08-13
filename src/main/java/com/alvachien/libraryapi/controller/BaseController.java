package com.alvachien.libraryapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alvachien.libraryapi.repository.SelfRegisteredRuntimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.Set;

public abstract class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String TENANTS_MAP_KEY = "tenant";
    private final String SCOPES_KEY = "scopes";
    private final String CONSUMER_TENANT_KEY = "consumerTenant";
    private final String PROVIDER_TENANT_KEY = "providerTenant";
    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String JWT_TOKEN_SPLIT_PARTS = "\\.";
    private final String INTERNAL_VISIBILITY_SCOPE = "internal_visibility:read";
    private final String TOKEN_CLIENT_ID_KEY = "tokenClientID";
    private final String TOKEN_REGION_KEY = "region";
    private final int PAYLOAD_INDEX = 1;

    @Autowired
    private SelfRegisteredRuntimeRepository repo;

    @Value("${subscription.provider_label_key:subscriptionProviderId}")
    private String selfRegKey;

    @Value("${subscription.region_key:region}")
    private String regionKey;

    @Value("${subscription.token_prefix:prefix-}")
    private String tokenPrefix;

    String extractTenantFromIDToken(final HttpServletRequest request) throws IOException {
        final String idToken = request.getHeader(AUTHORIZATION_HEADER);
        if (idToken == null || idToken.isEmpty()) {
            return "";
        }

        String idTokenDecoded = decodeIDToken(idToken);
        JsonNode tokenTree = mapper.readTree(idTokenDecoded);
        String unescapedTenants = tokenTree.get(TENANTS_MAP_KEY).asText().replace("\\", "");

        String providerTenantID = "";
        try {
            JsonNode tenantsTree = mapper.readTree(unescapedTenants);

            String tenant = tenantsTree.path(CONSUMER_TENANT_KEY).asText();
            providerTenantID = tenantsTree.path(PROVIDER_TENANT_KEY).asText();
            if (providerTenantID == null || providerTenantID.isEmpty() || providerTenantID.equals(tenant)) {
                return tenant;
            }

            logger.info("Both consumer {} and provider {} tenants are present. Checking if there is a subscription...", tenant, providerTenantID);
            String tokenClientId = tokenTree.get(TOKEN_CLIENT_ID_KEY).asText();
            if (tokenClientId == null || tokenClientId.isEmpty()) {
                logger.error("could not find consumer token client ID");
                return "";
            }
            if (tokenClientId.startsWith(tokenPrefix)){
                tokenClientId = tokenClientId.substring(tokenPrefix.length());
            }

            String tokenRegion = tokenTree.get(TOKEN_REGION_KEY).asText();
            if (tokenRegion == null || tokenRegion.isEmpty()) {
                logger.error("could not determine token's region");
                return "";
            }

            Set<String> runtimeIds = repo.findSelfRegisteredRuntimesByLabels(providerTenantID, selfRegKey, tokenClientId, regionKey, tokenRegion);
            for (String runtimeId : runtimeIds) {
                if (repo.isRuntimeSubscriptionAvailableInTenant(tenant, runtimeId) > 0) {
                    return tenant;
                }
            }
        } catch (IOException ignored) {}

        return providerTenantID;
    }

    boolean isInternalVisibilityScopePresent(final HttpServletRequest request) throws JsonProcessingException {
        final String idToken = request.getHeader(AUTHORIZATION_HEADER);

        if (idToken != null && !idToken.isEmpty()) {
            String idTokenDecoded = decodeIDToken(idToken);

            JsonNode tokenTree = mapper.readTree(idTokenDecoded);
            String scopes = tokenTree.get(SCOPES_KEY).asText();

            return scopes.contains(INTERNAL_VISIBILITY_SCOPE);
        }
        return false;
    }

    private String decodeIDToken(String idTokenEncoded) {
        // The id_token comes with `Bearer` prefix which we should trim
        String idTokenStripped = idTokenEncoded.substring(idTokenEncoded.indexOf(" ") + 1);

        String[] splitIDToken = idTokenStripped.split(JWT_TOKEN_SPLIT_PARTS);
        String base64EncodedPayload = splitIDToken[PAYLOAD_INDEX];

        byte[] idTokenBytes = Base64.getDecoder().decode(base64EncodedPayload);

        return new String(idTokenBytes);
    }
}
