package com.library.repository;

import com.library.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for Transaction documents.
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    /** Fetch all transactions for a given member (lending history) */
    List<Transaction> findByMemberId(String memberId);

    /** Fetch only overdue transactions for dashboard/alerts */
    List<Transaction> findByStatus(String status);

    /** Detect if a member already has an active (ISSUED) loan for the same book */
    boolean existsByBookIdAndMemberIdAndStatus(String bookId, String memberId, String status);
}
