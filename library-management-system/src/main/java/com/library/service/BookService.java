package com.library.service;

import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Business logic for book catalogue management.
 * Mapping between Book entity and BookResponse DTO is done here
 * to keep controllers thin.
 */
@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // ── Create ─────────────────────────────────────────────────────────────

    public BookResponse addBook(BookRequest request) {
        // Prevent duplicate ISBN across the catalogue
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .genre(request.getGenre())
                .totalCopies(request.getTotalCopies())
                // Initially all copies are available
                .availableCopies(request.getTotalCopies())
                .publishedYear(request.getPublishedYear())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(bookRepository.save(book));
    }

    // ── Read ───────────────────────────────────────────────────────────────

    /** Paginated list of all books */
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    public BookResponse getBookById(String id) {
        return toResponse(findBookOrThrow(id));
    }

    /**
     * Case-insensitive search across title, author, and genre.
     * Any parameter can be an empty string to act as a wildcard.
     */
    public Page<BookResponse> searchBooks(String title, String author,
                                          String genre, Pageable pageable) {
        // Replace null with empty string so the regex ".*" matches anything
        String t = (title  != null) ? title  : "";
        String a = (author != null) ? author : "";
        String g = (genre  != null) ? genre  : "";

        return bookRepository.searchBooks(t, a, g, pageable).map(this::toResponse);
    }

    // ── Update ─────────────────────────────────────────────────────────────

    public BookResponse updateBook(String id, BookRequest request) {
        Book book = findBookOrThrow(id);

        // If ISBN changed, check for collision
        if (!book.getIsbn().equals(request.getIsbn())
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Another book already has ISBN: " + request.getIsbn());
        }

        // Calculate the new available copies relative to the copy-count change
        int diff = request.getTotalCopies() - book.getTotalCopies();
        int newAvailable = book.getAvailableCopies() + diff;
        // Available copies can never go negative
        if (newAvailable < 0) newAvailable = 0;

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setGenre(request.getGenre());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(newAvailable);
        book.setPublishedYear(request.getPublishedYear());

        return toResponse(bookRepository.save(book));
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    public void deleteBook(String id) {
        findBookOrThrow(id);  // throws 404 if not found
        bookRepository.deleteById(id);
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    /** Reusable fetch-or-throw pattern */
    public Book findBookOrThrow(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }

    /** Map domain model → response DTO */
    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .genre(book.getGenre())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .publishedYear(book.getPublishedYear())
                .createdAt(book.getCreatedAt())
                .build();
    }
}
