# Apache Cassandra – Complete Architecture & Implementation Guide
## Deep Dive: From Theory to Production-Ready Spring Boot Integration

---

## Table of Contents
1. [Cassandra Architecture Fundamentals](#section1)
2. [Storage Engine & Write/Read Paths](#section2)
3. [Data Modeling & Query Patterns](#section3)
4. [Advanced Topics: Compaction, Repair & Self-Healing](#section4)
5. [Practical CQL & Database Operations](#section5)
6. [Spring Boot Integration & Configuration](#section6)
7. [Production Best Practices](#section7)

---

## 1. Cassandra Architecture Fundamentals {#section1}

### What is Cassandra?

Apache Cassandra is a masterless, distributed, linearly scalable, highly available, and eventually consistent NoSQL database. It is built for:

- **Massive write throughput** (millions of writes/second)
- **Zero single point of failure** (no master node)
- **Geo-distributed systems** (multi-datacenter replication)
- **Event-driven architectures** (Kafka, microservices, logs, metrics)

### Core Architectural Layers

Each Cassandra node contains these internal components:

```
Client
  ↓
Coordinator Node (Request Router)
  ↓
Partitioner (Token Ring Hash)
  ↓
MemTable (RAM - Sorted Structure)
  ↓
Commit Log (Disk - Durability)
  ↓
SSTable (Disk - Permanent Storage)
  ↓
Compaction (Storage Cleanup)
```

### Coordinator Node – Request Router

Every node in Cassandra is equal; there is no master. When a client sends a query:

- The receiving node becomes the **Coordinator**
- It determines which nodes own the data (based on partition key)
- It routes requests to replica nodes
- It collects and merges responses
- It returns the final result

**Key Principle**: No hierarchy, no single point of failure, automatic load balancing.

### Token Ring & Consistent Hashing

Cassandra distributes data using a consistent hash ring:

```
Hash(partition_key) → Token → Ring Position → Owning Node
```

**How it works:**
- Partition key is hashed using Murmur3
- Result is a token (0 to 2^64)
- Each token range is assigned to a node
- Provides uniform distribution and linear scalability

**Benefits:**
- No master node needed
- Adding nodes requires only local data rebalancing
- Automatic scaling without rewriting data

### Replication Strategy

Controls where copies of data live.

| Strategy | Use Case |
|----------|----------|
| SimpleStrategy | Single datacenter, development only |
| NetworkTopologyStrategy | Multi-datacenter, production geo-replication |

**Replication Factor (RF):** Defines number of copies. Common values: RF=3 for production.

---

## 2. Storage Engine & Write/Read Paths {#section2}

### Why 3-Layer Storage? The Fundamental Problem

Cassandra must solve an impossible requirement:
- 1 million writes/second
- Zero data loss
- No locking mechanisms
- No random disk I/O
- Linear scalability across unlimited nodes

**Solution:** Log-Structured Merge Tree (LSM Tree) architecture. This same core idea powers Kafka, RocksDB, HBase, and BigTable.

### The 3-Layer Write Engine Explained

#### Layer 1: Commit Log – Durability Guarantee

**Purpose:** "I will never lose your data."

When a write arrives:
1. Cassandra FIRST appends the write to the Commit Log (sequential disk write)
2. This is durability insurance—even if power fails, data is safe
3. Commit Log is NOT used for reads—only for crash recovery

**Why it's fast:**
- Sequential append-only writes
- No indexing or random seeking
- Extremely fast disk operation

#### Layer 2: MemTable – Speed Layer

After writing to Commit Log:
1. The same row is written to a **MemTable** (in-memory sorted structure)
2. Stored in RAM for extreme speed
3. Sorted by partition key
4. NOT durable (vulnerable to crashes)
5. Supports reads

**Key Point:** MemTable is an active write buffer, not just a cache. It's the hot path for recent data.

#### Layer 3: SSTable – Permanent Storage

When MemTable becomes full:
1. It is flushed to disk as an immutable file called **SSTable** (Sorted String Table)
2. SSTable is never modified after creation
3. Multiple SSTables accumulate over time
4. Later merged by Compaction process

**Properties:**
- Immutable (never changed)
- Sorted by partition key
- Stored on disk
- Multiple files exist simultaneously

### Why NOT Directly Write to SSTable?

| Direct SSTable Writes | Cassandra Design |
|----------------------|------------------|
| Random disk writes | Sequential writes only |
| Locking required | No locks needed |
| Slow | Extremely fast |
| Hard to scale | Linear scale achieved |

**The insight:** Sequential writes are orders of magnitude faster than random writes. Cassandra sacrifices complexity in reads to achieve simplicity and speed in writes.

### Full Write Path Example

```
User insert: (user_id=108, name="Hari", city="Bangalore")
         ↓
1. Append to CommitLog (disk)
   → Durability guaranteed
         ↓
2. Write to MemTable (RAM)
   → Sorted structure for fast reads
         ↓
3. Send ACK to client
   → User gets response immediately
         ↓
4. Later: MemTable full → Flush to SSTable (disk)
   → Permanent storage
         ↓
5. CommitLog segment deleted
   → Space reclaimed
```

### Lifecycle After Write

| Layer | Lifetime | Purpose |
|-------|----------|---------|
| CommitLog | Until SSTable is safely flushed | Crash recovery insurance |
| MemTable | Until full (typically seconds) | Fast reads & writes |
| SSTable | Permanent | Actual database |

**Key Rule:** CommitLog is cleaned automatically after MemTable is flushed. It is not a long-term data store.

### Read Path – Smart Optimization

Reads check multiple sources:

```
SELECT * FROM users WHERE user_id = 108
         ↓
1. Check MemTable (most likely)
   → Fast, in-memory
         ↓
2. Check Bloom Filters
   → Skip irrelevant SSTables
         ↓
3. Use Partition Index
   → Jump directly to disk offset
         ↓
4. Read required SSTables
   → Minimal disk I/O
         ↓
5. Merge versions
   → Latest wins
         ↓
6. Return result
```

### Read Acceleration Techniques

#### Bloom Filters – The Gatekeeper
- Probabilistic in-memory data structure attached to every SSTable
- Answers: "Is this partition key definitely NOT in this SSTable?"
- If Bloom says NO → Cassandra skips that SSTable entirely
- Never gives false negatives
- Reduces disk I/O by 90%+

#### Partition Index – Direct Disk Offset
- Inside each SSTable: stores exact byte offset of each partition
- Cassandra jumps directly to data—never scans files
- Provides O(1) lookup

#### Row Cache – Optional Hot Data Cache
| Good For | Not For |
|----------|---------|
| Hot keys | Large partitions |
| User profiles | Time-series data |
| Frequently accessed data | Write-heavy workloads |

#### Compaction – Read Optimizer
- More SSTables = slower reads
- Compaction merges SSTables to reduce file count
- Removes tombstones (deleted data markers)
- Improves data locality

---

## 3. Data Modeling & Query Patterns {#section3}

### The Golden Rule of Cassandra Data Modeling

**You NEVER design Cassandra tables based on entities. You design tables based on QUERIES.**

This is the opposite of relational database thinking.

| RDBMS Approach | Cassandra Approach |
|----------------|-------------------|
| users table (entity) | get_user_by_id table |
| orders table (entity) | get_orders_by_user table |
| One table per entity | One table per query |
| Normalized | Intentionally denormalized |

### Core Keys in Cassandra

| Key Type | Purpose |
|----------|---------|
| Partition Key | Determines which node owns the data |
| Clustering Columns | Determines sort order within a partition |
| Regular Columns | Actual data stored |

### Partition Key – Most Critical

The partition key **must**:
1. **Distribute evenly** → Avoid hotspot nodes
2. **Match your query patterns** → Avoid scans
3. **Bound partition size** → Keep partitions < 100MB typically

**Rule:** Bad partition key = cluster death.

### Clustering Columns – Sorted Storage

Clustering columns sort rows WITHIN a partition:

```cql
PRIMARY KEY (user_id, order_time)
```

Here:
- `user_id` = Partition Key (which node)
- `order_time` = Clustering Column (sort order inside partition)

**Result:** All orders for a user are stored together, sorted by time.

### Query-Driven Design Example

**Query:** "Get last 50 orders of a user"

```cql
CREATE TABLE orders_by_user (
  user_id uuid,
  order_time timestamp,
  order_id uuid,
  amount double,
  PRIMARY KEY (user_id, order_time)
) WITH CLUSTERING ORDER BY (order_time DESC);
```

**Why this works:**
- Partition key = user_id → Single node read
- Clustering key = order_time → Pre-sorted
- DESC order → Latest first
- No JOIN needed
- No aggregation needed
- Predictable latency

### Multiple Query Patterns – Multiple Tables

**Rule:** Different queries = different tables (denormalization).

**Example:**

Query Pattern | Table Name | Primary Key |
|---|---|---|
| Orders by user | orders_by_user | (user_id, order_time) |
| Orders by city | orders_by_city | (city, order_time, user_id) |
| Orders by product | orders_by_product | (product_id, order_time, user_id) |

**Implementation:** Application writes to all three tables in a single logical transaction.

### Composite Partition Keys – Avoiding Hot Partitions

**Problem:** One user generates 1M events/day → Single partition becomes huge → Slow reads, compaction storms.

**Solution:** Composite partition key

```cql
PRIMARY KEY ((user_id, day), event_time)
```

Benefits:
- User data split per day
- Each day has smaller, manageable partition
- Time-bound queries naturally
- Old data can be TTL'd cleanly

### Bucketing Strategy for Ultra-High Volume

For massive time-series (Kafka streams, metrics):

```cql
PRIMARY KEY ((user_id, day, bucket), event_time)
WHERE bucket = hash(event_time) % 10
```

Effect: Each day's data splits across 10 partitions → 10 nodes in parallel → Better parallelism & compaction.

### Anti-Patterns – Never Do These

| Anti-Pattern | Why It Fails |
|--------------|-------------|
| ALLOW FILTERING | Triggers full table scans |
| Large partitions (>100MB) | Slow reads, compaction nightmare |
| Secondary indexes at scale | Cross-node coordination |
| Joins | Not supported in Cassandra |
| Aggregations | Can't scan efficiently |

### Hot Partition – The Silent Killer

Hot partition = One partition receiving disproportionate load.

**Example of bad keys:**
- `timestamp` → Hotspot at current time
- `country` → Uneven distribution
- `status` → Few possible values
- `boolean` → Only 2 partitions possible

**Impact:** One node melts while others idle → cluster becomes unstable.

---

## 4. Advanced Topics: Compaction, Repair & Self-Healing {#section4}

### Compaction – The Storage Engine Brain

**What:** Compaction merges SSTables to clean, organize, and optimize storage.

**Why it's necessary:**
- SSTables never change (immutable)
- Multiple versions of same row exist over time
- Tombstones (deleted data markers) must be removed
- Too many files → reads must check multiple SSTables

**Without compaction:** Cassandra collapses under its own data.

### Compaction Strategies

| Strategy | Use Case | Characteristics |
|----------|----------|-----------------|
| SizeTieredCompaction (STCS) | Default, mixed workloads | Groups files by size, good throughput |
| LeveledCompaction (LCS) | Read-heavy workloads | Strict levels, very fast reads |
| TimeWindowCompaction (TWCS) | Logs, metrics, events, Kafka streams | Groups by time window, perfect for time-series |

#### Size-Tiered Compaction (STCS)
- Groups SSTables by size
- Merges similar-sized files together
- Good write throughput
- Read latency slightly higher
- **Best for:** Mixed workload systems

#### Leveled Compaction (LCS)
- Keeps SSTables in strict levels (L0, L1, L2, etc.)
- Only one SSTable per level contains a specific key
- Very fast reads (bounded file count)
- More compaction overhead
- **Best for:** User profiles, frequently accessed data

#### Time Window Compaction (TWCS) – Most Important for Event Systems
- Groups SSTables by time window (e.g., 1 day)
- Old data compacted only with old
- New data stays separate
- Tombstones cleaned fast
- Perfect for:
  - Kafka streams
  - Logs and metrics
  - Events and event sourcing
  - Time-series data

**TWCS Configuration Example:**
```cql
WITH compaction = {
  'class': 'TimeWindowCompactionStrategy',
  'compaction_window_size': '1',
  'compaction_window_unit': 'DAYS'
};
```

### TTL (Time To Live) & Tombstones – The Silent Killer

#### What is TTL?
TTL automatically deletes data after specified time.

```cql
INSERT INTO logs (...) USING TTL 86400;  -- Deletes after 24 hours
```

#### Why Tombstones are Dangerous
1. **Deletes don't remove data immediately** → They write a tombstone
2. **Tombstones are markers** → "This data is deleted as of time T"
3. **Reads must process tombstones** → Check every version
4. **Too many tombstones = slow reads, CPU spikes, GC storms**

#### The Tombstone Death Spiral
```
Many deletes/TTLs
    ↓
Tombstones explode
    ↓
Compaction becomes heavy
    ↓
Reads slow down
    ↓
Query timeouts
    ↓
Repair storms
    ↓
Node failures
    ↓
Cluster death
```

#### Safe TTL Pattern
**Rule:** Use TTL only on entire partitions via time-bucketing.

**BAD:**
```cql
INSERT INTO events (user_id, event_time, data)
VALUES (...) USING TTL 86400;
```
→ One row-level TTL → millions of tombstones

**GOOD:**
```cql
CREATE TABLE events (
  user_id uuid,
  day date,
  event_time timestamp,
  data text,
  PRIMARY KEY ((user_id, day), event_time)
) USING TTL 604800;  -- Partition-level TTL
```
→ Whole partition dies cleanly when expired

### Anti-Entropy & Repair – Making Replicas Consistent

#### Why Repair Exists

Cassandra is eventually consistent:
- Writes can succeed on some replicas
- Fail on others
- Nodes can be down
- Network partitions can occur

So replicas **drift** and Repair syncs them back.

#### How Repair Works

1. **Build Merkle Trees** for each partition on each replica
2. **Compare trees** between replicas
3. **If mismatch** → Transfer only missing/different ranges
4. **No full scans** → Efficient comparison

#### Critical Rule

**You MUST run Repair at least once per gc_grace_seconds (default 10 days).**

Without repair:
- Missed writes become permanent
- Tombstones remain forever (slow reads)
- TTL cleanup doesn't happen
- Disk bloats
- Hidden handoff overflows

#### Repair Types

| Type | When to Use |
|------|-------------|
| Full repair | Periodic maintenance (monthly) |
| Incremental repair | Daily automation |
| Subrange repair | Large cluster scaling |

**Production standard:** Incremental repair daily + full repair monthly.

### Hinted Handoff – Temporary Healing

**Scenario:** Node B is temporarily down.

```
Write arrives for partition owned by A, B, C
Coordinator:
  ✓ Writes to A and C
  ✓ Stores a Hint for B (on another node)
  
Later, when B comes back:
  → Cassandra replays the hint
  → B catches up automatically
```

**Effect:** Short outages don't cause data loss.

### Read Repair – Opportunistic Healing

During a read:
1. Cassandra fetches from multiple replicas
2. If they return different versions → Latest wins
3. Stale replicas are fixed in background

**Effect:** Reads slowly heal inconsistencies.

### Self-Healing Trio Working Together

| Mechanism | Purpose | Timeframe |
|-----------|---------|-----------|
| Hinted Handoff | Handle short node outages | Minutes to hours |
| Read Repair | Opportunistic healing | Real-time during reads |
| Full Repair | Guaranteed consistency | Days or weeks |

**Together:** Cassandra becomes self-healing, approaching correctness despite failures.

---

## 5. Practical CQL & Database Operations {#section5}

### Keyspace – The Logical Database

A Keyspace is the top-level namespace equivalent to a database in SQL.

**Defines:**
- Where data is stored
- How many copies (replication) exist
- How data is distributed across datacenters

### Creating a Keyspace

```cql
CREATE KEYSPACE demo
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};
```

**For production (multi-datacenter):**
```cql
CREATE KEYSPACE production
WITH replication = {
  'class': 'NetworkTopologyStrategy',
  'us_east': 3,
  'us_west': 3
};
```

### Switching to a Keyspace

```cql
USE demo;
```

### Creating Tables

**Simple table with single partition key:**
```cql
CREATE TABLE users (
  user_id uuid PRIMARY KEY,
  name text,
  city text
);
```

**Table with clustering columns:**
```cql
CREATE TABLE orders_by_user (
  user_id uuid,
  order_time timestamp,
  order_id uuid,
  amount double,
  PRIMARY KEY (user_id, order_time)
) WITH CLUSTERING ORDER BY (order_time DESC);
```

**Composite partition key (time-series):**
```cql
CREATE TABLE user_events (
  user_id uuid,
  day date,
  bucket int,
  event_time timestamp,
  event_type text,
  payload text,
  PRIMARY KEY ((user_id, day, bucket), event_time)
) WITH CLUSTERING ORDER BY (event_time DESC)
AND compaction = {
  'class': 'TimeWindowCompactionStrategy',
  'compaction_window_size': '1',
  'compaction_window_unit': 'DAYS'
};
```

### Core CQL Operations

#### Insert
```cql
INSERT INTO users (user_id, name, city)
VALUES (uuid(), 'Hari', 'Bangalore');
```

With TTL:
```cql
INSERT INTO logs (log_id, message)
VALUES (uuid(), 'Event') USING TTL 86400;
```

#### Read
```cql
-- Must specify partition key
SELECT * FROM users WHERE user_id = 550e8400-e29b-41d4-a716-446655440000;

-- With clustering column
SELECT * FROM orders_by_user 
WHERE user_id = 550e8400-e29b-41d4-a716-446655440000 
LIMIT 10;
```

#### Update
```cql
UPDATE users SET city = 'Pune' 
WHERE user_id = 550e8400-e29b-41d4-a716-446655440000;
```

#### Delete
```cql
DELETE FROM orders_by_user 
WHERE user_id = 550e8400-e29b-41d4-a716-446655440000 
AND order_time = 2024-01-15T10:30:00Z;
```

### Critical Query Rule

**You MUST always specify the FULL partition key in the WHERE clause.**

**These work:**
```cql
WHERE user_id = ?
WHERE user_id = ? AND order_time > ?
```

**These FAIL (no partition key):**
```cql
WHERE city = 'Bangalore'        -- ERROR: scans all partitions
WHERE amount > 1000             -- ERROR: scans all partitions
WHERE order_time > now()        -- ERROR: scans all partitions
```

### Denormalization in Practice

To support multiple query patterns:

**Query 1: Orders by user**
```cql
CREATE TABLE orders_by_user (
  user_id uuid,
  order_time timestamp,
  amount double,
  PRIMARY KEY (user_id, order_time)
);
```

**Query 2: Orders by city**
```cql
CREATE TABLE orders_by_city (
  city text,
  order_time timestamp,
  user_id uuid,
  amount double,
  PRIMARY KEY (city, order_time, user_id)
);
```

**Application logic:**
```
When new order arrives:
  → Insert into orders_by_user
  → Insert into orders_by_city
  → Acknowledge once both succeed (or retry)
```

This is exactly how Kafka consumers write to multiple Cassandra tables.

### Admin Commands

| Command | Purpose |
|---------|---------|
| `nodetool status` | View cluster health and node status |
| `nodetool repair` | Trigger repair process |
| `nodetool compactionstats` | View ongoing compaction |
| `nodetool tpstats` | Thread pool statistics |
| `nodetool info` | Detailed node information |
| `DESCRIBE KEYSPACE demo;` | Show keyspace schema |
| `DESCRIBE TABLE users;` | Show table schema |

---

## 6. Spring Boot Integration & Configuration {#section6}

### Step 1: Add Maven Dependency

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-cassandra</artifactId>
</dependency>
```

### Step 2: Configure application.yml

```yaml
spring:
  data:
    cassandra:
      contact-points: localhost
      port: 9042
      keyspace-name: demo
      local-datacenter: datacenter1
      schema-action: create_if_not_exists
      request:
        timeout: 10s
```

**Configuration meanings:**

| Property | Purpose |
|----------|---------|
| contact-points | Cassandra node address |
| port | CQL native port |
| keyspace-name | Default keyspace |
| local-datacenter | Required for driver (even single DC) |
| schema-action | Auto-create tables if not exists |
| timeout | Query timeout |

### Step 3: Define Entity Class

```java
@Table("users")
public class User {
  
  @PrimaryKey
  private UUID userId;
  
  private String name;
  private String city;
  
  // Constructors
  public User() {}
  
  public User(UUID userId, String name, String city) {
    this.userId = userId;
    this.name = name;
    this.city = city;
  }
  
  // Getters & Setters
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
}
```

### Step 4: Create Repository

```java
public interface UserRepository extends CassandraRepository<User, UUID> {
  // Derive queries from method names
  List<User> findByCity(String city);
}
```

### Step 5: Use in Service

```java
@Service
public class UserService {
  
  @Autowired
  private UserRepository userRepo;
  
  public void saveUser(String name, String city) {
    User user = new User(UUID.randomUUID(), name, city);
    userRepo.save(user);
  }
  
  public Optional<User> getUser(UUID userId) {
    return userRepo.findById(userId);
  }
  
  public List<User> getUsersByCity(String city) {
    return userRepo.findByCity(city);
  }
}
```

### Composite Keys

For tables with multiple partition key components:

```java
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

### Repository Queries

```java
public interface EventRepository extends CassandraRepository<Event, EventKey> {
  
  // Derived queries
  List<Event> findByEventKeyUserIdAndEventKeyDay(UUID userId, LocalDate day);
  
  // Ordered
  List<Event> findByEventKeyUserIdAndEventKeyDayOrderByEventKeyEventTimeDesc(
    UUID userId, LocalDate day
  );
}
```

### TTL in Spring Boot

```java
@Table("logs")
public class Log {
  
  @PrimaryKey
  private UUID logId;
  
  @TTL
  private long ttl = 86400;  // 24 hours
  
  private String message;
}
```

Or in CQL:
```cql
INSERT INTO logs (log_id, message) VALUES (uuid(), 'Event') 
USING TTL 86400;
```

### Async Repositories

For non-blocking operations:

```java
public interface UserRepository extends CassandraRepository<User, UUID> {
  
  CompletableFuture<User> findByIdAsync(UUID userId);
  
  CompletableFuture<Void> deleteByIdAsync(UUID userId);
}
```

---

## 7. Production Best Practices {#section7}

### Table Design Checklist

- [ ] **Partition Key**: Distributes evenly, matches query pattern, bounds partition size
- [ ] **Clustering Keys**: Used for sorting, ranges, or multi-row queries
- [ ] **One table per query**: Never try to do multiple queries on one table
- [ ] **Time-series tables**: Use composite keys with day/bucket to bound size
- [ ] **Denormalization**: Multiple tables for multiple queries is expected
- [ ] **Compaction strategy**: TWCS for logs/events, LCS for reads, STCS for mixed

### Write Path Optimization

| Consideration | Best Practice |
|---------------|----------------|
| Batch writes | Use batch inserts from application |
| TTL strategy | Use partition-level TTL (time-bucket) |
| Denormalized writes | Write to all denormalized tables in one transaction |
| Idempotency | Ensure all writes are idempotent (safe to retry) |
| Retry logic | Implement exponential backoff |

### Read Path Optimization

| Consideration | Best Practice |
|---------------|----------------|
| Partition key | Always specify in WHERE clause |
| Bloom filters | Set `bloom_filter_fp_chance = 0.01` for read-heavy |
| Row cache | Use for hot keys only |
| Caching | Disable for time-series, enable for profiles |
| Secondary indexes | Never at scale—use denormalized tables |

### Operational Excellence

| Task | Frequency | Importance |
|------|-----------|-----------|
| Run repair | At least once per 10 days | Critical |
| Monitor compaction | Daily | High |
| Monitor tombstones | Daily | High |
| Check disk usage | Daily | High |
| Check GC logs | Weekly | Medium |
| Capacity planning | Monthly | Medium |

### Kafka → Cassandra Ingestion Pattern

```
Kafka Topic
   ↓
Consumer Service
   ↓
Transform Event
   ↓
Write to Multiple Cassandra Tables
   ↓
Ack Kafka Offset
```

**Key principles:**
- Exactly-once processing (idempotent writes)
- Parallel writes to denormalized tables
- Batch when possible
- Exponential backoff on failures

### What NOT to Do

| Anti-Pattern | Why It Fails |
|--------------|-------------|
| Use secondary indexes at scale | Triggers cross-node scans |
| Don't specify partition key | Scans entire cluster |
| Rely on Materialized Views for sync | Async lag causes inconsistency |
| Use ALLOW FILTERING | Full table scans |
| Never run repair | Silent data loss over time |
| Row-level TTL on high-volume tables | Tombstone explosion |
| Design without thinking about growth | Hot partitions kill cluster |

### Real-World Example: Netflix Architecture

**Problem:** Store billions of streaming logs.

**Solution:**
```cql
CREATE TABLE streaming_logs (
  user_id uuid,
  date date,
  bucket int,
  timestamp timestamp,
  session_id uuid,
  content_id text,
  PRIMARY KEY ((user_id, date, bucket), timestamp)
) WITH compaction = {
  'class': 'TimeWindowCompactionStrategy',
  'compaction_window_size': '1',
  'compaction_window_unit': 'DAYS'
}
AND default_time_to_live = 2592000;  -- 30 days
```

**Results:**
- Writes: 5M+/sec
- Reads: 100K+/sec
- Read latency: <10ms
- No data loss
- Geo-replicated

**Key decisions:**
- Composite partition key (user_id, date, bucket)
- Bucketing splits load across nodes
- TWCS compaction for efficient cleanup
- TTL for automatic cleanup
- Time-bound queries naturally fit

---

## Table: Cassandra Options – Quick Reference

The table below shows important Cassandra table creation options. **Most require no changes from defaults:**

| Option | Default | When to Change | Value Range |
|--------|---------|----------------|-------------|
| `bloom_filter_fp_chance` | 0.1 | Read-heavy workloads | 0.01–1.0 |
| `caching` | keys:ALL, rows:500 | Hot data only | Disable for time-series |
| `compaction` | SizeTiered | Time-series → TWCS, Read-heavy → LCS | Strategy name |
| `compression` | LZ4 | Keep default | LZ4 (best) |
| `crc_check_chance` | 1.0 | Keep default | 1.0 |
| `default_time_to_live` | 0 (no TTL) | Time-series tables | Seconds |
| `gc_grace_seconds` | 864000 (10 days) | Keep default | Must be > repair window |
| `speculative_retry` | 99PERCENTILE | Keep default | PERCENTILE or µs |

### Tuning Philosophy

**Cassandra performance is controlled by:**
1. **Schema design** (partition key, clustering, denormalization)
2. **Compaction strategy** (TWCS vs LCS vs STCS)
3. **TTL/cleanup policy** (time-bucketing)

**NOT by:**
- Dozens of micro-tuning options
- Complex index configurations
- Query optimization flags

Focus on the big three and Cassandra scales automatically.

---

## Summary: Cassandra's Unique Value Proposition

| Aspect | Traditional DB | Cassandra |
|--------|---|---|
| Write throughput | Millions/sec (with replication overhead) | Billions/sec |
| Availability | Single master risk | Zero SPOF |
| Consistency | Strong (ACID) | Eventual (AP model) |
| Scalability | Vertical | Linear horizontal |
| Design | Entity-driven | Query-driven |
| Data duplication | Minimized (normalized) | Maximized (denormalized) |
| Failure recovery | Complex | Self-healing via gossip |

Cassandra is not a replacement for relational databases. It's a purpose-built engine for:
- **Event streams** (Kafka → Cassandra)
- **Time-series data** (metrics, logs, IoT)
- **Planet-scale systems** (Netflix, Uber, Apple, Twitter)
- **Zero-downtime systems** (no master, auto-healing)

Master its architecture, schema design, and operational patterns, and you've mastered distributed systems at scale.

---

## Next Steps for Production Readiness

1. **Set up local Cassandra** via Docker (4 nodes, RF=3)
2. **Model your first schema** with partition key, clustering, and denormalization
3. **Test time-series write loads** using TWCS compaction
4. **Integrate with Spring Boot** using CassandraRepository
5. **Design Kafka consumer** that fans out to multiple tables
6. **Automate repair** using operational tooling
7. **Monitor** disk, compaction, tombstones, read/write latencies
8. **Load test** with realistic throughput and partition sizes

---

**Version:** 1.0  
**Date:** January 2026  
**Audience:** Senior Java Engineers, Backend Architects  
**Knowledge Level:** Intermediate to Advanced
