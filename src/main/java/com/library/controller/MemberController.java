package com.library.controller;

import com.library.dto.request.MemberRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.MemberResponse;
import com.library.service.MemberService;
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
 * REST controller for library member management.
 * All endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * POST /api/members
     * Register a new library member. membershipId is auto-generated.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> registerMember(
            @Valid @RequestBody MemberRequest request) {

        MemberResponse member = memberService.registerMember(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member registered successfully", member));
    }

    /**
     * GET /api/members?page=0&size=10
     * Paginated list of all members (active and inactive).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<MemberResponse> members = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    /**
     * GET /api/members/{id}
     * Get a single member by MongoDB ObjectId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(
            @PathVariable String id) {

        MemberResponse member = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success("Member retrieved successfully", member));
    }

    /**
     * PUT /api/members/{id}
     * Update member profile (name, email, phone, membershipType).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable String id,
            @Valid @RequestBody MemberRequest request) {

        MemberResponse member = memberService.updateMember(id, request);
        return ResponseEntity.ok(ApiResponse.success("Member updated successfully", member));
    }

    /**
     * DELETE /api/members/{id}
     * Soft-delete: sets isActive = false. Transaction history is preserved.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> deactivateMember(
            @PathVariable String id) {

        MemberResponse member = memberService.deactivateMember(id);
        return ResponseEntity.ok(ApiResponse.success("Member deactivated successfully", member));
    }
}
