package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document representing an application user (ADMIN or LIBRARIAN).
 * Passwords are stored BCrypt-hashed — never plaintext.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    /** Unique login handle */
    @Indexed(unique = true)
    @Field("username")
    private String username;

    @Indexed(unique = true)
    @Field("email")
    private String email;

    /** BCrypt-hashed password */
    @Field("password")
    private String password;

    /** Role: ADMIN or LIBRARIAN */
    @Field("role")
    private String role;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
