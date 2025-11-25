# JPA Relationships Cheat Sheet

This README summarizes OneToOne, OneToMany/ManyToOne, and ManyToMany relationships in JPA with cascade types, fetch strategies, JSON handling, and common pitfalls.


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

## 1️⃣ OneToOne

**Definition:** One entity relates to exactly **one** instance of another entity.

**Example:** `User ↔ Address`

```java
@Entity
class User {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
}

@Entity
class Address {
    @OneToOne(mappedBy = "address", fetch = FetchType.LAZY)
    private User user;
}
```

**Key Points:**

- Owning side has `@JoinColumn`
- Cascade works only on owning side
- Fetch default: EAGER
- OrphanRemoval deletes child when removed from parent

---

## 2️⃣ OneToMany / ManyToOne

**Definition:** One entity relates to **many** instances of another.

**Example:** `User ↔ Address`

```java
@Entity
class User {
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
}

@Entity
class Address {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
```

**Key Points:**

- ManyToOne side is owning
- Fetch default: ManyToOne → EAGER, OneToMany → LAZY
- Use convenience methods to sync both sides
- N+1 problem common when fetching collections, fix with `JOIN FETCH` or `@EntityGraph`

---

## 3️⃣ ManyToMany

**Definition:** Many entities relate to many entities.

**Example:** `Student ↔ Course`

```java
@Entity
class Student {
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}

@Entity
class Course {
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Student> students = new HashSet<>();
}
```

**Key Points:**

- Owning side defines `@JoinTable`
- Cascade only on owning side
- Lazy fetch recommended
- Avoid `@JsonManagedReference/@JsonBackReference` for ManyToMany
- Exclude collections in Lombok `@EqualsAndHashCode` to prevent StackOverflowError

---

## 4️⃣ Cascade Types

| Type    | Effect                                |
| ------- | ------------------------------------- |
| PERSIST | Save child automatically              |
| MERGE   | Update child automatically            |
| REMOVE  | Delete child automatically            |
| REFRESH | Refresh child state                   |
| DETACH  | Detach child from persistence context |
| ALL     | All of the above                      |

- Cascade applies **only on owning side**

---

## 5️⃣ Fetch Strategies

| Type  | When used                     | Default                |
| ----- | ----------------------------- | ---------------------- |
| EAGER | Always load child with parent | OneToOne / ManyToOne   |
| LAZY  | Load child only when accessed | OneToMany / ManyToMany |

- N+1 problem occurs when accessing lazy collections in a loop
- Fix with `JOIN FETCH` or DTO projection

---

## 6️⃣ Common Pitfalls

1. **Infinite recursion in JSON**: Use `@JsonIgnore` on inverse side
2. **StackOverflowError with Lombok**: Exclude collections in `@EqualsAndHashCode`
3. **Orphan removal**: Works only on OneToOne/OneToMany
4. **N+1 queries**: Happens when fetching lazy collections in loop, fix with `JOIN FETCH` or `@EntityGraph`

---

## 7️⃣ Summary Table of Relationships

| Relationship | Owning side           | Cascade works? | Fetch default    | JSON consideration            |
| ------------ | --------------------- | -------------- | ---------------- | ----------------------------- |
| OneToOne     | JoinColumn side       | Yes            | EAGER            | JsonManagedReference optional |
| OneToMany    | Many side (ManyToOne) | Yes            | OneToMany → LAZY | JsonIgnore on inverse side    |
| ManyToOne    | Many side             | Yes            | EAGER            | Usually no JSON issue         |
| ManyToMany   | JoinTable side        | Yes            | LAZY             | JsonIgnore on inverse side    |

---

**Notes:**

- Always maintain bidirectional relationships using convenience methods
- Use LAZY fetch for collections in production
- Avoid infinite recursion in JSON by ignoring inverse side

