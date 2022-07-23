package com.alvachien.library.util;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public final class Constants {
	private Constants() {
        throw new IllegalStateException("Utility class");
    }
	
	// Service Namespace
	public static final String NAMESPACE = "OData.Demo";

	// EDM Container
	public static final String CONTAINER_NAME = "Container";
	public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

	// Entity Types Names
	public static final String ET_AUTHOR_NAME = "Author";
	public static final FullQualifiedName ET_AUTHOR_FQN = new FullQualifiedName(NAMESPACE, ET_AUTHOR_NAME);

	// Entity Set Names
	public static final String ES_AUTHORS_NAME = "Authors";
}
