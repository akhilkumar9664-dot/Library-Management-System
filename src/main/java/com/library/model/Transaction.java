package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document capturing a single book-lending transaction.
 *
 * Status lifecycle:
 *   ISSUED  →  RETURNED  (on time)
 *   ISSUED  →  OVERDUE   (returned past dueDate)
 *
 * We denormalise bookTitle and memberName to avoid joins on every read.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Field("bookId")
    private String bookId;

    /** Denormalised for fast display without a book lookup */
    @Field("bookTitle")
    private String bookTitle;

    @Field("memberId")
    private String memberId;

    /** Denormalised for fast display without a member lookup */
    @Field("memberName")
    private String memberName;

    @Field("issueDate")
    private LocalDateTime issueDate;

    /** issueDate + 14 days; set at issue time */
    @Field("dueDate")
    private LocalDateTime dueDate;

    /** Null until the book is returned */
    @Field("returnDate")
    private LocalDateTime returnDate;

    /** ISSUED | RETURNED | OVERDUE */
    @Field("status")
    private String status;

    /**
     * Fine accrued for late returns.
     * ₹5 per overdue day; 0.0 if returned on time or not yet returned.
     */
    @Field("fineAmount")
    private double fineAmount;
}
