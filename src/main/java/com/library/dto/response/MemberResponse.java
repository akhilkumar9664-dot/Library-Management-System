package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for member-related endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String membershipId;
    private String membershipType;
    private boolean isActive;
    private LocalDateTime joinedAt;
}
