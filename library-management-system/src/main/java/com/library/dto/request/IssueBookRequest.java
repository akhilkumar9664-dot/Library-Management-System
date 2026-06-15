package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for POST /api/transactions/issue
 */
@Data
public class IssueBookRequest {

    @NotBlank(message = "Book ID is required")
    private String bookId;

    @NotBlank(message = "Member ID is required")
    private String memberId;
}
