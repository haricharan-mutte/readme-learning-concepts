# Transaction Management in Spring Boot JPA
 Transaction management is a critical aspect of enterprise applications that ensures data integrity and consistency. Spring Boot provides robust support for managing transactions through both declarative and programmatic approaches. Let's explore the different implementations, ACID properties, and best practices with real-world examples.
## Overview

Transaction management ensures 
- atomicity, 
- consistency, 
- isolation, and 
- durability (ACID) of database operations. 
- Spring Boot provides declarative and programmatic transaction management,
- In Spring Boot, transaction management can be implemented 
- declaratively using the `@Transactional` annotation or 
- programmatically using the `TransactionTemplate/PlatformTranasactionManager` Bean.

---

## ACID Properties

- **Atomicity:** All operations succeed or none do.(if any runtime exceptions auto rolledback)
- **Consistency:** Database remains valid before and after transaction.(DB constraints ensure data integrity)
- **Isolation:** Transactions are isolated to prevent interference.(one transaction do not read/interfer another transaction results untill it is comitted)
- **Durability:** Committed changes are permanent.

---

## Implementations

### Declarative (Recommended)

1. Declarative (Recommended)
   Use @Transactional annotation at method or class level in service layer.

```
@Service
public class BankService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Transactional
    public void transferMoney(Long fromAccountId, Long toAccountId, Double amount) {
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow();
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow();
        
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // If any exception occurs here, entire transaction rolls back
    }
}


```
2. Programmatic
Use TransactionTemplate or PlatformTransactionManager for fine control.

```
Configuration for @Transactional

In Spring Boot, transaction management is auto-configured when you include spring-boot-starter-data-jpa. 
For non-Spring Boot applications

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}

```

## Programmatic Transaction Management Using TransactionTemplate

 For scenarios requiring fine-grained control over transactions

```
@Service
public class UserService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    public Long registerUser(User user) {
        return transactionTemplate.execute(status -> {
            try {
                // Business logic
                User savedUser = userRepository.save(user);
                
                // Additional operations
                processUserRegistration(savedUser);
                
                return savedUser.getId();
            } catch (Exception e) {
                // Manually set rollback
                status.setRollbackOnly();
                throw new RuntimeException("Registration failed", e);
            }
        });
    }
}


```
4. complex trnasaction method example

```
@Transactional(
propagation = Propagation.REQUIRED,
isolation = Isolation.READ_COMMITTED,
timeout = 30,
readOnly = false,
rollbackFor = {BusinessException.class, SystemException.class},
noRollbackFor = {ValidationException.class}
)
public void complexTransactionalMethod() {
// Method with specific transaction requirements
}

```

# Transaction Propagation Types and Usage


---

## Transaction Propagation Types

| Propagation   | Description                                          | Usage Example                      |
|--------------|----------------------------------------------------|----------------------------------|
| REQUIRED     | Join or create transaction (default)               | Standard business methods        |
| REQUIRES_NEW | Suspend current and create new transaction         | Audit logging, notifications     |
| NESTED       | Savepoint within parent transaction                 | Batch processing with partial rollback |
| SUPPORTS     | Join if exists, else no transaction                 | Flexible read-only services      |
| NOT_SUPPORTED| Suspend current transaction, run non-transactional | External API calls, file processing |
| NEVER        | Throw exception if transaction exists               | System utilities                 |
| MANDATORY    | Must have existing transaction or throw exception  | Validation, security checks      |

---

## Exception Handling and Rollback

- Uncaught runtime exceptions trigger rollback.
- Caught exceptions do not rollback unless `setRollbackOnly()` is called.
- Checked exceptions do not trigger rollback unless specified.

Example:

```

@Transactional
public void handle() {
try {
riskyOperation();
} catch(Exception e) {
TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
}
}

```


---

## Isolation Levels

| Level            | Prevents                  | Use Case Example                 |
|------------------|---------------------------|---------------------------------|
| READ_UNCOMMITTED | None (allows dirty reads) | Analytics, logging systems       |
| READ_COMMITTED   | Dirty reads               | Most general apps (default)      |
| REPEATABLE_READ  | Dirty & non-repeatable reads | Financial apps                  |
| SERIALIZABLE     | Dirty/non-repeatable/phantom reads | Critical strict apps         |

