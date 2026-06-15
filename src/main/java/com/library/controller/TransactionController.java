package com.library.controller;

import com.library.dto.request.IssueBookRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.TransactionResponse;
import com.library.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book lending transactions.
 * All endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * POST /api/transactions/issue
     * Issue a book to a member.
     * Returns 201 Created with the new transaction.
     */
    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<TransactionResponse>> issueBook(
            @Valid @RequestBody IssueBookRequest request) {

        TransactionResponse transaction = transactionService.issueBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book issued successfully", transaction));
    }

    /**
     * POST /api/transactions/return/{transactionId}
     * Return a previously issued book.
     * Calculates fine if overdue (₹5/day).
     */
    @PostMapping("/return/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> returnBook(
            @PathVariable String transactionId) {

        TransactionResponse transaction = transactionService.returnBook(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Book returned successfully", transaction));
    }

    /**
     * GET /api/transactions
     * Get all transactions (full history).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Transactions retrieved successfully", transactions));
    }

    /**
     * GET /api/transactions/member/{memberId}
     * Get the lending history for a specific member.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByMember(
            @PathVariable String memberId) {

        List<TransactionResponse> transactions =
                transactionService.getTransactionsByMember(memberId);
        return ResponseEntity.ok(
                ApiResponse.success("Member transactions retrieved", transactions));
    }

    /**
     * GET /api/transactions/overdue
     * Get all transactions with status OVERDUE.
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getOverdueTransactions() {
        List<TransactionResponse> transactions = transactionService.getOverdueTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Overdue transactions retrieved", transactions));
    }
}
