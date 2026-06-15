package com.library.controller;

import com.library.dto.request.BookRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.BookResponse;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for book catalogue operations.
 * All endpoints require a valid JWT (enforced by SecurityConfig + JwtAuthFilter).
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * POST /api/books
     * Add a new book to the catalogue.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> addBook(
            @Valid @RequestBody BookRequest request) {

        BookResponse book = bookService.addBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book added successfully", book));
    }

    /**
     * GET /api/books?page=0&size=10&sort=title
     * Paginated list of all books.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<BookResponse> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    /**
     * GET /api/books/{id}
     * Get a single book by its MongoDB ObjectId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable String id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    /**
     * GET /api/books/search?title=&author=&genre=&page=0&size=10
     * Case-insensitive search across title, author, and genre.
     * Any parameter can be omitted (defaults to empty → wildcard).
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "") String author,
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> books = bookService.searchBooks(title, author, genre, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results", books));
    }

    /**
     * PUT /api/books/{id}
     * Update book details (title, author, ISBN, copies, etc.).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable String id,
            @Valid @RequestBody BookRequest request) {

        BookResponse book = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", book));
    }

    /**
     * DELETE /api/books/{id}
     * Hard-delete a book from the catalogue.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
    }
}
