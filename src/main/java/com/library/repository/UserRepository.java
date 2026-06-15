package com.library.repository;

import com.library.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for User documents.
 * Spring Data generates the implementation at runtime.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /** Used by UserDetailsServiceImpl to load a user by username for authentication */
    Optional<User> findByUsername(String username);

    /** Used during registration to check for duplicate usernames */
    boolean existsByUsername(String username);

    /** Used during registration to check for duplicate emails */
    boolean existsByEmail(String email);
}
