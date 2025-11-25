
# N+1 Query Problem in JPA / Hibernate

## **1. What is N+1 Query Problem?**

The **N+1 query problem** occurs when fetching a collection of entities causes additional queries for each entity's associations.  

**Example Scenario:**
- Fetch 10 users from the database.
- For each user, fetch their addresses.
- Instead of 1 query, Hibernate executes:
  1. `SELECT * FROM user`  → 1 query
  2. `SELECT * FROM address WHERE user_id = ?` → N queries (10 queries for 10 users)
- Total = N + 1 queries → performance problem.

---

## **2. Solutions to N+1 Problem**

### **2.1 Eager Fetching**
```java
@OneToMany(fetch = FetchType.EAGER)
private List<Address> addresses;
```
- Pros: Simple, data available immediately.
- Cons: Fetches all data even if not needed → memory overhead.
- Best for: Always needing the association.

---

### **2.2 JOIN FETCH in JPQL**
```java
SELECT u FROM User u JOIN FETCH u.addresses
```
- Reduces N+1 to 1 query.
- Pros: Fetches only required associations.
- Cons: Cartesian product risk with multiple collections.
- Best for: Specific queries where associations are needed.

---

### **2.3 EntityGraph**

#### **Option 1: Named EntityGraph**
```java
@NamedEntityGraph(
    name = "User.addresses",
    attributeNodes = @NamedAttributeNode("addresses")
)
@Entity
public class User { ... }
```

Repository:
```java
@EntityGraph(value = "User.addresses", type = EntityGraph.EntityGraphType.LOAD)
Optional<User> findById(Long id);
```

#### **Option 2: Dynamic / Ad-hoc EntityGraph**
```java
@EntityGraph(attributePaths = "addresses")
Optional<User> findById(Long id);
```

- Pros: Avoids N+1 without changing entity fetch type.
- Named graphs are reusable, dynamic graphs are one-off.
- Best for: Controlling fetch strategy per query.

---

### **2.4 DTO / Projection**
```java
public class UserAddressDTO {
    private String userName;
    private String city;
    public UserAddressDTO(String userName, String city) { ... }
}
```

Repository:
```java
@Query("SELECT new com.example.UserAddressDTO(u.name, a.city) " +
       "FROM User u JOIN u.addresses a WHERE u.id = :userId")
List<UserAddressDTO> findUserWithAddresses(@Param("userId") Long userId);
```

- Pros: Fetch only required data, single query, avoids entity overhead.
- Best for: Read-only or reporting queries.

---

### **2.5 Batch / IN Clause / Subselect**
```java
@BatchSize(size = 10)
@OneToMany(mappedBy = "user")
private List<Address> addresses;
```
- Hibernate executes batches instead of N queries.
- Best for: Lazy collections frequently accessed.

---

## **3. Comparison Table**

| Approach         | N+1 Avoided? | Best Use Case                          | Trade-offs |
|-----------------|--------------|----------------------------------------|------------|
| Eager Fetch      | ✅           | Always need association                | Heavy memory usage |
| JOIN FETCH       | ✅           | Specific query, known relations       | Cartesian product risk |
| Named EntityGraph| ✅           | Reusable, controlled fetching         | Slight setup |
| Dynamic EntityGraph| ✅         | One-off queries                        | Not reusable |
| DTO / Projection | ✅           | Read-only / reporting                  | Not managed entities |
| Batch / Subselect| ✅           | Lazy collections, moderate data       | Still multiple queries |

---

## **4. Summary / Rules of Thumb**

1. **Use JOIN FETCH** or **EntityGraph** for transactional reads needing associations.
2. **Use DTOs** for read-only, reporting, or large datasets.
3. **Use Batch / Subselect** when you want lazy loading but want to reduce N+1 without changing fetch type.
4. Keep **default fetch type LAZY** for collections to avoid unnecessary queries.

---

## **5. Example: User–Address EntityGraph**

```java
@Entity
public class User {
    @Id @GeneratedValue private Long id;
    private String name;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Address> addresses;
}

@EntityGraph(attributePaths = "addresses")
Optional<User> findById(Long id);
```

- Executes **one query** to fetch user and addresses.
- Avoids N+1 problem dynamically.

---

## **6. Example: DTO / Projection**

```java
public class UserAddressDTO {
    private String userName;
    private String city;
    public UserAddressDTO(String userName, String city) { ... }
}

@Query("SELECT new com.example.UserAddressDTO(u.name, a.city) " +
       "FROM User u JOIN u.addresses a WHERE u.id = :userId")
List<UserAddressDTO> findUserWithAddresses(@Param("userId") Long userId);
```

- Fetches only required fields.
- Single query, avoids entity overhead.

---

**References:**
- [Hibernate Docs: EntityGraph](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#fetching-entitygraph)
- [Spring Data JPA EntityGraph](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-graph)
