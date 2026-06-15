package com.library.service;

import com.library.dto.request.MemberRequest;
import com.library.dto.response.MemberResponse;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Member;
import com.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Business logic for library member management.
 *
 * Key responsibility: auto-generate a unique membershipId
 * in the format "LIB-XXXXXX" (6 random digits).
 */
@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    private final Random random = new Random();

    // ── Create ─────────────────────────────────────────────────────────────

    public MemberResponse registerMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Member with email " + request.getEmail() + " already exists");
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .membershipId(generateUniqueMembershipId())
                .membershipType(request.getMembershipType().toUpperCase())
                .isActive(true)
                .joinedAt(LocalDateTime.now())
                .build();

        return toResponse(memberRepository.save(member));
    }

    // ── Read ───────────────────────────────────────────────────────────────

    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(this::toResponse);
    }

    public MemberResponse getMemberById(String id) {
        return toResponse(findMemberOrThrow(id));
    }

    // ── Update ─────────────────────────────────────────────────────────────

    public MemberResponse updateMember(String id, MemberRequest request) {
        Member member = findMemberOrThrow(id);

        // If email changed, check for collision with another member
        if (!member.getEmail().equals(request.getEmail())
                && memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already in use: " + request.getEmail());
        }

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setMembershipType(request.getMembershipType().toUpperCase());

        return toResponse(memberRepository.save(member));
    }

    // ── Soft Delete ────────────────────────────────────────────────────────

    /**
     * Deactivate a member instead of hard-deleting.
     * This preserves the full transaction history for audit purposes.
     */
    public MemberResponse deactivateMember(String id) {
        Member member = findMemberOrThrow(id);
        member.setActive(false);
        return toResponse(memberRepository.save(member));
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    public Member findMemberOrThrow(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
    }

    /**
     * Generate a membership ID like "LIB-482910".
     * Retries if (rarely) there is a collision in MongoDB.
     */
    private String generateUniqueMembershipId() {
        String id;
        do {
            // nextInt(900000) gives 0–899999; +100000 ensures exactly 6 digits
            int digits = 100000 + random.nextInt(900000);
            id = "LIB-" + digits;
        } while (memberRepository.existsByMembershipId(id));
        return id;
    }

    private MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .membershipId(member.getMembershipId())
                .membershipType(member.getMembershipType())
                .isActive(member.isActive())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
