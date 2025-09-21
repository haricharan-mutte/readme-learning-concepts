# Java & JVM Knowledge Base

This document consolidates the Java, JVM, and related topics discussed in a chat session. It covers JVM memory, garbage collection, performance tuning, best practices, monitoring tools, and more.

---

## 1. JVM Memory Model - What It Is and How It Works

**Definition:**  
The Java Virtual Machine (JVM) Memory Model defines how memory is organized and managed when a Java program runs. It ensures efficient allocation, execution, and garbage collection while providing thread safety and consistency across multiple cores and processors.

## 1. Memory Areas Of JVM:
---
The JVM divides memory into several logical areas, each serving a specific purpose:

##### 1. Method Area
Stores class-level information like:
Class structure (methods, fields, metadata)
Constant pool
Static variables
Shared among all threads.

##### 2. Heap
Stores objects and arrays.
It is shared across threads.
Managed by Garbage Collection (GC).
The largest portion of memory in JVM.

##### 3. Stack
Stores method call frames, including:
Local variables
Operand stack
Method call information
Each thread has its own stack → helps in thread safety.

##### 4. Program Counter (PC) Register
Keeps track of the current instruction being executed.
Each thread has its own PC register → allows independent execution.

##### 5. Native Method Stack
Used for executing native methods written in languages like C or C++.

---
## ⚙ 2. How JVM Memory Model Works
##### A. Class Loading
When you run a Java program, classes are loaded into the Method Area.
Static data and constants are prepared for use.

##### B. Object Creation
Objects are created in the Heap memory.
Reference to objects is stored in the Stack.

##### C. Method Invocation
When a method is called:
A new frame is created in the Stack.
Local variables and parameters are stored in that frame.
Instructions are processed using the PC register.

##### D. Garbage Collection
Unused objects in the Heap are identified and removed.

This helps free memory and prevent leaks.

---
# 🔄 3. Working with Threads and Memory Consistency

Multiple threads can access the Heap, so memory consistency is important.

JVM Memory Model defines how variables are read and written:

Volatile keyword → ensures that a variable is read from main memory, not cached locally.

Synchronized blocks/methods → ensure that only one thread accesses critical sections at a time.

This prevents:

Data races , Visibility problems

--- 
## 🟠 4. Key Concepts Related to JVM Memory Model

Stack vs Heap:

Stack → faster, private to each thread.

Heap → slower, shared among threads.

Garbage Collection → automates memory management.

Synchronization → ensures thread-safe access to shared memory.

---

| Area               | Purpose                              | Shared/Thread-specific |
|-------------------|--------------------------------------|----------------------|
| Method Area       | Class info, static data               | Shared               |
| Heap              | Objects, arrays                       | Shared               |
| Stack             | Local variables, method calls         | Thread-specific      |
| PC Register       | Instruction pointer                   | Thread-specific      |
| Native Stack      | Native methods execution              | Thread-specific      |

---
## 2. JVM Performance Tuning
Tuning JVM helps your Java application run faster, more efficiently, and avoid unnecessary pauses or crashes due to memory pressure. It’s about managing heap size, garbage collection, thread settings, etc.

📌 Key Areas for JVM Performance Tuning
### 🔹 A. Heap Size Configuration

Initial heap size (-Xms) → the memory allocated at startup.

Maximum heap size (-Xmx) → the maximum memory the JVM can use.

** ex: java -Xms512m -Xmx2048m MyApp**

### 🔹 B. Garbage Collector Selection
##### Different GC algorithms have pros and cons:

| GC Algorithm     | Description                     | Use Cases                                |
| ---------------- | ------------------------------- | ---------------------------------------- |
| Serial GC        | Single-threaded GC              | Small apps, low-latency not critical     |
| Parallel GC      | Multithreaded GC for throughput | Medium to large apps                     |
| G1 GC            | Balanced, region-based GC       | Large heaps, low pause times             |
| ZGC / Shenandoah | Ultra-low pause GC              | Large-scale applications, near real-time |

You can specify GC using JVM flags:
#### java -XX:+UseG1GC MyApp

## 🔹 C. Monitoring and Profiling
Tools:

JVisualVM → monitor memory, CPU usage, GC activity.

JConsole → track thread, memory, and class loading.

GC logs → analyze GC behavior.

Enable GC logs:

## 🔹 D. Just-In-Time (JIT) Compilation

JVM optimizes bytecode to native instructions at runtime.

Tuning JIT parameters can boost performance in computation-heavy apps.

Example:

** java -XX:+TieredCompilation MyApp

## E. Thread Configuration

Thread pools help reuse threads instead of constantly creating new ones.

Avoid creating excessive threads → leads to context-switching overhead.

Use frameworks like Executors:  ExecutorService executor = Executors.newFixedThreadPool(10);

## 🔹 F. Avoiding Excessive Synchronization

Use fine-grained locks or concurrent collections (ConcurrentHashMap) instead of global synchronization.

Avoid blocking critical paths unnecessarily.


# ✅ JVM Performance Tuning Checklist:

✔ Set proper heap sizes (-Xms, -Xmx)
✔ Choose the right garbage collector
✔ Monitor GC logs and heap dumps
✔ Optimize thread usage and synchronization
✔ Tune JIT and runtime flags when needed

---
✅ Memory Leaks in JVM

### 📌 What is a Memory Leak?
A memory leak happens when objects that are no longer needed are still referenced, preventing the garbage collector from freeing memory → leads to memory exhaustion and performance degradation.

## 🔹 Common Causes of Memory Leaks

##### Static Collections

Objects stored in static lists, maps, etc., that are never cleared.

static List<String> cache = new ArrayList<>();

##### Unclosed Resources

Files, database connections, sockets not closed → memory or native resource leaks.

✅ Always close resources:

try (FileReader reader = new FileReader("file.txt")) {
// work with file
}


##### Listeners or Callbacks Not Deregistered

Objects kept alive by event listeners or observers.

##### Improper Caching

Storing too much data without eviction policies.

##### ThreadLocal Misuse

Threads holding references that are never cleaned up.

---
## 🔹 How to Detect Memory Leaks

##### Monitoring Tools

JVisualVM, JConsole, Eclipse MAT (Memory Analyzer Tool).

##### Heap Dumps

Analyze object references → find what's holding onto memory.

##### GC Logs

##### See frequent Full GCs without reclaiming space → memory pressure.

##### OutOfMemoryError

JVM explicitly throws java.lang.OutOfMemoryError: Java heap space.

## 🔹 How to Fix Memory Leaks

✔ Avoid keeping unnecessary references 

✔ Use weak references for caches when appropriate 

✔ Always close resources in finally or try-with-resources 

✔ Deregister listeners when not needed 

✔ Avoid using ThreadLocal unless necessary and clean up properly 

✔ Implement eviction policies in caching mechanisms 

Example – using WeakReference: Map<String, WeakReference<Object>> cache = new HashMap<>();

---

## ✅ Memory Leak Checklist:

✔ Regularly profile memory usage

✔ Analyze heap dumps

✔ Avoid static fields unless necessary

✔ Clean up after listeners and threads

✔ Use resource management patterns like try-with-resources

✔ Implement proper cache management

## 📘 Summary – JVM Performance & Memory Leaks

#### Performance Tuning:

Control heap size and GC.

Optimize threading, synchronization, and JIT compilation.

Monitor using tools and logs.

Memory Leaks:

Caused by lingering references, unclosed resources, or caching mistakes.

Detect using profiling tools and GC logs.

Fix by cleaning up references and resources systematically.