---

## Important Phenomena

- **Dirty Read:** Reading uncommitted data.
- **Non-Repeatable Read:** Different values on repeated reads.
- **Phantom Read:** Different result sets on repeated queries.

---

## Spring Data JPA Default Behavior

- Repository read methods: `@Transactional(readOnly=true)` by default.
- Write methods: `@Transactional` by default.
- Use service layer transactions for method boundary control.

---

## Best Practices

- Use class-level `@Transactional` to avoid repetition.
- Use appropriate propagation for independent transactions.
- Choose isolation based on data consistency needs.
- Handle exceptions explicitly for rollback control.
- Avoid long transactions, especially during external calls.

---

## Understamding ISOLATION LEVELS WITH EXAMPLES

---

#### Understanding Dirty Read, Repeatable Read, and Phantom Read with Scenarios
1. **What is Dirty Read?**
A dirty read occurs when a transaction reads data that has been modified by another transaction but not yet committed. If the other transaction rolls back, the data read was never permanently saved, leading to possible inconsistencies.

2. **How Can Uncommitted Data be Viewed by Other Transactions?**
- When a transaction modifies data, it's in an uncommitted state.
- This data is stored in transactional memory (in-memory changes) or database locks/undo logs until commit or rollback.
- Certain isolation levels like READ_UNCOMMITTED allow other transactions to read these uncommitted changes, hence "dirty reads".
- Most databases implement locks or multiversion concurrency control (MVCC) to manage visibility of these uncommitted changes.

***Example***:
- Transaction A updates a row but does not commit yet.
- Transaction B reads the updated row (uncommitted).
- Transaction A rolls back.
- Transaction B has read an invalid or "dirty" value.

3. **What is Repeatable Read?**
A repeatable read anomaly happens when a transaction reads the same data twice and gets different values because another committed transaction changed the data between the two reads.

***Scenario:***
- Transaction A reads a row with value $100.
- Transaction B updates the value to $150 and commits.
- Transaction A reads the row again and gets $150 instead of $100.
- This happens under isolation levels lower than REPEATABLE_READ. The REPEATABLE_READ isolation level ensures that once a transaction reads data, no other transaction can modify it until this transaction completes to guarantee consistent repeated reads.

4. **What is Phantom Read?**
- A phantom read occurs when a transaction re-executes a query that returns a set of rows, and between the executions, new rows are inserted or deleted by another committed transaction, so the second query returns a different set of rows (phantoms).

****Scenario:****
- Transaction A runs SELECT * FROM orders WHERE customer_id = 1 and gets 5 rows.
- Transaction B inserts a new order for customer_id = 1 and commits.
- Transaction A re-runs the same query and now gets 6 rows including the "phantom" row inserted by Transaction B.
- Phantom reads are prevented only by the strictest isolation level SERIALIZABLE.

****Summary Table of Isolation Phenomena****

Phenomenon	Description	Example	Prevented By Dirty Read	Reading uncommitted data from another transaction	Reading data updated but rolled back later	READ_COMMITTED+
Repeatable Read	Reading the same row twice but getting different values	Reading a row with $100, updated to $150 later	REPEATABLE_READ+
Phantom Read	Re-executing query returns different rows (inserts/deletes by others)	Query returns extra rows on second execution	SERIALIZABLE

Where Uncommitted Data is Stored/Tracked?
- When a transaction modifies data, these changes are typically kept in an undo log or transactional buffer.
- The database keeps the original version unchanged for other transactions to read.
- Committing writes the changes permanently to disk; rolling back discards the uncommitted changes.
- MVCC-enabled databases (like PostgreSQL) store multiple data versions until a transaction commits or rolls back.

****Conclusion****
- Dirty reads happen because a transaction reads uncommitted data held in memory or transaction logs of another ongoing transaction.
- Repeatable reads ensure stable reads of the same rows by blocking concurrent updates.
- Phantom reads relate to changes in the number of rows returned between repeated queries due to inserts/deletes.
- Isolation levels and locking/MVCC techniques applied by DBMSs control these anomalies, balancing transaction accuracy and concurrency.
- These are crucial concepts for designing reliable transactional applications with data consistency guarantees under concurrent access.
