package com.library.repository;

import com.library.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for Member documents.
 */
@Repository
public interface MemberRepository extends MongoRepository<Member, String> {

    boolean existsByEmail(String email);

    boolean existsByMembershipId(String membershipId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMembershipId(String membershipId);
}
