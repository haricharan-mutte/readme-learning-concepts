
# Spring MVC Comprehensive Guide with Examples

## Overview

This guide covers the essential and advanced concepts of Spring MVC with Spring Boot, explaining core ideas and showing practical examples. Topics include request handling, binding, validation, response rendering, interceptors and filters, file upload/download, content negotiation, internationalization, testing, and REST API fundamentals.
---

## Table of Contents

1. [Spring MVC Basics](#spring-mvc-basics)  
2. [Request Handling and Binding](#request-handling-and-binding)  
3. [Message Converters and Validation](#message-converters-and-validation)  
4. [Response Handling](#response-handling)  
5. [View Technologies](#view-technologies)  
6. [Interceptors and Filters](#interceptors-and-filters)  
7. [HTTPS Request Flow](#https-request-flow)  
8. [File Upload and Download](#file-upload-and-download)  
9. [Advanced Topics](#advanced-topics)  
10. [Testing Spring MVC](#testing-spring-mvc)  
11. [REST API Concepts](#rest-api-concepts)  
12. [HTTP Request Structure and Headers](#http-request-structure-and-headers)  

---

## Spring MVC Basics

- Spring MVC uses the DispatcherServlet as front controller.
- Controllers annotated with `@Controller` or `@RestController` receive requests.
- Uses annotations like `@GetMapping`, `@PostMapping` to map URLs.
- Supports view rendering and REST responses.

Example:

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring MVC!";
    }
}
```

---

## Request Handling and Binding

### `@RequestParam` and `@PathVariable` for parameters:
```java
@GetMapping("/user/{id}")
public String getUser(@PathVariable String id, @RequestParam(defaultValue="en") String lang) {
    return "User ID: " + id + ", Language: " + lang;
}
```

### `@ModelAttribute` for form binding:
```java
@PostMapping("/form")
public String submitForm(@ModelAttribute User user) {
    return "User: " + user.getName();
}
```

### `@RequestBody` for JSON/XML input:
```java
@PostMapping("/api/user")
public User createUser(@RequestBody User user) {
    return user;
}
```

---

## Message Converters and Validation

- Spring Boot auto-configures converters (e.g., Jackson for JSON).
- Customize by implementing `WebMvcConfigurer`.
- Bean Validation with `@Valid` and constraints like `@NotNull`, `@Size`.

Example validation:

```java
@PostMapping("/api/user")
public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
    // method logic
}
```

Global exception handling for validation:

```java
@ControllerAdvice
public class ValidationHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(...) { ... }
}
```

---

## Response Handling

- Use `@ResponseBody` or `@RestController` to write response body.
- `ResponseEntity` to customize status, headers, body.

Example:

```java
@GetMapping("/user/{id}")
public ResponseEntity<User> getUser(@PathVariable String id) {
    return ResponseEntity.ok(new User("Alice", 25));
}
```

---

## View Technologies

- Spring Boot supports Thymeleaf and JSP.
- Controller returns view name plus model data.
- Thymeleaf example:

```java
@GetMapping("/greet")
public String greet(Model model) {
    model.addAttribute("name", "John");
    return "greeting"; // greeting.html
}
```

---

## Interceptors and Filters

- **Filters** run before DispatcherServlet, belong to servlet container.
- **Interceptors** run inside Spring MVC pipeline.
- Register interceptors in `WebMvcConfigurer`.
- Use for logging, auth, modifying requests/responses.

Example interceptor:

```java
public class CustomInterceptor implements HandlerInterceptor { ... }

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomInterceptor());
    }
}
```

---

## HTTPS Request Flow

1. Client sends HTTPS request 9 TLS Handshake 9 decrypted by server.
2. Servlet container processes filters.
3. DispatcherServlet invokes controllers.
4. Controller processes request and returns response.
5. Response passes filters backward and is encrypted and sent to client.

---

## File Upload and Download

- Multipart file upload accessible with `MultipartFile`.
- Download using `ResponseEntity<Resource>`.

Upload example:

```java
@PostMapping("/upload")
public String uploadFile(@RequestParam MultipartFile file) { ... }
```

Download example:

```java
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) { ... }
```

---

## Advanced Topics

- Content Negotiation (`produces`, `consumes`)
- Internationalization (i18n) support with `MessageSource` and `LocaleResolver`
- Caching, Security, Pagination etc.

---

## Testing Spring MVC

- Use `@WebMvcTest` with `MockMvc` for controller unit tests.
- Verify status, response contents, headers.

Example test:

```java
@WebMvcTest(GreetingController.class)
public class GreetingTest {
    @Autowired private MockMvc mvc;
    @Test void testHello() throws Exception {
        mvc.perform(get("/hello"))
           .andExpect(status().isOk())
           .andExpect(content().string("Hello, Spring MVC!"));
    }
}
```

---

## REST API Concepts

- REST uses HTTP methods on resources (URIs).
- Stateless, cacheable, uniform interface.
- Responses use standard HTTP codes, JSON commonly used format.

---

## HTTP Request Structure and Headers

- Request line: METHOD URI HTTP-version
- Headers: key-value metadata (`Content-Type`, `Authorization`, etc.)
- Optional body for POST/PUT.

Access headers in Spring MVC:

```java
@GetMapping("/header")
public String getHeader(@RequestHeader("User-Agent") String userAgent) {
    return userAgent;
}
```

--- COMPLETE GUIDE-------

1. Basics of Spring Boot and Spring MVC
   Understand Spring Boot basics (starter projects, auto-configuration, dependency management)

Introduction to Spring MVC architecture and components (DispatcherServlet, Controller, Model, View, HandlerMapping)

Setting up a simple Spring Boot project with Spring Web (spring-boot-starter-web)

2. Creating Controllers and Routing
   Understanding @Controller and @RestController annotations

Request handling with @RequestMapping, @GetMapping, @PostMapping, etc.

Path variables and query parameters (@PathVariable, @RequestParam)

Response types: returning String views vs JSON (REST) responses

3. Request and Response Handling
   Binding form data using @ModelAttribute and command objects

Understanding Model, ModelMap, and ModelAndView

Request body and response body annotations (@RequestBody, @ResponseBody)

Handling form submissions and validation basics

4. View Technologies
   Default view rendering with Thymeleaf or JSP

Configuring view resolvers

Dynamic page rendering and passing data to views

5. Form Handling and Validation
   Binding form data to Java objects

Using JSR-303/JSR-380 Bean Validation annotations (@Valid, @NotNull, etc.)

Handling validation errors and binding results

Custom validators

6. Exception Handling in Spring MVC
   Using @ExceptionHandler methods in controllers

Global exception handling with @ControllerAdvice

Returning custom error pages or JSON error responses

7. Interceptors and Filters
   Implementing HandlerInterceptor for pre- and post-processing requests

Understanding Filters vs Interceptors

Use cases for interceptors (logging, authentication, etc.)

8. File Upload and Download
   Handling multipart file uploads with MultipartFile

Serving files for download in response

9. Advanced Topics
   Content negotiation (produces and consumes in request mapping)

Internationalization (i18n) support in Spring MVC

Asynchronous requests handling with DeferredResult or WebAsyncTask

Integrating Spring Security with Spring MVC

10. Testing Spring MVC Controllers
    Writing unit tests with MockMvc

Testing request mappings, parameters, and responses
