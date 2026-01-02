# Cassandra with Spring Boot: Integration Guide Summary

## What You Now Have

This comprehensive guide covers **three distinct approaches** to working with Apache Cassandra in Java/Spring Boot:

### 📄 Document 1: **cassandra_complete.md** - Full Architecture Guide
Complete guide covering:
- Cassandra architecture fundamentals
- Storage engine deep-dive (CommitLog, MemTable, SSTable)
- Data modeling best practices
- All three integration approaches with code examples
- Production best practices

**Best for:** Understanding the complete system end-to-end

---

## The Three Integration Approaches

### 1️⃣ Native DataStax Driver (Maximum Control & Performance)

**When to use:**
- Netflix-scale systems (5M+ writes/sec)
- Performance-critical paths
- Complex custom logic
- Need advanced driver features

**Maven dependency:**
```xml
<dependency>
  <groupId>com.datastax.oss</groupId>
  <artifactId>java-driver-core</artifactId>
  <version>4.17.0</version>
</dependency>
```

**Key concepts:**
- CqlSession: Main entry point
- Prepared Statements: For performance & security
- Query Builder: Type-safe query construction
- BoundStatement: Binding values to prepared statements

**Basic example:**
```java
@Configuration
public class CassandraConfig {
  @Bean
  public CqlSession cqlSession() {
    return CqlSession.builder()
        .addContactPoint(new InetSocketAddress("localhost", 9042))
        .withLocalDatacenter("datacenter1")
        .withKeyspace("demo")
        .build();
  }
}

@Service
public class UserServiceNative {
  @Autowired private CqlSession session;
  
  private PreparedStatement insertStmt;
  
  @PostConstruct
  void init() {
    insertStmt = session.prepare(
        "INSERT INTO users (user_id, name, city) VALUES (?, ?, ?)"
    );
  }
  
  public void createUser(UUID userId, String name, String city) {
    BoundStatement bound = insertStmt.bind(userId, name, city);
    session.execute(bound);
  }
}
```

**Pros:**
- Maximum performance
- Full driver control
- No Spring Data dependency overhead
- Direct access to advanced features

**Cons:**
- More boilerplate code
- Manual session management
- Steeper learning curve
- Requires prepared statement caching

---

### 2️⃣ CassandraTemplate (Balanced Approach)

**When to use:**
- Analytics and complex queries
- Balanced performance/development speed
- Need more control than Spring Data but less than native driver
- Multiple query patterns

**Maven dependency:**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-cassandra</artifactId>
</dependency>
```

**Configuration:**
```yaml
spring:
  data:
    cassandra:
      contact-points: localhost
      port: 9042
      keyspace-name: demo
      local-datacenter: datacenter1
```

**Key concepts:**
- CassandraTemplate: Spring's template pattern implementation
- Query/Criteria: Fluent DSL for building queries
- Exception Translation: Converts driver exceptions to Spring exceptions
- ReactiveCassandraTemplate: Non-blocking alternative

**Basic example:**
```java
@Table("users")
public class User {
  @PrimaryKey private UUID userId;
  private String name;
  private String city;
  // Getters, setters
}

@Service
public class UserServiceTemplate {
  @Autowired
  private CassandraTemplate cassandraTemplate;
  
  public void createUser(User user) {
    cassandraTemplate.insert(user);
  }
  
  public User getUserById(UUID userId) {
    return cassandraTemplate.selectOneById(userId, User.class);
  }
  
  public List<User> getUsersByCity(String city) {
    return cassandraTemplate.select(
        query(where("city").is(city)).withAllowFiltering(),
        User.class
    );
  }
}
```

**Pros:**
- Reduces boilerplate significantly
- Exception translation
- Familiar Spring patterns
- Good documentation
- Auto-configuration

**Cons:**
- Slight performance overhead
- Limited compared to native driver
- Spring Data version dependency

---

### 3️⃣ Spring Data Repository (Rapid Development)

**When to use:**
- Standard CRUD operations
- Rapid development/prototyping
- Multiple query patterns (via denormalization)
- Most microservices

**Maven dependency:**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-cassandra</artifactId>
</dependency>
```

**Key concepts:**
- CassandraRepository: Base repository interface
- Query Derivation: Method names generate queries
- Automatic CRUD: save, delete, update generated
- ReactiveCassandraRepository: Non-blocking alternative

