# 📘 Complete Guide: REST API with Spring Boot

## 📌 Table of Contents

1. What is REST?
2. Why Do We Need REST?
3. What is Spring MVC?
4. How to Implement REST with Spring MVC in Spring Boot
5. Ways to Implement REST API in Spring Boot
6. Different REST API Approaches
7. Best Practices for REST API Design
8. HTTP API Fundamentals
9. Common HTTP Status Codes
10. Tools and Utilities to Improve REST API Implementation
11. Summary

---

## 1. What is REST?

**REST** (Representational State Transfer) is an architectural style for designing networked applications using standard HTTP methods (GET, POST, PUT, DELETE). REST APIs operate on resources identified by URIs and represented in formats like JSON or XML.

### Key Principles:

- Stateless interactions
- Uniform interface
- Resource-based (nouns, not actions)
- Uses standard HTTP methods

---

## 2. Why Do We Need REST?

- Platform-independent
- Lightweight and fast
- Easy to use over HTTP
- Language agnostic
- Great for mobile and web clients

---

## 3. What is Spring MVC?

**Spring MVC** is a web framework in the Spring ecosystem that provides a clean separation of concerns:

- **Model**: Data and business logic
- **View**: UI (Not used in REST)
- **Controller**: Handles HTTP requests

Spring MVC supports RESTful APIs through annotations like `@RestController`, `@RequestMapping`, etc.

---

## 4. How to Implement REST with Spring MVC in Spring Boot

### Required Dependency:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Sample Structure:

- `@RestController`: Marks class as REST API controller
- `@GetMapping`, `@PostMapping`: Map HTTP methods
- `@RequestBody`, `@PathVariable`: Data extraction

```java
@RestController
@RequestMapping("/users")
public class UserController {
  @GetMapping("/{id}")
  public User getUser(@PathVariable Long id) {
    return new User(id, "Hari", "hari@example.com");
  }
}
```

---

## 5. Ways to Implement REST API in Spring Boot

### A. Using Spring MVC (Blocking)

- Most common, synchronous
- Uses standard Servlet API
- Easy to learn and debug

### B. Using Spring WebFlux (Non-Blocking)

- Reactive programming with Mono/Flux
- Asynchronous and event-driven
- Suitable for high-concurrency apps

### C. Using Spring Data REST

- Exposes JPA repositories as REST endpoints automatically
- Minimal coding
- Best for rapid prototyping/admin tools

### D. Using Spring HATEOAS

- Adds hypermedia links in responses
- Navigable APIs
- Rare in modern REST unless hypermedia is required

---

## 6. Different REST API Approaches

| Approach         | When to Use                 | Benefit                             |
| ---------------- | --------------------------- | ----------------------------------- |
| Spring MVC       | General purpose REST APIs   | Mature, widely adopted              |
| Spring WebFlux   | Reactive, non-blocking apps | Scalable under high load            |
| Spring Data REST | Quick CRUD REST             | Rapid development, less boilerplate |
| HATEOAS          | Hypermedia navigation       | REST maturity level 3               |

---

## 7. Best Practices for REST API Design

- Use **DTOs** instead of exposing entities
- Validate input using `@Valid`
- Return appropriate **HTTP status codes**
- Handle errors globally using `@ControllerAdvice`
- Use **versioning** (URI, headers, media-type)
- Follow naming conventions: `/users/{id}` not `/getUser`
- Use pagination and filtering for large data sets
- Use Swagger/OpenAPI for documentation
- Secure APIs with Spring Security or JWT

---

## 8. HTTP API Fundamentals

### Structure:

- **Method**: GET, POST, PUT, DELETE, PATCH
- **URL/URI**: `/api/v1/users`
- **Headers**: Content-Type, Authorization
- **Body**: JSON data (for POST/PUT)
- **Response**: JSON + Status Code

### HTTP Methods:

| Method | Purpose              |
| ------ | -------------------- |
| GET    | Retrieve data        |
| POST   | Create new resource  |
| PUT    | Update full resource |
| PATCH  | Partial update       |
| DELETE | Delete resource      |

---

## 9. Common HTTP Status Codes

| Code | Meaning               |
| ---- | --------------------- |
| 200  | OK                    |
| 201  | Created               |
| 204  | No Content            |
| 400  | Bad Request           |
| 401  | Unauthorized          |
| 403  | Forbidden             |
| 404  | Not Found             |
| 500  | Internal Server Error |

---

## 10. Tools and Utilities for Effective REST API

| Tool/Utility              | Purpose                                       |
| ------------------------- | --------------------------------------------- |
| **Postman**               | API testing client                            |
| **Swagger/OpenAPI**       | API documentation and interactive UI          |
| **Lombok**                | Reduce boilerplate code in model classes      |
| **MapStruct/ModelMapper** | Auto-map between Entity and DTO               |
| **Spring Security / JWT** | Authentication and authorization              |
| **Actuator**              | Monitoring and health endpoints for REST APIs |
| **WireMock**              | Mock API responses for testing                |

---

## 11. Summary

Spring Boot + REST API using Spring MVC is a production-ready, scalable, and maintainable approach for modern applications. Depending on the need (reactive, prototyping, hypermedia), you can choose between MVC, WebFlux, Data REST, or HATEOAS. Following best practices, correct versioning, DTO separation, and tooling ensures your REST API is clean, secure, and future-proof.

