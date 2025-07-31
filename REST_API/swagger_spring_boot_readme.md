# Swagger Integration with Spring Boot

This guide provides a complete understanding of Swagger (OpenAPI) integration with Spring Boot applications. It covers what Swagger is, why it's needed, the different tools available for integration, implementation steps, and the pros and cons of each approach.

---

## 📌 What is Swagger?

Swagger is a set of tools built around the OpenAPI Specification that helps you:

- Design, build, document, and consume REST APIs
- Automatically generate interactive documentation
- Enable API exploration and testing via a UI (Swagger UI)

---

## 🤔 Why is Swagger Needed?

- **Developer Communication**: Helps frontend and backend teams understand and collaborate on APIs.
- **Interactive Documentation**: API consumers can test endpoints via the browser.
- **Validation and Code Generation**: Helps in generating server stubs and client SDKs.
- **API Contract Enforcement**

---

## 🚀 Swagger Integration Approaches in Spring Boot

### 1. **Springfox** (Legacy)

#### ✅ Setup

```xml
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
    <version>3.0.0</version>
</dependency>
```

#### ✅ Configuration

```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example"))
            .paths(PathSelectors.any())
            .build();
    }
}
```

#### 🌐 Swagger UI

Access at: `http://localhost:8080/swagger-ui/`

#### ⚠️ Limitations

- No longer actively maintained.
- Not compatible with Spring Boot 3.x / Jakarta EE.

---

### 2. **springdoc-openapi-ui**

#### ✅ Setup (For Spring Boot 2.x)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

#### ✅ Features

- Supports OpenAPI 3.0
- Autoconfigures Swagger UI
- No need for extra configuration for basic setup

#### 🌐 Swagger UI

Access at: `http://localhost:8080/swagger-ui.html`

#### ⚠️ Limitations

- Not compatible with Spring Boot 3.x (Jakarta namespace).

---

### 3. **springdoc-openapi-starter-webmvc-ui** (Recommended for Spring Boot 3.x+)

#### ✅ Setup (For Spring Boot 3.x+)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

#### ✅ Features

- Fully supports Spring Boot 3 and Jakarta EE
- Better performance and OpenAPI 3.1+ features
- Enhanced plugin ecosystem

#### 🌐 Swagger UI

Access at: `http://localhost:8080/swagger-ui.html`

#### ⚠️ Limitations

- Only works with Spring Boot 3.x and above

---

## 🆚 Comparison Table

| Feature                   | Springfox | springdoc-openapi-ui  | springdoc-openapi-starter-webmvc-ui |
| ------------------------- | --------- | --------------------- | ----------------------------------- |
| Spring Boot 3.x Support   | ❌         | ❌                     | ✅                                   |
| Maintenance               | ❌         | ⚠️ (maintenance only) | ✅                                   |
| OpenAPI 3.x+              | ❌         | ✅                     | ✅                                   |
| Swagger UI Autoconfigured | ❌         | ✅                     | ✅                                   |

---

## 🔧 Useful Configurations

- Customize API info:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("My REST API")
            .version("v1")
            .description("Documentation of My REST API")
        );
}
```

- Grouped APIs:

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/**")
        .build();
}
```

---

## 🛠️ Alternatives to Swagger

| Tool          | Description                            |
| ------------- | -------------------------------------- |
| **Redoc**     | Clean, minimalistic documentation UI   |
| **Postman**   | API testing and mock server capability |
| **Stoplight** | Visual OpenAPI designer                |
| **Insomnia**  | API testing with OpenAPI sync          |

---

## ✅ Recommendation

| Spring Boot Version | Suggested Tool                        |
| ------------------- | ------------------------------------- |
| 2.x                 | `springdoc-openapi-ui`                |
| 3.x and above       | `springdoc-openapi-starter-webmvc-ui` |

---

## 📚 References

- [Springdoc OpenAPI Docs](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)

---

## 🔚 Conclusion

Swagger simplifies REST API development and documentation. Choosing the right implementation based on your Spring Boot version ensures future compatibility and ease of maintenance.