**Basic example:**
```java
@Repository
public interface UserRepository extends CassandraRepository<User, UUID> {
  
  // Query derivation from method names
  List<User> findByCity(String city);
  List<User> findByCityAndName(String city, String name);
  
  // Custom CQL
  @Query("SELECT * FROM users WHERE city = ?0")
  List<User> getUsersByCity(String city);
}

@Service
public class UserServiceRepository {
  @Autowired private UserRepository userRepository;
  
  public User createUser(User user) {
    return userRepository.save(user);
  }
  
  public User getUserById(UUID userId) {
    return userRepository.findById(userId).orElse(null);
  }
  
  public List<User> getUsersByCity(String city) {
    return userRepository.findByCity(city);
  }
}
```

**Pros:**
- Least boilerplate code
- Automatic CRUD methods
- Query derivation from method names
- Rapid development
- Clean separation of concerns

**Cons:**
- Least control over execution
- Query derivation has limits
- Can't do very complex queries easily
- Performance overhead

---

## Quick Comparison

| Feature | Native Driver | CassandraTemplate | Spring Data |
|---------|---|---|---|
| **Performance** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Control** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐ |
| **Development Speed** | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Boilerplate** | High | Low | Minimal |
| **Learning Curve** | Steep | Moderate | Gentle |
| **Production Ready** | ✅ | ✅ | ✅ |
| **Best For** | High-performance systems | Balanced approach | Standard CRUD |

---

## Decision Tree

```
Question 1: Is this a new project?
├─ NO → Use existing approach's pattern
│
└─ YES → Question 2: What's your priority?
   ├─ Performance (Netflix-scale) → Native Driver
   ├─ Development Speed → Spring Data Repository
   └─ Balanced → CassandraTemplate
```

---

## Real-World Examples

### Example 1: E-Commerce Microservice
```
Requirements: Standard CRUD + some filtering
Decision: Spring Data Repository
Reasoning: Rapid development, standard operations, team productivity

@Repository
interface OrderRepository extends CassandraRepository<Order, UUID> {
  List<Order> findByUserId(UUID userId);
  List<Order> findByUserIdOrderByOrderDateDesc(UUID userId);
}
```

### Example 2: Streaming Event Analytics
```
Requirements: 5M+ writes/sec, low latency reads
Decision: Native Driver + Query Builder
Reasoning: Performance is critical, need full control

// Prepared statements + batching
PreparedStatement insertEvent = session.prepare(...);
BoundStatement bound = insertEvent.bind(...);
session.execute(bound);
```

### Example 3: Analytics Dashboard
```
Requirements: Complex queries, real-time aggregation
Decision: CassandraTemplate
Reasoning: Balance of control and convenience

// Query/Criteria builder for complex queries
cassandraTemplate.select(
    query(where("date").gte(startDate)
        .and(where("revenue").gte(minRevenue))),
    UserAnalytics.class
);
```

---

## Data Modeling Key Principles

### 1. Query-Driven Design
Design tables based on QUERIES, not entities.

**Bad:** One users table
**Good:** Multiple tables (users_by_id, users_by_email, users_by_city)

### 2. Partition Keys
- Distribute data evenly across cluster
- Must be specified in WHERE clause
- Avoid timestamp, country, status as keys

### 3. Composite Partition Keys (Time-Series)
```cql
PRIMARY KEY ((user_id, day, bucket), event_time)
```
Splits data per day, then per bucket → prevents hot partitions

### 4. TTL Strategy
```cql
-- Good: Partition-level TTL
PRIMARY KEY ((user_id, day), timestamp)
USING TTL 604800;

-- Bad: Row-level TTL (creates tombstones)
USING TTL 86400;
```

### 5. Compaction Strategy
- **TWCS**: Time-series, logs, events (RECOMMENDED FOR KAFKA→CASSANDRA)
- **LCS**: Read-heavy workloads
- **STCS**: Default, general purpose

---

## Common Gotchas & Solutions

### Gotcha 1: ALLOW FILTERING
```java
// Bad: This scans all partitions!
userRepository.findByCity("Bangalore");  // SLOW!

// Better: Denormalize - have users_by_city table
// Or use Spring Data @Query with ALLOW FILTERING only when needed
```

### Gotcha 2: Large Partitions
```
Symptom: Slow reads, compaction storms
Cause: All data for one key in single partition
Solution: Time-bucket - PRIMARY KEY ((user_id, day), timestamp)
```

