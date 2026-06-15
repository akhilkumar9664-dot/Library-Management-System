<div align="center">

# 📚 Library Management System

**A production-quality RESTful backend built with Java Spring Boot & MongoDB**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

*A complete academic project demonstrating Spring Boot REST API design patterns with JWT security, MongoDB persistence, and clean layered architecture.*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [API Endpoints](#-api-endpoints)
- [Data Models](#-data-models)
- [Business Rules](#-business-rules)
- [Getting Started](#-getting-started)
- [Postman Testing Guide](#-postman-testing-guide)
- [Security Design](#-security-design)

---

## 🔍 Overview

The **Library Management System** is a fully functional backend API that handles the complete lifecycle of a library — from user authentication and book cataloguing to member management and book lending with automated fine calculation.

Built as an academic project, it demonstrates real-world software engineering practices including:
- Layered architecture (Controller → Service → Repository)
- JWT-based stateless authentication
- Clean DTO pattern with Jakarta validation
- Centralised exception handling
- MongoDB document modelling with proper indexing

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔐 **JWT Authentication** | Secure login/register with BCrypt-hashed passwords and 24-hour JWT tokens |
| 📖 **Book Catalogue** | Add, update, delete, and search books with case-insensitive regex queries |
| 👥 **Member Management** | Register members with auto-generated membership IDs (`LIB-XXXXXX`) |
| 🔄 **Book Transactions** | Issue and return books with automated availability tracking |
| 💰 **Fine Calculation** | Auto-calculates ₹5/day fine for overdue returns |
| 📄 **Pagination** | All list endpoints support page/size/sort query parameters |
| ⚠️ **Global Error Handling** | Consistent `ApiResponse<T>` wrapper across all endpoints |
| 🛡️ **Soft Delete** | Members are deactivated (not deleted) to preserve transaction history |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Database | MongoDB 7.0 |
| ORM | Spring Data MongoDB |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Validation | Jakarta Bean Validation |
| Build Tool | Apache Maven |
| Boilerplate | Lombok |

---

## 📁 Project Structure

```
library-management-system/
├── src/main/java/com/library/
│   ├── LibraryManagementSystemApplication.java   # App entry point
│   │
│   ├── config/
│   │   ├── SecurityConfig.java                   # Spring Security + JWT filter chain
│   │   └── MongoConfig.java                      # MongoDB + auto-index configuration
│   │
│   ├── controller/
│   │   ├── AuthController.java                   # POST /api/auth/register, /login
│   │   ├── BookController.java                   # CRUD + search for books
│   │   ├── MemberController.java                 # CRUD + deactivate members
│   │   └── TransactionController.java            # Issue, return, overdue queries
│   │
│   ├── model/
│   │   ├── User.java                             # @Document: users collection
│   │   ├── Book.java                             # @Document: books collection
│   │   ├── Member.java                           # @Document: members collection
│   │   └── Transaction.java                      # @Document: transactions collection
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── BookRepository.java                   # Custom regex search query
│   │   ├── MemberRepository.java
│   │   └── TransactionRepository.java
│   │
│   ├── service/
│   │   ├── AuthService.java                      # Registration + JWT login
│   │   ├── BookService.java                      # Book business logic
│   │   ├── MemberService.java                    # Membership ID generation
│   │   └── TransactionService.java               # Issue/return + fine calculation
│   │
│   ├── dto/
│   │   ├── request/                              # Validated inbound payloads
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── BookRequest.java
│   │   │   ├── MemberRequest.java
│   │   │   └── IssueBookRequest.java
│   │   └── response/                             # Outbound API shapes
│   │       ├── ApiResponse.java                  # Generic wrapper {success, message, data}
│   │       ├── AuthResponse.java
│   │       ├── BookResponse.java
│   │       ├── MemberResponse.java
│   │       └── TransactionResponse.java
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java           # @RestControllerAdvice
│   │   ├── ResourceNotFoundException.java        # 404
│   │   ├── BookNotAvailableException.java        # 400
│   │   └── DuplicateResourceException.java       # 409
│   │
│   └── security/
│       ├── JwtUtil.java                          # Token generation & validation
│       ├── JwtAuthFilter.java                    # OncePerRequestFilter
│       └── UserDetailsServiceImpl.java           # MongoDB-backed UserDetails
│
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## 🏗 Architecture

```
Client (Postman / Frontend)
        │
        ▼
┌───────────────────┐
│   JWT Auth Filter  │  ← Validates Bearer token on every protected request
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│    Controllers     │  ← @RestController — receives HTTP, delegates to service
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│     Services       │  ← Business logic, DTO mapping, validations
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│   Repositories     │  ← MongoRepository — Spring Data auto-generates queries
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│     MongoDB        │  ← Collections: users, books, members, transactions
└───────────────────┘
```

---

## 🌐 API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register ADMIN or LIBRARIAN |
| `POST` | `/api/auth/login` | Login and receive JWT token |

### Books 🔒 *(Requires JWT)*

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/books` | Add a new book |
| `GET` | `/api/books?page=0&size=10` | Get all books (paginated) |
| `GET` | `/api/books/{id}` | Get book by ID |
| `GET` | `/api/books/search?title=&author=&genre=` | Case-insensitive search |
| `PUT` | `/api/books/{id}` | Update book details |
| `DELETE` | `/api/books/{id}` | Delete a book |

### Members 🔒 *(Requires JWT)*

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/members` | Register a new member |
| `GET` | `/api/members?page=0&size=10` | Get all members (paginated) |
| `GET` | `/api/members/{id}` | Get member by ID |
| `PUT` | `/api/members/{id}` | Update member details |
| `DELETE` | `/api/members/{id}` | Deactivate member (soft delete) |

### Transactions 🔒 *(Requires JWT)*

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions/issue` | Issue a book to a member |
| `POST` | `/api/transactions/return/{transactionId}` | Return a book |
| `GET` | `/api/transactions` | Get all transactions |
| `GET` | `/api/transactions/member/{memberId}` | Get member's lending history |
| `GET` | `/api/transactions/overdue` | Get all overdue transactions |

### Standard Response Format

Every endpoint returns the same wrapper:

```json
{
  "success": true,
  "message": "Book added successfully",
  "data": { ... }
}
```

---

## 🗄 Data Models

### User
```json
{
  "_id": "ObjectId",
  "username": "admin",
  "email": "admin@library.com",
  "password": "$2a$10$... (BCrypt hashed)",
  "role": "ADMIN | LIBRARIAN",
  "createdAt": "2024-01-15T10:00:00"
}
```

### Book
```json
{
  "_id": "ObjectId",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "genre": "Software Engineering",
  "totalCopies": 5,
  "availableCopies": 3,
  "publishedYear": 2008,
  "createdAt": "2024-01-15T10:00:00"
}
```

### Member
```json
{
  "_id": "ObjectId",
  "name": "Akhil Kumar",
  "email": "akhil@example.com",
  "phone": "9876543210",
  "membershipId": "LIB-482910",
  "membershipType": "BASIC | PREMIUM",
  "isActive": true,
  "joinedAt": "2024-01-15T10:00:00"
}
```

### Transaction
```json
{
  "_id": "ObjectId",
  "bookId": "...",
  "bookTitle": "Clean Code",
  "memberId": "...",
  "memberName": "Akhil Kumar",
  "issueDate": "2024-01-15T10:00:00",
  "dueDate": "2024-01-29T10:00:00",
  "returnDate": null,
  "status": "ISSUED | RETURNED | OVERDUE",
  "fineAmount": 0.0
}
```

---

## ⚙️ Business Rules

1. **Issuing a Book**
   - `availableCopies` must be > 0, else `BookNotAvailableException` (400)
   - Decrements `availableCopies` by 1
   - Sets `dueDate = issueDate + 14 days`
   - Sets `status = ISSUED`

2. **Returning a Book**
   - Increments `availableCopies` by 1
   - If `returnDate > dueDate` → `status = OVERDUE`, `fineAmount = overdueDays × ₹5`
   - Else → `status = RETURNED`, `fineAmount = 0`

3. **Membership ID Generation**
   - Format: `LIB-` + 6 random digits (e.g. `LIB-482910`)
   - Guaranteed unique — retries on collision

4. **Book Search**
   - Case-insensitive MongoDB regex across `title`, `author`, and `genre`
   - Any parameter can be omitted to act as a wildcard

5. **Member Deactivation**
   - Soft delete: sets `isActive = false`
   - Transaction history is fully preserved

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Apache Maven | 3.6+ |
| MongoDB | 7.0 (local) or Atlas (cloud) |

### 1. Clone the Repository

```bash
git clone https://github.com/akhilkumar9664-dot/Library-Management-System.git
cd Library-Management-System
```

### 2. Configure MongoDB

**Option A — Local MongoDB** (if MongoDB is installed and running):
```properties
# src/main/resources/application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/library_db
spring.data.mongodb.database=library_db
```

**Option B — MongoDB Atlas** (cloud):
```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/library_db?retryWrites=true&w=majority
spring.data.mongodb.database=library_db
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The server starts at: **`http://localhost:8080`**

You should see:
```
Started LibraryManagementSystemApplication in X.XXX seconds
```

### 4. Verify Startup

```bash
curl http://localhost:8080/api/auth/register -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"test123","role":"ADMIN"}'
```

---

## 🧪 Postman Testing Guide

### Step 1 — Register

**POST** `http://localhost:8080/api/auth/register`
```json
{
  "username": "admin",
  "email": "admin@library.com",
  "password": "admin123",
  "role": "ADMIN"
}
```

### Step 2 — Login (copy the token!)

**POST** `http://localhost:8080/api/auth/login`
```json
{
  "username": "admin",
  "password": "admin123"
}
```
> Save the `data.token` value. All subsequent requests need: `Authorization: Bearer <token>`

### Step 3 — Add a Book

**POST** `http://localhost:8080/api/books`
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "genre": "Software Engineering",
  "totalCopies": 5,
  "publishedYear": 2008
}
```

### Step 4 — Register a Member

**POST** `http://localhost:8080/api/members`
```json
{
  "name": "Akhil Kumar",
  "email": "akhil@example.com",
  "phone": "9876543210",
  "membershipType": "PREMIUM"
}
```

### Step 5 — Issue a Book

**POST** `http://localhost:8080/api/transactions/issue`
```json
{
  "bookId": "<book_id from Step 3>",
  "memberId": "<member_id from Step 4>"
}
```

### Step 6 — Return a Book

**POST** `http://localhost:8080/api/transactions/return/<transaction_id>`

*(No body needed)*

---

## 🔒 Security Design

| Aspect | Implementation |
|--------|---------------|
| Password Hashing | BCrypt with auto-salting (work factor 10) |
| Token Format | JWT signed with HMAC-SHA256 |
| Token Expiry | 24 hours (configurable via `jwt.expiration`) |
| Session Policy | Stateless — no server-side sessions |
| CSRF | Disabled (not needed for stateless REST) |
| Protected Routes | All `/api/**` except `/api/auth/**` |
| Filter Order | `JwtAuthFilter` runs before `UsernamePasswordAuthenticationFilter` |

---

## 📊 HTTP Status Code Reference

| Code | Meaning | When |
|------|---------|------|
| `200` | OK | Successful GET, update, return |
| `201` | Created | Successful register/add |
| `400` | Bad Request | No copies available, validation failure |
| `401` | Unauthorized | Invalid/missing JWT or wrong credentials |
| `404` | Not Found | Book/member/transaction ID doesn't exist |
| `409` | Conflict | Duplicate ISBN, email, or username |
| `500` | Server Error | Unexpected failures |

---

## 👨‍💻 Author

**Akhil Kumar**  
📧 Academic Project — Library Management System  
🔗 [GitHub](https://github.com/akhilkumar9664-dot)

---

<div align="center">
  <sub>Built with ❤️ using Java Spring Boot & MongoDB</sub>
</div>
