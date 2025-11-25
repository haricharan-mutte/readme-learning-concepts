
# JDBC, Hibernate, and JPA - Consolidated Technical Reference

## 1. JDBC (Java Database Connectivity)
- API for interacting with relational databases using SQL.
- Components:
  - DriverManager
  - Connection
  - Statement / PreparedStatement
  - ResultSet
- Operations:
  - Open connection
  - Create and execute SQL
  - Process results
  - Close resources
- Limitations:
  - Manual SQL handling
  - Boilerplate code
  - Tight coupling
  - No ORM
  - No entity state tracking

## 2. Hibernate
- ORM framework implementing JPA and adding extra features.
- Replaces direct SQL with Java objects.
- Features:
  - Entity mapping
  - HQL/Criteria
  - Caching
  - Lazy loading
  - Transaction handling
- Internally uses JDBC for DB communication.
- Benefits over JDBC:
  - Less boilerplate
  - Automatic mapping
  - Database independence
  - Relationship handling

## 3. JPA (Java Persistence API)
- Specification (not an implementation).
- Standard for ORM in Java.
- Abstracts persistence logic.
- Uses providers like Hibernate/EclipseLink.
- Key Components:
  - Entity
  - EntityManager-Persistance Context
  - EntityManagerFactory- Persistence Unit
  - EntityTransaction
  - Persistence Unit
  - Transactions
  - JPQL
- Benefits over Hibernate:
  - Vendor independence
  - Cleaner abstraction
  - Portability

## 4. JDBC vs Hibernate vs JPA
- JDBC: Low-level API for direct SQL.
- Hibernate: ORM framework, uses JDBC.
- JPA: Specification, Hibernate implements it.
- Hierarchy:
  JDBC -> Hibernate (implements) -> JPA (standard API)

## 5. JPA Deep Dive Structure
Modules:
1. Introduction & Setup
2. Entities
3. Relationships
4. EntityManager Usage
5. JPQL & Queries
6. Advanced Mapping
7. Persistence Context
8. Exception Handling
9. Spring Integration

## 6. Persistence Unit
- Logical configuration for JPA.
- Defines entities and connection settings.
- Standard JPA: persistence.xml
- Spring Boot: auto-configured via properties.

Example (standard JPA):
<persistence-unit name="unitName">
  - Classes
  - DB properties
  - Provider
</persistence-unit>

## 7. Persistence in Spring Boot
- No need for persistence.xml normally.
- Configure via application.properties.
- Spring auto-creates EntityManagerFactory.

## 8. Manual Multi-DB & EntityManagerFactory Setup

### DataSources (application.properties)
spring.datasource.dbA...
spring.datasource.dbB...

### Beans
- DataSource beans with @ConfigurationProperties
- LocalContainerEntityManagerFactoryBean with:
  - dataSource
  - packages to scan
  - persistenceUnit name
- TransactionManager per factory

### Repository Wiring
@EnableJpaRepositories with:
- basePackages
- entityManagerFactoryRef
- transactionManagerRef

### EntityManager Injection
@PersistenceContext(unitName="unit")

## 9. Flow (Manual Config)
DataSource -> EntityManagerFactory -> TransactionManager -> EntityManager -> Entities/Repositories

## 10. JPA Annotations
- Mapping classes to tables: @Entity, @Table
- Mapping fields to columns: @Id, @GeneratedValue, @Column
- Relationship mappings: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany
- Lifecycle annotations: @PrePersist, @PostLoad, etc.
- embeddable objects: @Embeddable, @Embedded
- Inheritance strategies: @Inheritance, @DiscriminatorColumn, etc.

## JPA Query Language (JPQL)
- Structure of JPQL vs SQL.
- Writing queries using EntityManager.
- Using NamedQuery, NamedNativeQuery.
- Criteria API for dynamic queries.

## Transactions
- Understanding EntityTransaction.
- How JPA manages commit/rollback. 
- Propagation and isolation concepts

## Exception Handling
- JPA exception hierarchy
- How runtime exceptions are propagated

## Advanced Topics
- Inheritance mapping strategies.
- Optimistic and pessimistic locking.
- using Converters with @Convert.