### Gotcha 3: Row-Level TTL Explosion
```
Symptom: Read slowness, high CPU
Cause: Millions of individual row TTLs → tombstones
Solution: Use partition-level TTL via time-bucketing
```

### Gotcha 4: Mutation in Loop
```java
// Bad (Native Driver)
for (User user : users) {
  template.insert(user, InsertOptions.builder().ttl(3600).build());
}

// Better: Batch
template.batchOps().insert(users).execute();
```

---

## Integration with Kafka

Typical architecture:

```
Kafka Topic
    ↓
Spring Boot Consumer
    ↓
Deserialize Event
    ↓
Write to Multiple Cassandra Tables
(denormalized for different query patterns)
    ↓
Ack Kafka Offset
```

**Implementation approach:**

```java
@Service
@EnableKafka
public class EventProcessor {
  
  @Autowired
  private OrderRepository orderRepository;
  
  @Autowired
  private OrdersByCustomerRepository ordersByCustomerRepository;
  
  @KafkaListener(topics = "order-events")
  public void processOrderEvent(OrderEvent event) {
    Order order = event.getOrder();
    
    // Write to multiple tables
    orderRepository.save(order);
    ordersByCustomerRepository.save(
        new OrderByCustomer(order.getCustomerId(), order.getOrderId(), ...)
    );
  }
}
```

---

## Performance Tuning Checklist

### Schema Level
- [ ] Partition key distributes evenly
- [ ] Partition size bounded (<100MB)
- [ ] Correct compaction strategy (TWCS for time-series)
- [ ] TTL at partition level (time-bucketing)
- [ ] Clustering columns for range queries

### Write Path
- [ ] Use prepared statements (native driver)
- [ ] Batch operations where possible
- [ ] Async/reactive where applicable
- [ ] Idempotent writes for safety

### Read Path
- [ ] Always specify partition key
- [ ] Use clustering key for ranges
- [ ] Avoid ALLOW FILTERING
- [ ] Cache hot keys (row cache)
- [ ] Use correct consistency level

### Operations
- [ ] Run incremental repair daily
- [ ] Monitor compaction stats
- [ ] Check disk usage
- [ ] Monitor GC logs
- [ ] Track tombstone percentage

---

## Migration Path Between Approaches

### Spring Data → CassandraTemplate
```java
// Before
List<User> users = userRepository.findByCity(city);

// After
List<User> users = cassandraTemplate.select(
    query(where("city").is(city)),
    User.class
);
```

### CassandraTemplate → Native Driver
```java
// Before
cassandraTemplate.insert(user);

// After
BoundStatement bound = insertStmt.bind(
    user.getUserId(), user.getName(), user.getCity()
);
session.execute(bound);
```

---

## Production Deployment

### For Spring Data Repository
- [ ] All repositories tested
- [ ] Query derivation limits understood
- [ ] Pagination implemented
- [ ] Load tested
- [ ] Monitoring configured
- [ ] Alerts set up

### For CassandraTemplate
- [ ] Template bean scoped correctly
- [ ] Exception handling in place
- [ ] Query performance verified
- [ ] TTL strategy confirmed
- [ ] Replication tested
- [ ] Failover tested

### For Native Driver
- [ ] Connection pooling configured
- [ ] Prepared statements cached
- [ ] Batch operations tested
- [ ] Load testing completed (target throughput)
- [ ] Failover & recovery tested
- [ ] Monitoring & metrics in place

---

## Recommended Learning Path

1. **Start:** Spring Data Repository (rapid development)
2. **Intermediate:** CassandraTemplate (for complex queries)
3. **Advanced:** Native Driver (for performance tuning)

All are production-ready. Choose based on your specific needs.

---

## Resources

- **Official DataStax Driver:** https://docs.datastax.com/en/developer/java-driver/
- **Spring Data Cassandra:** https://spring.io/projects/spring-data-cassandra
- **Cassandra Official:** https://cassandra.apache.org/

---

## Key Takeaways

1. **All three approaches are production-ready**
2. **Start with Spring Data for rapid development**
3. **Move to CassandraTemplate when complexity increases**
4. **Use Native Driver only when performance requires it**
5. **Design tables based on QUERIES, not entities**
6. **Use time-bucketing for time-series data**
7. **Batch writes, cache reads, run repair regularly**
8. **Monitor compaction, tombstones, and disk usage**

---

**Version:** 1.0  
**Date:** January 2026  
**Status:** Production-Ready Guide
