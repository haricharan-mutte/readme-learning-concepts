# Apache Cassandra – Complete Architecture & Integration Guide
## Production-Ready: From Theory to Spring Boot Implementation

---

## Table of Contents
1. [Cassandra Fundamentals](#section1)
2. [Storage Engine & Performance](#section2)
3. [Data Modeling](#section3)
4. [Advanced Topics](#section4)
5. [CQL Operations](#section5)
6. [Three Integration Approaches](#section6)
7. [Production Best Practices](#section7)

---

## 1. Cassandra Architecture Fundamentals {#section1}

### What is Cassandra?

Apache Cassandra is a masterless, distributed, linearly scalable, highly available, and eventually consistent NoSQL database. Built for:

- **Massive write throughput** (millions of writes/second)
- **Zero single point of failure** (no master node)
- **Geo-distributed systems** (multi-datacenter replication)
- **Event-driven architectures** (Kafka, microservices, logs, metrics)

### Core Layers (Each Node)

```
Client
  ↓
Coordinator Node (Request Router)
  ↓
Partitioner (Token Ring Hash)
  ↓
MemTable (RAM - Sorted)
  ↓
CommitLog (Disk - Durability)
  ↓
SSTable (Disk - Permanent)
  ↓
Compaction (Storage Cleanup)
```

### Coordinator Node

Every node is equal—no master. When a client sends a query:

- Receiving node becomes Coordinator
- Determines which nodes own the data
- Routes requests to replica nodes
- Collects and merges responses
- Returns final result

### Token Ring & Consistent Hashing

```
Hash(partition_key) → Token → Ring Position → Owning Node
```

**Benefits:**
- Uniform data distribution
- Linear scalability
- No rewriting on node addition
- Automatic load balancing

### Replication Strategy

| Strategy | Use Case |
|----------|----------|
| SimpleStrategy | Development (single DC) |
| NetworkTopologyStrategy | Production (multi-DC) |

---

## 2. Storage Engine & Performance {#section2}

### Why 3-Layer Storage?

Cassandra must achieve:
- 1M+ writes/second
- Zero data loss
- No locking
- No random disk I/O
- Linear scalability

**Solution:** Log-Structured Merge Tree (LSM Tree)

### The 3-Layer Write Engine

#### Layer 1: Commit Log (Durability)

```
Write arrives → Append to CommitLog (disk)
             → Durability guaranteed
             → Sequential write (FAST)
```

| Property | Value |
|----------|-------|
| Purpose | Crash recovery insurance |
| Storage | Disk |
| Speed | Sequential append |
| Deleted | After SSTable flush |

#### Layer 2: MemTable (Speed)

```
CommitLog written → Write to MemTable (RAM)
                  → Sorted structure
                  → Supports reads
```

| Property | Value |
|----------|-------|
| Purpose | Hot data for reads/writes |
| Storage | RAM (in-memory) |
| Speed | Extremely fast |
| Lifetime | Until full (seconds) |

#### Layer 3: SSTable (Permanent)

```
MemTable full → Flush to SSTable (disk)
              → Immutable file
              → Never modified
```

| Property | Value |
|----------|-------|
| Purpose | Actual database |
| Storage | Disk |
| Nature | Immutable |
| Cleanup | Via Compaction |

### Full Write Path

```
INSERT (user_id=108, name="Hari")
         ↓
1. CommitLog ← durability
         ↓
2. MemTable ← speed
         ↓
3. ACK sent to client
         ↓
4. Later: SSTable ← permanent
         ↓
5. CommitLog deleted ← cleanup
```

### Read Path: Smart Optimization

```
SELECT WHERE user_id = 108
         ↓
1. Check MemTable (likely)
         ↓
2. Check Bloom Filters (skip SSTables)
         ↓
3. Use Partition Index (disk offset)
         ↓
4. Read SSTables
         ↓
5. Merge versions
         ↓
6. Return result
```

### Read Acceleration

| Technique | Purpose | Impact |
|-----------|---------|--------|
| Bloom Filters | Skip irrelevant SSTables | 90% I/O reduction |
| Partition Index | Jump to data offset | O(1) lookup |
| Row Cache | Hot partition cache | Hot key speedup |
| Compaction | Reduce SSTable count | Fewer disk reads |

---

## 3. Data Modeling {#section3}

### Golden Rule

**Design tables based on QUERIES, not ENTITIES**

| RDBMS | Cassandra |
|-------|-----------|
| One table per entity | One table per query |
| Normalized | Denormalized |
| Joins allowed | Denormalization instead |

### Partition Key (Critical)

Must:
1. Distribute evenly (avoid hotspots)
2. Match query patterns
3. Bound partition size (<100MB typically)

**Bad keys:** timestamp, country, status, boolean

### Clustering Columns

Sort rows WITHIN a partition:

```cql
PRIMARY KEY (user_id, order_time)
```

- user_id = Partition Key (which node)
- order_time = Clustering Column (sort order)

### Composite Keys: Avoiding Hot Partitions

**Problem:** One user generates 1M events/day → huge partition → slow reads

**Solution:** Time-bucketing

```cql
PRIMARY KEY ((user_id, day), event_time)
```

Benefits:
- Split data per day
- Smaller manageable partitions
- Old data can be TTL'd cleanly

### Denormalization: Supporting Multiple Queries

| Query Pattern | Table Name | Primary Key |
|---|---|---|
| Orders by user | orders_by_user | (user_id, order_time) |
| Orders by city | orders_by_city | (city, order_time) |
| Orders by product | orders_by_product | (product_id, order_time) |

**Application logic:**
```
New order arrives
  → Write to orders_by_user
  → Write to orders_by_city
  → Write to orders_by_product
  → Acknowledge after all succeed
```

---

## 4. Advanced Topics {#section4}

### Compaction Strategies

| Strategy | Use Case | Characteristic |
|----------|----------|---|
| SizeTieredCompaction (STCS) | Default, mixed workloads | Good throughput |
| LeveledCompaction (LCS) | Read-heavy | Fast reads |
| TimeWindowCompaction (TWCS) | Logs, metrics, events | Perfect for time-series |

### TWCS Configuration (Recommended for Kafka→Cassandra)

```cql
WITH compaction = {
  'class': 'TimeWindowCompactionStrategy',
  'compaction_window_size': '1',
  'compaction_window_unit': 'DAYS'
};
```

### TTL & Tombstones

**TTL** = Automatic deletion after time

```cql
INSERT INTO logs (...) USING TTL 86400;
```

**Danger:** Row-level TTL → millions of tombstones → slow reads

**Safe Pattern:** Partition-level TTL via time-bucketing

```cql
PRIMARY KEY ((user_id, day), event_time)
USING TTL 604800;  -- Whole partition expires
```

### Repair & Consistency

**Repair** = Sync replicas via Merkle Trees

**Rule:** Run repair at least once per 10 days

Without repair:
- Missed writes become permanent
- Tombstones remain forever
- Silent data loss

---

## 5. CQL Operations {#section5}

### Keyspace Creation

```cql
CREATE KEYSPACE demo
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

USE demo;
```

### Table Creation

```cql
-- Simple
CREATE TABLE users (
  user_id uuid PRIMARY KEY,
  name text,
  city text
);

-- With clustering
CREATE TABLE orders_by_user (
  user_id uuid,
  order_time timestamp,
  amount double,
  PRIMARY KEY (user_id, order_time)
) WITH CLUSTERING ORDER BY (order_time DESC);

-- Composite partition key (time-series)
CREATE TABLE user_events (
  user_id uuid,
  day date,
  bucket int,
  event_time timestamp,
  payload text,
  PRIMARY KEY ((user_id, day, bucket), event_time)
) WITH CLUSTERING ORDER BY (event_time DESC)
AND compaction = {
  'class': 'TimeWindowCompactionStrategy',
  'compaction_window_size': '1',
  'compaction_window_unit': 'DAYS'
};
```

### CRUD Operations

```cql
-- INSERT
INSERT INTO users (user_id, name, city)
VALUES (uuid(), 'Hari', 'Bangalore');

-- INSERT with TTL
INSERT INTO logs (log_id, message)
VALUES (uuid(), 'Event') USING TTL 86400;

-- SELECT (MUST specify partition key)
SELECT * FROM users WHERE user_id = ?;

-- UPDATE
UPDATE users SET city = 'Pune'
WHERE user_id = ?;

-- DELETE
DELETE FROM users WHERE user_id = ?;
```

---

## 6. Three Spring Boot Integration Approaches {#section6}

### Comparison Matrix

| Aspect | Native Driver | CassandraTemplate | Spring Data |
|--------|---------------|-------------------|------------|
| **Control** | Maximum | Good | Limited |
| **Boilerplate** | Significant | Minimal | Minimal |
| **Learning curve** | Steep | Moderate | Gentle |
| **Performance** | Excellent | Excellent | Good |
| **Best for** | High-performance, custom logic | Balanced | Standard CRUD |

---

### Option 1: Native DataStax Driver (Maximum Control)

#### Dependencies

```xml
<!-- Core driver -->
<dependency>
  <groupId>com.datastax.oss</groupId>
  <artifactId>java-driver-core</artifactId>
  <version>4.17.0</version>
</dependency>

<!-- Query builder (type-safe) -->
<dependency>
  <groupId>com.datastax.oss</groupId>
  <artifactId>java-driver-query-builder</artifactId>
  <version>4.17.0</version>
</dependency>
```

#### Configuration

```java
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetSocketAddress;

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
```

#### Prepared Statements (BEST PRACTICE)

```java
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

@Service
public class UserServiceNative {
  
  @Autowired
  private CqlSession session;
  
  private PreparedStatement insertStmt;
  private PreparedStatement selectStmt;
  
  @PostConstruct
  public void init() {
    // Prepare once (reused many times)
    insertStmt = session.prepare(
        "INSERT INTO users (user_id, name, city) VALUES (?, ?, ?)"
    );
    selectStmt = session.prepare(
        "SELECT * FROM users WHERE user_id = ?"
    );
  }
  
  // INSERT
  public void createUser(UUID userId, String name, String city) {
    BoundStatement bound = insertStmt.bind(userId, name, city);
    session.execute(bound);
  }
  
  // SELECT
  public User getUserById(UUID userId) {
    BoundStatement bound = selectStmt.bind(userId);
    ResultSet resultSet = session.execute(bound);
    Row row = resultSet.one();
    
    if (row != null) {
      return new User(
          row.getUuid("user_id"),
          row.getString("name"),
          row.getString("city")
      );
    }
    return null;
  }
}
```

#### Query Builder (Type-Safe)

```java
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;

@Service
public class UserServiceQueryBuilder {
  
  @Autowired
  private CqlSession session;
  
  // Type-safe, compile-checked
  public User getUserById(UUID userId) {
    var select = selectFrom("users")
        .all()
        .whereColumn("user_id").isEqualTo(literal(userId));
    
    ResultSet resultSet = session.execute(select.build());
    Row row = resultSet.one();
    
    return row != null ? mapRowToUser(row) : null;
  }
  
  // UPDATE
  public void updateCity(UUID userId, String newCity) {
    var update = update("users")
        .setColumn("city", literal(newCity))
        .whereColumn("user_id").isEqualTo(literal(userId));
    
    session.execute(update.build());
  }
  
  // DELETE
  public void deleteUser(UUID userId) {
    var delete = deleteFrom("users")
        .whereColumn("user_id").isEqualTo(literal(userId));
    
    session.execute(delete.build());
  }
  
  private User mapRowToUser(Row row) {
    return new User(
        row.getUuid("user_id"),
        row.getString("name"),
        row.getString("city")
    );
  }
}
```

#### Batch Operations

```java
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;

public void batchInsertUsers(List<User> users) {
  BatchStatement batch = BatchStatement.builder(BatchType.UNLOGGED)
      .addStatement(insertStmt.bind(users.get(0).getId(), ...))
      .addStatement(insertStmt.bind(users.get(1).getId(), ...))
      .addStatement(insertStmt.bind(users.get(2).getId(), ...))
      .build();
  
  session.execute(batch);
}
```

#### Async/Reactive

```java
// Async with CompletableFuture
public CompletableFuture<User> getUserAsync(UUID userId) {
  BoundStatement bound = selectStmt.bind(userId);
  
  return session.executeAsync(bound)
      .toCompletableFuture()
      .thenApply(resultSet -> {
        Row row = resultSet.one();
        return row != null ? mapRowToUser(row) : null;
      });
}

// Reactive with Project Reactor
public Mono<User> getUserReactive(UUID userId) {
  return Mono.fromCompletionStage(() -> {
    BoundStatement bound = selectStmt.bind(userId);
    return session.executeAsync(bound).toCompletableFuture();
  }).flatMap(resultSet -> {
    Row row = resultSet.one();
    return row != null ? Mono.just(mapRowToUser(row)) : Mono.empty();
  });
}
```

**Pros:** Maximum performance, full control, advanced features
**Cons:** More boilerplate, manual session management

---

### Option 2: CassandraTemplate (Balanced)

#### Dependencies

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-cassandra</artifactId>
</dependency>
```

#### Configuration

```yaml
spring:
  data:
    cassandra:
      contact-points: localhost
      port: 9042
      keyspace-name: demo
      local-datacenter: datacenter1
      schema-action: create_if_not_exists
```

#### Entity Definition

```java
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyType;

@Table("users")
public class User {
  
  @PrimaryKey
  private UUID userId;
  
  private String name;
  private String city;
  
  // Constructors, getters, setters
}

// Composite key example
@PrimaryKeyClass
public class EventKey {
  
  @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
  private UUID userId;
  
  @PrimaryKeyColumn(name = "day", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
  private LocalDate day;
  
  @PrimaryKeyColumn(name = "event_time", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
  private Instant eventTime;
  
  // Constructors, getters, setters
}

@Table("user_events")
public class Event {
  
  @PrimaryKey
  private EventKey eventKey;
  
  private String eventType;
  private String payload;
  
  // Constructors, getters, setters
}
```

#### CRUD with CassandraTemplate

```java
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Update;
import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Service
public class UserServiceTemplate {
  
  @Autowired
  private CassandraTemplate cassandraTemplate;
  
  // INSERT
  public void createUser(User user) {
    cassandraTemplate.insert(user);
  }
  
  // INSERT with TTL
  public void createUserWithTTL(User user, int ttlSeconds) {
    cassandraTemplate.insert(user, InsertOptions.builder()
        .ttl(ttlSeconds)
        .build());
  }
  
  // SELECT by ID
  public User getUserById(UUID userId) {
    return cassandraTemplate.selectOneById(userId, User.class);
  }
  
  // SELECT with query
  public List<User> getUsersByCity(String city) {
    return cassandraTemplate.select(
        query(where("city").is(city)).withAllowFiltering(),
        User.class
    );
  }
  
  // UPDATE
  public void updateCity(UUID userId, String newCity) {
    cassandraTemplate.update(
        query(where("user_id").is(userId)),
        Update.create().set("city", newCity),
        User.class
    );
  }
  
  // DELETE
  public void deleteUser(UUID userId) {
    cassandraTemplate.deleteById(userId, User.class);
  }
  
  // BATCH
  public void batchInsertUsers(List<User> users) {
    cassandraTemplate.batchOps()
        .insert(users)
        .execute();
  }
}
```

#### Fluent Template API

```java
// Cleaner fluent API
public List<User> getRecentUsers() {
  return cassandraTemplate.query(User.class)
      .matching(query(where("city").is("Bangalore")))
      .all();
}

// Stream for large datasets
public void processAllUsersInStream() {
  try (Stream<User> stream = cassandraTemplate.query(User.class)
      .all()
      .stream()) {
    stream.forEach(user -> System.out.println(user.getName()));
  }
}
```

#### Reactive CassandraTemplate

```java
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserServiceReactiveTemplate {
  
  @Autowired
  private ReactiveCassandraTemplate reactiveCassandraTemplate;
  
  public Mono<User> createUserAsync(User user) {
    return reactiveCassandraTemplate.insert(user);
  }
  
  public Mono<User> getUserByIdAsync(UUID userId) {
    return reactiveCassandraTemplate.selectOneById(userId, User.class);
  }
  
  public Flux<User> getUsersByCity(String city) {
    return reactiveCassandraTemplate.select(
        query(where("city").is(city)),
        User.class
    );
  }
}
```

**Pros:** Reduced boilerplate, exception translation, balanced abstraction
**Cons:** Slight overhead, Spring dependency

---

### Option 3: Spring Data Repository (Rapid Development)

#### Dependencies

Same as CassandraTemplate

#### Repository Interface

```java
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CassandraRepository<User, UUID> {
  
  // Query derivation
  List<User> findByCity(String city);
  
  List<User> findByCityAndName(String city, String name);
  
  Optional<User> findOneByEmail(String email);
  
  // Custom CQL
  @Query("SELECT * FROM users WHERE city = ?0")
  List<User> getUsersByCity(String city);
  
  // Count
  long countByCity(String city);
  
  // Delete
  void deleteByCity(String city);
}
```

#### Usage in Service

```java
@Service
public class UserServiceRepository {
  
  @Autowired
  private UserRepository userRepository;
  
  // CREATE
  public User createUser(User user) {
    return userRepository.save(user);
  }
  
  public List<User> createUsers(List<User> users) {
    return userRepository.saveAll(users);
  }
  
  // READ
  public User getUserById(UUID userId) {
    return userRepository.findById(userId).orElse(null);
  }
  
  public List<User> getUsersByCity(String city) {
    return userRepository.findByCity(city);
  }
  
  // UPDATE
  public User updateUser(User user) {
    return userRepository.save(user);  // Idempotent
  }
  
  // DELETE
  public void deleteUser(UUID userId) {
    userRepository.deleteById(userId);
  }
  
  public void deleteByCity(String city) {
    userRepository.deleteByCity(city);
  }
}
```

#### Reactive Repository

```java
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository 
    extends ReactiveCassandraRepository<User, UUID> {
  
  Flux<User> findByCity(String city);
  
  Mono<User> findOneByEmail(String email);
}

@Service
public class UserServiceReactiveRepository {
  
  @Autowired
  private ReactiveUserRepository userRepository;
  
  public Mono<User> getUserAsync(UUID userId) {
    return userRepository.findById(userId);
  }
  
  public Flux<User> getUsersByCity(String city) {
    return userRepository.findByCity(city);
  }
}
```

**Pros:** Least boilerplate, rapid CRUD development, Spring integration
**Cons:** Least control, query derivation limits

---

## 7. Production Best Practices {#section7}

### Table Design Checklist

- [ ] Partition key distributes evenly
- [ ] Partition key matches query patterns
- [ ] Partition size bounded (<100MB)
- [ ] One table per query pattern
- [ ] Time-series use time-bucketing
- [ ] Denormalization for multiple queries
- [ ] Correct compaction strategy selected

### Write Path Optimization

| Practice | Benefit |
|----------|---------|
| Use prepared statements | Network & server efficiency |
| Batch operations | Reduced round trips |
| Idempotent writes | Safe retries |
| Partition-level TTL | Efficient cleanup |
| Async/reactive | Non-blocking |

### Read Path Optimization

| Practice | Benefit |
|----------|---------|
| Always use partition key | Single node read |
| Use clustering keys for range | Pre-sorted results |
| Avoid ALLOW FILTERING | Prevents full scans |
| Caching for hot keys | Reduced latency |
| Correct compaction strategy | Fewer disk reads |

### Operational Excellence

| Task | Frequency | Importance |
|------|-----------|-----------|
| Run incremental repair | Daily | Critical |
| Monitor compaction | Hourly | High |
| Monitor tombstones | Daily | High |
| Check disk usage | Daily | High |
| Monitor GC | Weekly | Medium |
| Capacity planning | Monthly | Medium |

### When to Use Each Approach

| Scenario | Approach |
|----------|----------|
| Netflix-scale streaming | Native Driver + Query Builder |
| Microservice with CRUD | Spring Data Repository |
| Complex analytics | CassandraTemplate + native fallback |
| High-throughput writes | Native Driver + batching |
| Rapid prototyping | Spring Data Repository |
| Existing native driver code | Stay with native driver |
| New Spring Boot project | Start with Spring Data Repository |

---

## Summary: Three Approaches at a Glance

### Native Driver
- **When:** High performance, custom logic needed
- **Setup:** Manual CqlSession configuration
- **Code:** Prepared statements or Query Builder
- **Control:** Maximum
- **Learning:** Steep

### CassandraTemplate
- **When:** Balanced approach
- **Setup:** Auto-configured, minimal setup
- **Code:** Fluent template API, Query/Criteria builder
- **Control:** Good
- **Learning:** Moderate

### Spring Data Repository
- **When:** Standard CRUD, rapid development
- **Setup:** Auto-configured with repositories
- **Code:** Query derivation from method names
- **Control:** Limited
- **Learning:** Gentle

---

All three approaches are production-ready and performant. Choose based on your team's expertise, project complexity, and specific requirements.

**Version:** 1.0  
**Date:** January 2026  
**Audience:** Senior Java Engineers, Backend Architects  
**Knowledge Level:** Intermediate to Advanced
