package com.library.service;

import com.library.dto.request.IssueBookRequest;
import com.library.dto.response.TransactionResponse;
import com.library.exception.BookNotAvailableException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.repository.BookRepository;
import com.library.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core lending business logic:
 *   - Issue a book (validates availability, decrements stock)
 *   - Return a book (increments stock, calculates fine)
 *   - Query transactions (all / by member / overdue)
 *
 * Fine rule: ₹5 per overdue day (returnDate − dueDate, if positive).
 */
@Service
public class TransactionService {

    /** Fine rate per overdue day in Rupees */
    private static final double FINE_PER_DAY = 5.0;

    /** Loan period in days */
    private static final int LOAN_DAYS = 14;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberService memberService;   // reuse findMemberOrThrow

    // ── Issue ──────────────────────────────────────────────────────────────

    /**
     * Issue a book to a member.
     *
     * Business rules:
     *   1. Book must exist.
     *   2. Member must exist and be active.
     *   3. availableCopies must be > 0.
     *   4. Decrement availableCopies by 1.
     *   5. Create a transaction with status ISSUED and dueDate = now + 14 days.
     */
    public TransactionResponse issueBook(IssueBookRequest request) {
        // Validate book exists
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book", "id", request.getBookId()));

        // Validate member exists
        Member member = memberService.findMemberOrThrow(request.getMemberId());

        // Guard: copies available
        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException(
                    "No available copies for book: " + book.getTitle());
        }

        // Guard: member is active
        if (!member.isActive()) {
            throw new IllegalStateException(
                    "Member " + member.getName() + " is deactivated and cannot borrow books");
        }

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .bookId(book.getId())
                .bookTitle(book.getTitle())          // denormalised
                .memberId(member.getId())
                .memberName(member.getName())         // denormalised
                .issueDate(now)
                .dueDate(now.plusDays(LOAN_DAYS))    // due in 14 days
                .returnDate(null)                    // not yet returned
                .status("ISSUED")
                .fineAmount(0.0)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    // ── Return ─────────────────────────────────────────────────────────────

    /**
     * Process a book return.
     *
     * Business rules:
     *   1. Transaction must exist and be in ISSUED status.
     *   2. Increment availableCopies by 1.
     *   3. If returnDate > dueDate → OVERDUE, fine = 5 × overdue days.
     *   4. Else → RETURNED, fine = 0.
     */
    public TransactionResponse returnBook(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "id", transactionId));

        // Prevent double-return
        if (!"ISSUED".equals(transaction.getStatus())) {
            throw new IllegalStateException(
                    "Transaction is already in status: " + transaction.getStatus());
        }

        LocalDateTime returnDate = LocalDateTime.now();
        transaction.setReturnDate(returnDate);

        double fine = 0.0;
        String status;

        if (returnDate.isAfter(transaction.getDueDate())) {
            // Overdue: count the number of late days (rounded up via DAYS unit)
            long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), returnDate);
            if (overdueDays == 0) overdueDays = 1; // same-day late still counts as 1
            fine = overdueDays * FINE_PER_DAY;
            status = "OVERDUE";
        } else {
            status = "RETURNED";
        }

        transaction.setStatus(status);
        transaction.setFineAmount(fine);

        // Increment available copies on the book
        Book book = bookRepository.findById(transaction.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book", "id", transaction.getBookId()));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return toResponse(transactionRepository.save(transaction));
    }

    // ── Read ───────────────────────────────────────────────────────────────

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByMember(String memberId) {
        // Verify member exists before querying
        memberService.findMemberOrThrow(memberId);
        return transactionRepository.findByMemberId(memberId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Return all transactions currently in OVERDUE status.
     * Note: ISSUED transactions that have passed their dueDate are not automatically
     * flipped to OVERDUE here — that flip happens only on return.
     * For a full overdue view, consider a scheduled job (future enhancement).
     */
    public List<TransactionResponse> getOverdueTransactions() {
        return transactionRepository.findByStatus("OVERDUE")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .bookId(t.getBookId())
                .bookTitle(t.getBookTitle())
                .memberId(t.getMemberId())
                .memberName(t.getMemberName())
                .issueDate(t.getIssueDate())
                .dueDate(t.getDueDate())
                .returnDate(t.getReturnDate())
                .status(t.getStatus())
                .fineAmount(t.getFineAmount())
                .build();
    }
}
