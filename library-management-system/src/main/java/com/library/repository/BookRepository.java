package com.library.repository;

import com.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for Book documents.
 * Custom @Query methods use MongoDB regex for case-insensitive search.
 */
@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    /** Check duplicate ISBN before inserting */
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    /**
     * Case-insensitive search across title, author, and genre.
     * Each parameter is optional (empty string matches everything via .*).
     *
     * MongoDB regex syntax: { field: { $regex: value, $options: 'i' } }
     */
    @Query("{ 'title': { $regex: ?0, $options: 'i' }, " +
           "'author': { $regex: ?1, $options: 'i' }, " +
           "'genre': { $regex: ?2, $options: 'i' } }")
    Page<Book> searchBooks(String title, String author, String genre, Pageable pageable);
}
