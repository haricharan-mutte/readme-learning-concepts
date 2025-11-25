# JPA & Spring Boot Guide

This document summarizes detailed discussions on JPA, Spring Boot, transaction management, and related concepts, including entity lifecycle, caching, lazy loading, and Spring Data JPA behavior.

---

## 1. `@JoinColumn` vs `mappedBy`

- ``: Indicates the owning side in a relationship; specifies the foreign key column.
- ``: Indicates the inverse side; specifies the field in the owning side that owns the relationship.

**Owning side:**

- Manages the relationship.
- Changes here are persisted in the join column.

**Example:**

```java
@Entity
class Department {
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
}

@Entity
class Employee {
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;
}
```

---

## 2. Cascading and Fetching Strategies

### Cascading

- Automatically propagate operations (persist, merge, remove, refresh, detach) from parent to child.
- Example: `cascade = CascadeType.ALL`

### Fetching

- **LAZY (default for **``**)**: loads data on access.
- **EAGER (default for **``**)**: loads immediately.

**Default types:**

- OneToOne: EAGER
- OneToMany: LAZY
- ManyToOne: EAGER
- ManyToMany: LAZY

---

## 3. Query Management in JPA

- **JPQL**: `SELECT e FROM Employee e WHERE e.name = :name`
- **Native SQL**: `@Query(value = "SELECT * FROM employee WHERE name = :name", nativeQuery = true)`
- **Criteria API**: Programmatic query building.
- **Spring Data JPA derived queries**: `findByName(String name)`

---

## 4. Locking Strategy

- **Optimistic Locking**: Assumes low contention; uses `@Version` field to prevent overwrites.
- **Pessimistic Locking**: Locks row in DB (`LockModeType.PESSIMISTIC_WRITE`) to prevent concurrent changes.

---

## 5. Transaction Management

### Transaction Types

- **Programmatic**: Using `TransactionTemplate` or `PlatformTransactionManager`.
- **Declarative**: Using `@Transactional` annotations.

### ACID Properties

- **Atomicity**: All-or-nothing.
- **Consistency**: DB remains consistent.
- **Isolation**: Transactions don’t interfere improperly.
- **Durability**: Committed changes persist.

### Transaction Attributes

- **Propagation**: REQUIRED, REQUIRES\_NEW, NESTED, SUPPORTS, MANDATORY, NOT\_SUPPORTED, NEVER.
- **Isolation**: READ\_UNCOMMITTED, READ\_COMMITTED, REPEATABLE\_READ, SERIALIZABLE.
- **Rollback Rules**: Rollback on specific exceptions.
- **Timeout**: Auto-rollback if exceeded.
- **Read-Only**: Optimize for read operations.

### Example

```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.REPEATABLE_READ,
    rollbackFor = Exception.class
)
public void transfer(Long fromId, Long toId, double amount) {
    // perform DB operations
}
```

---

## 6. EntityManager, Persistence Context & Lifecycle

### Key Concepts

- **EntityManager**: Interface to interact with JPA entities.
- **Persistence Context (PC)**: First-level cache; manages entity states.
- **Entity States**: Transient, Persistent, Detached, Removed.
- **Lazy Loading**: Associations loaded on access.
- **Flush**: Sync changes to DB.

### Example Flow

```java
@Transactional
public void workflow() {
    Employee emp = new Employee("Hari"); // transient
    em.persist(emp); // persistent
    Employee e = em.find(Employee.class, 1L); // persistent
    Department dept = e.getDepartment(); // lazy, triggers SELECT
    e.setName("Updated"); // tracked, flush on commit
}
```

### Notes

- Each request thread gets its own **Persistence Context**.
- EntityManagerFactory is singleton; EntityManager is per transaction.
- Lazy loading proxies throw `LazyInitializationException` if accessed outside transaction.

---

## 7. Spring Data JPA Repositories

- Repository interfaces are proxies that **delegate to EntityManager** internally.
- Behaviors: Persistence context, first-level cache, entity states, lazy loading all work the same as direct EntityManager usage.
- Example:

```java
Employee e = repo.findById(1L).get(); // uses EntityManager behind the scenes
repo.save(emp); // uses EntityManager.persist/merge
```

---

## 8. Best Practices

- Use `@Transactional` at service layer.
- Keep transactions short.
- Understand lazy vs eager loading.
- Use DTOs to prevent N+1 problems.
- Avoid manual EntityManager management unless multi-datasource is required.
- Monitor and tune caching (first-level + optional second-level).
- Configure rollback rules and isolation based on business needs.

---

## References

- [Spring Boot JPA Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.jpa-and-spring-data)
- [JPA Specification](https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)

