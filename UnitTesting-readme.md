#🧠 PART 1: Foundations of Unit Testing Frameworks
---

✅ What is Unit Testing?
Unit testing is isolating a class/method and testing its logic without depending on actual frameworks, databases, or APIs.

**For example:**
You're testing a StudentService class without hitting the DB. You mock StudentRepository.

---
✅ What Makes a Good Unit Test?
---

Fast – should run in milliseconds

Independent – no external services like DBs

Repeatable – always returns the same result

Focused – tests one thing at a time

---
🧱 PART 2: Common Java Testing Libraries

| Library              | Purpose                                 |
| -------------------- | --------------------------------------- |
| **JUnit 5**          | Writing test methods and assertions     |
| **Mockito**          | Mocking collaborators (repos, services) |
| **AssertJ**          | Fluent assertions                       |
| **Hamcrest**         | Matcher-style assertions                |
| **Spring Boot Test** | Testing Spring layers with context      |

---
 # PART 3: Structure of a Test Class
---
A. Anatomy of a Unit Test Class

    @ExtendWith(MockitoExtension.class) // Enables Mockito
    public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void testFindStudentById() {
        // Arrange (setup)
        // Act (call method)
        // Assert (verify result)
    }
}

---

| Part               | Purpose                                          |
| ------------------ | ------------------------------------------------ |
| `@ExtendWith(...)` | Enables JUnit to support Mockito annotations     |
| `@Mock`            | Creates a mock (fake) of a class (no real logic) |
| `@InjectMocks`     | Injects mocks into the tested class              |
| `@Test`            | Marks a method as a test                         |

---
#🧭 PART 4: Testing Layers in Spring Boot

Let’s take a Student microservice with layers:

Controller -> Service -> Repository -> DB

| Layer              | What to test                    | Tools/Annotations                     |
| ------------------ | ------------------------------- | ------------------------------------- |
| **Unit**           | One class only (no Spring)      | `JUnit + Mockito + AssertJ`           |
| **Web/Controller** | API input/output & status codes | `@WebMvcTest + MockMvc`               |
| **Service**        | Business logic (with mocks)     | `@ExtendWith(MockitoExtension.class)` |
| **Repository**     | DB queries                      | `@DataJpaTest`                        |
| **Integration**    | End-to-end (with DB)            | `@SpringBootTest`                     |

#🧑‍💻 PART 5: Hands-on – Testing a Student Microservice

🎯 1. StudentServiceTest – Pure Unit Test

    @ExtendWith(MockitoExtension.class)
    public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void shouldReturnStudentWhenIdExists() {
        Student mockStudent = new Student(1L, "John");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        Student result = studentService.getStudentById(1L);

        assertThat(result.getName()).isEqualTo("John");
        verify(studentRepository).findById(1L); // verifies that method was called
    }
    }
    🔍 Purpose: Test service logic, mock dependencies

---
 # 2. StudentControllerTest – Controller + Web Layer Test

    @WebMvcTest(StudentController.class)
    class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void shouldReturnStudentById() throws Exception {
        Student student = new Student(1L, "Alice");
        when(studentService.getStudentById(1L)).thenReturn(student);

        mockMvc.perform(get("/students/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Alice"));
    }
    }
    🔍 Purpose: Test controller routing, input/output, HTTP codes
    @MockBean: Mocks a bean inside Spring context
    MockMvc: Fake HTTP requests without real server
---
# 🎯 3. StudentRepositoryTest – Data Layer Test

    @DataJpaTest
    class StudentRepositoryTest {

    @Autowired
    private StudentRepository repository;

    @Test
    void shouldSaveAndFetchStudent() {
        Student student = new Student(null, "Tom");
        repository.save(student);

        List<Student> all = repository.findAll();
        assertThat(all).extracting(Student::getName).contains("Tom");
    }
    }
    🔍 Purpose: Ensure queries and persistence work
    Uses in-memory H2 DB

---
# 4. StudentIntegrationTest – Full Spring Context

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureMockMvc
    class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository repository;

    @Test
    void testAddAndFetchStudent() throws Exception {
        repository.save(new Student(null, "Jane"));

        mockMvc.perform(get("/students"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("Jane"));
    }
    }

---
# 🔖 Summary of Key Annotations

| Annotation              | Use Case                                       |
| ----------------------- | ---------------------------------------------- |
| `@Test`                 | Marks a test method (JUnit 5)                  |
| `@ExtendWith(...)`      | Enables Mockito or Spring support              |
| `@Mock`                 | Create a dummy object                          |
| `@InjectMocks`          | Inject mocks into the class being tested       |
| `@WebMvcTest`           | Test only Controller layer                     |
| `@DataJpaTest`          | Test Repository layer                          |
| `@SpringBootTest`       | Full Spring Boot app context                   |
| `@MockBean`             | Mock a Spring-managed bean                     |
| `@AutoConfigureMockMvc` | Auto configures `MockMvc` in `@SpringBootTest` |
| `@Transactional`        | Rollbacks DB changes after each test           |


# 🧰 What to Test Where?

| Test Class               | Type        | What You’re Testing                 |
| ------------------------ | ----------- | ----------------------------------- |
| `StudentServiceTest`     | Unit        | Logic only, with mocks              |
| `StudentControllerTest`  | Web         | HTTP inputs/outputs using `MockMvc` |
| `StudentRepositoryTest`  | Data        | DB queries and saving records       |
| `StudentIntegrationTest` | Integration | End-to-end test including DB        |

 
