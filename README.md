# Junits Foundation And Basics with Spring Boot Starter Test Dependency
---
**spring-boot-starter-test** is a Spring Boot starter dependency that bundles common testing libraries needed for testing Spring Boot applications.

**It provides**:

Unit Testing,
Integration Testing,
Mocking,
Web Layer Testing,
Data Layer Testing,
All wired to work with Spring Boot's auto-configuration

---
# ✅ Dependencies Included in spring-boot-starter-test
Here are the key libraries it includes (transitively):

| Library               | Purpose                                                             |
| --------------------- | ------------------------------------------------------------------- |
| **JUnit 5 (Jupiter)** | Core testing framework (writing and running tests)                  |
| **Mockito**           | Creating mocks and stubs                                            |
| **Hamcrest**          | Readable assertions (e.g., `assertThat(a, is(b))`)                  |
| **AssertJ**           | Fluent assertions (e.g., `assertThat(x).isEqualTo(y)`)              |
| **Spring Test**       | Utilities for testing Spring context, beans, transactions, etc.     |
| **Spring Boot Test**  | Auto-config, @WebMvcTest, @DataJpaTest, @SpringBootTest annotations |
| **JsonPath**          | Extract/verify values from JSON responses                           |
| **MockMvc**           | Mock HTTP request testing for controllers (without starting server) |


# ✅ When You Use Which Tool

| Scenario                         | Tool/Annotation/Support    |
| -------------------------------- | -------------------------- |
| Unit testing a POJO              | JUnit + AssertJ / Hamcrest |
| Testing service class with mocks | JUnit + Mockito            |
| Testing controller layer         | `@WebMvcTest` + `MockMvc`  |
| Testing full Spring context      | `@SpringBootTest`          |
| Testing JPA repository           | `@DataJpaTest`             |
| Verifying JSON output            | `MockMvc` + `jsonPath`     |

# ✅ Recommended Stack for Spring Boot Projects

| Type                  | Library Stack                                      |
| --------------------- | -------------------------------------------------- |
| Unit Test             | JUnit 5 + Mockito + AssertJ                        |
| Spring Component Test | `spring-boot-starter-test` (with JUnit + MockMvc)  |
| JSON API Test         | MockMvc + JsonPath or RestAssured                  |
| Repository Test       | `@DataJpaTest` + H2 + Testcontainers (for real DB) |
| External API Stubbing | WireMock                                           |
| Acceptance Test (BDD) | Cucumber (optional)                                |
| Full Integration Test | `@SpringBootTest` + Testcontainers                 |

# ✅ Example Usage Quick Snippets

@ExtendWith(MockitoExtension.class)
public class MyServiceTest {

    @Mock
    private MyRepository repo;

    @InjectMocks
    private MyService service;

    @Test
    void testSomething() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Entity()));
        ...
    }
}

# Controller Test with @WebMvcTest

@WebMvcTest(MyController.class)
class MyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testApi() throws Exception {
        mockMvc.perform(get("/api/something"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1));
    }
}

# Repository Test with @DataJpaTest

@DataJpaTest
class MyRepoTest {

    @Autowired
    private MyRepository repo;

    @Test
    void testSaveAndFind() {
        MyEntity e = repo.save(new MyEntity(...));
        assertThat(repo.findById(e.getId())).isPresent();
    }
}



