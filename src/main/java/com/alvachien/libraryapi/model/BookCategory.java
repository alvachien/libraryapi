package com.alvachien.libraryapi.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "book_ctgy_def")
public class BookCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name="category_value", nullable = false)
    private BookCategoryEnum categoryValue;

    @Column(name="category_name", nullable = true, length = 50)
    private String categoryName;

    @ManyToMany(fetch=FetchType.LAZY, mappedBy="categories")
    private List<Book> books;

    public BookCategory() {
        this.books = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookCategoryEnum getCategoryValue() {
        return categoryValue;
    }

    public void setCategoryValue(BookCategoryEnum categoryValue) {
        this.categoryValue = categoryValue;
    }

    public List<Book> getBooks() {
        return books;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
