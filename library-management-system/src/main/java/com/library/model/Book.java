package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document representing a book in the library catalogue.
 * Tracks total copies and currently available copies separately
 * so that we can accurately manage concurrent lending.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("author")
    private String author;

    /** ISBN must be globally unique across the catalogue */
    @Indexed(unique = true)
    @Field("isbn")
    private String isbn;

    @Field("genre")
    private String genre;

    /** Physical/total copies owned by the library */
    @Field("totalCopies")
    private int totalCopies;

    /** Copies currently not lent out (decrements on issue, increments on return) */
    @Field("availableCopies")
    private int availableCopies;

    @Field("publishedYear")
    private int publishedYear;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
