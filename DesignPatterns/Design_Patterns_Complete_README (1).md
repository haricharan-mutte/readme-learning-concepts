# Design Patterns, HLD/LLD, SOLID, Immutable Design, and How to Combine Them (Java-Focused)

> A practical, one-stop README you can learn from, copy code from, and reference in interviews or at work.

---

## What are Design Patterns?
Design patterns are **proven, reusable templates** for solving common design problems. They’re not full programs—just **structured approaches** that help you write code that is easier to **extend, test, and maintain**.

### Why do we need them?
- Create a **shared vocabulary** for design decisions (e.g., “use Strategy here”).  
- Reduce **accidental complexity** by reusing **battle-tested solutions**.  
- Improve **testability**, **loose coupling**, and **separation of concerns**.  
- Make architecture decisions **intentional** and **documented**.
- Makes code more **reusable**, maintainable, and scalable.

### Types of Design Patterns
1. **Creational** – object creation: *Singleton, Factory Method, Abstract Factory, Builder, Prototype*  
2. **Structural** – composition and structure: *Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy*  
3. **Behavioral** – object interaction: *Strategy, Observer, Command, Chain of Responsibility, State, Template Method, Visitor, Mediator, Memento, Iterator*

> Tip: Patterns compose. You’ll often see multiple patterns working together (e.g., **Factory + Strategy**, **Facade + Singleton**, **Observer + Mediator**).

---

### Analogy
- Think of design patterns like recipes in cooking:
- You don’t invent a new way every time you make pasta; you follow a proven recipe.
- But you can still tweak ingredients (implementation) based on your project needs.
---






# 1) Creational Patterns

## 1.1 Singleton
**What**: Ensure **one instance** with a **global access point**.  
**Why**: Shared resources (config, caches, loggers).  
**Structure**: Private ctor + controlled instance creation.

### Variations (Java)
#### A) Lazy (non-thread-safe) – *demo only*
```java
public class LazySingleton {
  private static LazySingleton instance;
  private LazySingleton() {}
  public static LazySingleton getInstance() {
    if (instance == null) instance = new LazySingleton();
    return instance;
  }
}
```
#### B) Synchronized (simple & safe, slower)
```java
public class SyncSingleton {
  private static SyncSingleton instance;
  private SyncSingleton() {}
  public static synchronized SyncSingleton getInstance() {
    if (instance == null) instance = new SyncSingleton();
    return instance;
  }
}
```
#### C) Double-Checked Locking (fast & safe)
```java
public class DclSingleton {
  private static volatile DclSingleton instance;
  private DclSingleton() {}
  public static DclSingleton getInstance() {
    if (instance == null) {
      synchronized (DclSingleton.class) {
        if (instance == null) instance = new DclSingleton();
      }
    }
    return instance;
  }
}
```
#### D) Initialization-on-Demand Holder (clean & safe)
```java
public class HolderSingleton {
  private HolderSingleton() {}
  private static class Holder {
    private static final HolderSingleton INSTANCE = new HolderSingleton();
  }
  public static HolderSingleton getInstance() { return Holder.INSTANCE; }
}
```
#### E) Enum (serialization & reflection proof)
```java
public enum EnumSingleton {
  INSTANCE;
  public void doWork() {}
}
```

#### F)  Implement using AtomicReference
```
public class AtomicSingleton {
// Atomic reference to hold the Singleton instance
private static final AtomicReference<AtomicSingleton> INSTANCE = new AtomicReference<>();

    // Private constructor to prevent instantiation
    private AtomicSingleton() {
        System.out.println("Singleton created");
    }

    // Public method to get the instance
    public static AtomicSingleton getInstance() {
        AtomicSingleton current = INSTANCE.get();
        if (current == null) {
            AtomicSingleton newInstance = new AtomicSingleton();
            // Atomically set the instance if it's still null
            if (INSTANCE.compareAndSet(null, newInstance)) {
                return newInstance;
            } else {
                // Another thread beat us to it, use the one it created
                return INSTANCE.get();
            }
        }
        return current;
    }

    // Just a sample method
    public void sayHello() {
        System.out.println("Hello from Singleton!");
    }
}
```

**Real usage**: `java.lang.Runtime`, Spring beans (default singleton scope), logging singletons.  
**Use cases**: configuration registry, shared object mapper, centralized metrics client.

---

## 1.2 Factory Method
**What**: Defer creation to subclasses/factories.  
**Why**: Remove `new` from client code; follow **Open–Closed**.  
**Structure**: Creator defines factory method; concrete creators override.

```java
interface Button { void render(); }
class WindowsButton implements Button { public void render(){ System.out.println("Win"); } }
class MacButton implements Button { public void render(){ System.out.println("Mac"); } }

abstract class Dialog {
  public void renderWindow() {
    Button ok = createButton();
    ok.render();
  }
  protected abstract Button createButton();
}
class WindowsDialog extends Dialog {
  protected Button createButton() { return new WindowsButton(); }
}
class MacDialog extends Dialog {
  protected Button createButton() { return new MacButton(); }
}
```
**Variations**: static factory method; parameterized factory; registry-based factory.

```java
// Static/Parameterized
class ShapeFactory {
  public static Shape create(String type) {
    return switch (type.toLowerCase()) {
      case "circle" -> new Circle();
      case "square" -> new Square();
      default -> throw new IllegalArgumentException("Unknown type");
    };
  }
}
```

**Real usage**: `java.util.Calendar#getInstance`, `DocumentBuilderFactory`, Spring `FactoryBean`.  
**Use cases**: UI widgets, drivers, message parsers.

---

## 1.3 Abstract Factory
**What**: Create **families of related objects** without specifying concrete classes.  
**Why**: Keep products consistent (e.g., dark theme widgets).

```java
interface Button { void paint(); }
interface Checkbox { void toggle(); }

interface GUIFactory {
  Button createButton();
  Checkbox createCheckbox();
}
class WinFactory implements GUIFactory {
  public Button createButton(){ return new WinButton(); }
  public Checkbox createCheckbox(){ return new WinCheckbox(); }
}
class MacFactory implements GUIFactory {
  public Button createButton(){ return new MacButton(); }
  public Checkbox createCheckbox(){ return new MacCheckbox(); }
}

class Application {
  private final Button button; private final Checkbox checkbox;
  Application(GUIFactory f){ this.button = f.createButton(); this.checkbox = f.createCheckbox(); }
}
```
**Variations**: factory-of-factories; reflectively-loaded factories; DI containers.  
**Real usage**: Spring profiles/beans, JDBC drivers via `DriverManager`.  
**Use cases**: theming, cloud-provider abstractions, pluggable storage engines.

---

## 1.4 Builder
**What**: Step-by-step construction of complex objects; **immutable outputs**.  
**Why**: Many optional params; telescoping constructors avoided.

```java
class User {
  private final String name; private final int age; private final String email;
  private User(Builder b){ this.name=b.name; this.age=b.age; this.email=b.email; }
  public static class Builder {
    private String name; private int age; private String email;
    public Builder name(String v){ this.name=v; return this; }
    public Builder age(int v){ this.age=v; return this; }
    public Builder email(String v){ this.email=v; return this; }
    public User build(){ return new User(this); }
  }
}
User u = new User.Builder().name("Hari").age(28).email("h@x.com").build();
```
**Variations**: fluent builder, staged builder (type-safe steps), Lombok `@Builder`, Director.

```java
// Staged builder (compile-time enforced ordering)
interface Step1 { Step2 name(String n); }
interface Step2 { Step3 age(int a); }
interface Step3 { User build(); }
```
**Real usage**: `StringBuilder`, `Stream.Builder`, many HTTP client builders.  
**Use cases**: DTOs, config objects, immutable domain objects.

---

## 1.5 Prototype
**What**: Create new objects by **cloning** existing ones.  
**Why**: Expensive creation or many similar objects.

```java
class Document implements Cloneable {
  String title; List<String> pages = new ArrayList<>();
  public Document(String t){ this.title=t; }
  @Override protected Document clone() {
    try {
      Document copy = (Document) super.clone();
      copy.pages = new ArrayList<>(this.pages); // deep-copy mutable field
      return copy;
    } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
  }
}
```
**Variations**: shallow vs deep clone; copy constructors; serialization-based copy.  
**Real usage**: `Object#clone` (legacy), `java.lang.Record` copy via `with`-like patterns; JSON copy.  
**Use cases**: game objects, UI element templates, configuration blueprints.

---

# 2) Structural Patterns

## 2.1 Adapter
**What**: Convert one interface to another expected by clients.  
**Why**: Integrate incompatible APIs.

```java
// Target
interface PaymentProcessor { void pay(int cents); }
// Adaptee
class ThirdPartyPay {
  void makePayment(double dollars){ System.out.println("Paid $" + dollars); }
}
// Object Adapter
class PaymentAdapter implements PaymentProcessor {
  private final ThirdPartyPay adaptee = new ThirdPartyPay();
  public void pay(int cents){ adaptee.makePayment(cents / 100.0); }
}
```
**Variations**: object adapter (composition), class adapter (inheritance).  
**Real usage**: Spring `HandlerAdapter`, Java IO `InputStreamReader`.  
**Use cases**: migrate libraries, wrap legacy services.

---

## 2.2 Bridge
**What**: Decouple abstraction from implementation to vary them independently.

```java
interface Renderer { void renderCircle(float radius); }
class VectorRenderer implements Renderer { public void renderCircle(float r){ System.out.println("Vector "+r); } }
class RasterRenderer implements Renderer { public void renderCircle(float r){ System.out.println("Raster "+r); } }

abstract class Shape { protected Renderer r; Shape(Renderer r){ this.r=r; } abstract void draw(); }
class Circle extends Shape { float radius; Circle(Renderer r, float radius){ super(r); this.radius=radius; }
  void draw(){ r.renderCircle(radius); }
}
```
**Real usage**: JDBC (API) + vendors (drivers), SLF4J + bindings.  
**Use cases**: multi-platform UIs, storage backends, renderers.

---

## 2.3 Composite
**What**: Treat **individual** and **composed** objects uniformly (tree).

```java
interface Component { void render(); }
class Leaf implements Component { public void render(){ System.out.println("Leaf"); } }
class CompositeNode implements Component {
  private final List<Component> children = new ArrayList<>();
  public void add(Component c){ children.add(c); }
  public void render(){ children.forEach(Component::render); }
}
```
**Real usage**: Swing/AWT containers, DOM tree.  
**Use cases**: menus, file systems, org charts.

---

## 2.4 Decorator
**What**: Add behavior **dynamically** without changing the original class.

```java
interface Notifier { void send(String msg); }
class EmailNotifier implements Notifier { public void send(String m){ System.out.println("Email: "+m); } }
class NotifierDecorator implements Notifier {
  protected final Notifier wrappee;
  NotifierDecorator(Notifier n){ this.wrappee=n; }
  public void send(String m){ wrappee.send(m); }
}
class SMSDecorator extends NotifierDecorator {
  public SMSDecorator(Notifier n){ super(n); }
  public void send(String m){ super.send(m); System.out.println("SMS: "+m); }
}
```
**Real usage**: Java IO streams (`BufferedInputStream`), Spring `HandlerInterceptor` chains.  
**Use cases**: cross-cutting features (caching, retry, metrics).

---

## 2.5 Facade
**What**: Provide a **simple API** over a **complex subsystem**.

```java
class VideoFacade {
  private final Demuxer d = new Demuxer(); private final Decoder dec = new Decoder();
  public Frame extractFirst(String file){ var stream = d.open(file); return dec.decode(stream); }
}
```
**Real usage**: `javax.crypto.Cipher`, Spring `JdbcTemplate`.  
**Use cases**: SDKs, libraries hiding protocol details.

---

## 2.6 Flyweight
**What**: Share **intrinsic** state to save memory; pass **extrinsic** state at runtime.

```java
record Glyph(char c) {}
class GlyphFactory {
  private static final Map<Character,Glyph> cache = new HashMap<>();
  public static Glyph get(char c){ return cache.computeIfAbsent(c, Glyph::new); }
}
```
**Real usage**: `Integer.valueOf` cache, String interning.  
**Use cases**: text editors, map tiles, particle systems.

---

## 2.7 Proxy
**What**: Stand-in that controls access to a real object.  
**Variations**: virtual (lazy), remote, protection, logging, caching, **dynamic** proxy.

```java
interface Service { String data(); }
class RealService implements Service { public String data(){ return "DATA"; } }

// Static proxy
class LoggingProxy implements Service {
  private final Service target;
  LoggingProxy(Service t){ this.target=t; }
  public String data(){ System.out.println("before"); var r=target.data(); System.out.println("after"); return r; }
}

// JDK dynamic proxy
import java.lang.reflect.*;
class DynamicLogging {
  @SuppressWarnings("unchecked")
  static <T> T wrap(T target, Class<T> iface){
    return (T) Proxy.newProxyInstance(
      iface.getClassLoader(),
      new Class<?>[]{iface},
      (Object p, Method m, Object[] a) -> {
        System.out.println("before"); Object r = m.invoke(target, a);
        System.out.println("after"); return r;
      });
  }
}
```
**Real usage**: Spring AOP proxies, Hibernate lazy proxies, RMI.  
**Use cases**: cross-cutting concerns, remote calls, access control.

---

# 3) Behavioral Patterns

## 3.1 Strategy
**What**: Swap algorithms at runtime behind a common interface.

```java
interface SortStrategy { <T extends Comparable<T>> void sort(List<T> list); }
class QuickSortStrategy implements SortStrategy {
  public <T extends Comparable<T>> void sort(List<T> list){ list.sort(Comparable::compareTo); }
}
class Context {
  private SortStrategy s; public Context(SortStrategy s){ this.s=s; }
  public <T extends Comparable<T>> void perform(List<T> items){ s.sort(items); }
}
```
**Variation (Java 8+)**: lambdas as strategies.
```java
Context c = new Context((List<Integer> xs) -> Collections.sort(xs));
```
**Real usage**: `Comparator` strategies, Spring `ConversionService`.  
**Use cases**: pricing, routing, compression algorithms.

---

## 3.2 Observer (Publish–Subscribe)
**What**: Notify dependents automatically when state changes.

```java
interface Observer { void update(String event); }
interface Subject { void attach(Observer o); void detach(Observer o); void notifyAll(String e); }

class EventBus implements Subject {
  private final List<Observer> obs = new ArrayList<>();
  public void attach(Observer o){ obs.add(o); }
  public void detach(Observer o){ obs.remove(o); }
  public void notifyAll(String e){ obs.forEach(o -> o.update(e)); }
}
```
**Variations**: push vs pull; reactive streams.  
**Real usage**: `java.util.Observer`(legacy), RxJava/Project Reactor, GUI events, Spring ApplicationEvents.  
**Use cases**: eventing, domain events, cache invalidation.

---

## 3.3 Command
**What**: Encapsulate requests as objects (undo/redo, queues).

```java
interface Command { void execute(); }
class Light { void on(){System.out.println("ON");} void off(){System.out.println("OFF");} }
class LightOn implements Command { private final Light l; LightOn(Light l){this.l=l;} public void execute(){ l.on(); } }
class Invoker { private final Queue<Command> q = new ArrayDeque<>(); void submit(Command c){ q.add(c); } void run(){ while(!q.isEmpty()) q.poll().execute(); } }
```
**Real usage**: GUI actions, job queues, `Runnable`.  
**Use cases**: audit/undo, task scheduling, transactional steps.

---

## 3.4 Chain of Responsibility
**What**: Pass request along chain until one handles it.

```java
abstract class Handler {
  protected Handler next;
  public Handler linkWith(Handler n){ this.next=n; return n; }
  public void handle(String req){ if(next!=null) next.handle(req); }
}
class AuthHandler extends Handler { public void handle(String r){ if(!r.contains("auth")) throw new RuntimeException("no auth"); super.handle(r);} }
class LoggingHandler extends Handler { public void handle(String r){ System.out.println("log"); super.handle(r);} }
```
**Real usage**: Servlet filters, Spring `HandlerInterceptor`, SLF4J appenders.  
**Use cases**: request pipelines, middleware, validation chains.

---

## 3.5 State
**What**: Change behavior when internal state changes.

```java
interface State { void handle(ContextX c); }
class ContextX { private State s; public ContextX(State s){this.s=s;} void setState(State s){this.s=s;} void request(){ s.handle(this);} }
class Draft implements State { public void handle(ContextX c){ System.out.println("draft->review"); c.setState(new Review()); } }
class Review implements State { public void handle(ContextX c){ System.out.println("review->publish"); c.setState(new Published()); } }
class Published implements State { public void handle(ContextX c){ System.out.println("already published"); } }
```
**Real usage**: parser states, workflow engines, TCP connection states.  
**Use cases**: order lifecycle, documents, payment flows.

---

## 3.6 Template Method
**What**: Define algorithm skeleton; defer steps to subclasses.

```java
abstract class DataExporter {
  public final void export(){ open(); writeData(); close(); }
  protected abstract void open(); protected abstract void writeData(); protected void close(){ System.out.println("close"); }
}
class CsvExporter extends DataExporter { protected void open(){ System.out.println("open csv"); } protected void writeData(){ System.out.println("rows"); } }
```
**Real usage**: JUnit `setUp/tearDown`, Spring `JdbcTemplate`.  
**Use cases**: algorithm families with invariant structure.

---

## 3.7 Visitor
**What**: Add new operations over an object structure **without modifying** the elements.

```java
interface Visitable { void accept(Visitor v); }
interface Visitor { void visit(Book b); void visit(Fruit f); }
class Book implements Visitable { int price; public Book(int p){price=p;} public void accept(Visitor v){ v.visit(this);} }
class Fruit implements Visitable { int weight; public Fruit(int w){weight=w;} public void accept(Visitor v){ v.visit(this);} }
class PriceVisitor implements Visitor {
  public void visit(Book b){ System.out.println("Book price:"+b.price); }
  public void visit(Fruit f){ System.out.println("Fruit weight:"+f.weight); }
}
```
**Real usage**: AST visitors (compilers), XML/JSON visiting.  
**Use cases**: code analysis, reporting across mixed elements.

---

## 3.8 Mediator
**What**: Centralize communication to reduce object coupling.

```java
interface ChatRoom { void send(String from, String msg); }
class SimpleChat implements ChatRoom {
  private final Map<String, User> users = new HashMap<>();
  void join(User u){ users.put(u.name, u); }
  public void send(String from, String msg){ users.values().forEach(u -> { if(!u.name.equals(from)) u.receive(from, msg); }); }
}
class User { final String name; final ChatRoom room; User(String n, ChatRoom r){ name=n; room=r; }
  void send(String m){ room.send(name, m); } void receive(String f, String m){ System.out.println(f+": "+m); } }
```
**Real usage**: UI dialog coordinators, message brokers, Spring `ApplicationContext` as mediator.  
**Use cases**: chat rooms, air-traffic control metaphors, module orchestration.

---

## 3.9 Memento
**What**: Capture object state to restore later (undo).

```java
class Editor {
  private String text; public void set(String t){text=t;} public String get(){return text;}
  public Memento save(){ return new Memento(text); } public void restore(Memento m){ this.text = m.state; }
  static class Memento { private final String state; private Memento(String s){state=s;} }
}
```
**Use cases**: undo/redo, checkpoints, transactional savepoints.

---

## 3.10 Iterator
**What**: Sequential access without exposing underlying structure.  
**Real usage**: Java `Iterator`, `Iterable`, Streams.  
**Use cases**: collections, custom cursors.

---

# 4) Beyond GoF: SAGA & Orchestrator Patterns

## 4.1 SAGA Pattern (Distributed Transactions)
**What**: Manage a long business transaction as a series of **local transactions** with **compensating actions** on failure (no 2PC).  
**Styles**:  
- **Choreography** – services publish/subscribe domain events; flow emerges.  
- **Orchestration** – a central **orchestrator** commands steps.

### Example (Choreography) with events
```java
// Pseudocode using Spring events (or Kafka)
class OrderService {
  void placeOrder(Order o){
    // save order PENDING
    eventBus.publish(new OrderCreated(o.id()));
  }
  @EventListener void onPaymentFailed(PaymentFailed e){ // compensate
    // mark order CANCELED and refund if needed
  }
}
class PaymentService {
  @EventListener void onOrderCreated(OrderCreated e){
    boolean ok = charge(e.orderId());
    if(ok) eventBus.publish(new PaymentSucceeded(e.orderId()));
    else   eventBus.publish(new PaymentFailed(e.orderId()));
  }
}
class InventoryService {
  @EventListener void onPaymentSucceeded(PaymentSucceeded e){ reserveStock(e.orderId()); }
}
```
### Example (Orchestration) with a coordinator
```java
class OrderOrchestrator {
  private final PaymentClient payment; private final InventoryClient inventory; private final ShippingClient shipping;
  public void execute(String orderId){
    if(!payment.charge(orderId)) { // stop + compensate
      // notify failure
      return;
    }
    if(!inventory.reserve(orderId)) {
      payment.refund(orderId);
      return;
    }
    shipping.createShipment(orderId);
  }
}
```
**Use cases**: e-commerce checkout, booking, multi-step onboarding.  
**Framework notes**: Spring + Kafka/RabbitMQ; Camunda/Temporal for workflow-based orchestration (sagas as workflows).

## 4.2 Orchestrator Design Pattern
**What**: A dedicated component coordinating interactions/steps between services or modules, often encoding **workflow**, **timeouts**, **retries**, and **compensation**.  
**When**: complex sequencing, visibility/monitoring, central policy, easier change management.  
**Trade-offs**: single place of control but risk of centralization; design for **idempotency** and **failure handling**.

---

# 5) HLD vs LLD (with examples)

## High-Level Design (HLD)
**Scope**: system **architecture**—components, data flow, storage, scaling, APIs, external deps, SLAs, **non-functionals**.  
**Artifacts**: block diagrams, sequence diagrams, data models, capacity estimates, read/write patterns, sharding/partitioning plans, caching strategy, messaging topology.

**Example (URL Shortener HLD)**  
- Components: API Gateway → URL Service → DB; Redirect Service; Cache (Redis); Analytics pipeline.  
- Storage: write-heavy table `short_code -> long_url` (NoSQL or RDBMS with index).  
- Non-functional: 99.9% uptime; 10k RPS; P99 latency < 50ms; rate limiting.

## Low-Level Design (LLD)
**Scope**: **class-level** design, interfaces, DTOs, DB schema details, algorithms, error handling, validations, exact APIs, design patterns selection.  
**Artifacts**: class diagrams, method signatures, entity relationships, sequence details.

**Example (LLD for URL Shortener)**  
- Class `UrlService` with `createShortUrl(String)` -> `ShortUrl`.  
- Repository interface `UrlRepository { Optional<String> find(String code); void save(String code, String longUrl); }`  
- Patterns: **Strategy** for code generation (Base62 vs Hash), **Decorator** for rate-limiter, **Proxy** for metrics/logging, **Factory** for repository (in-memory vs Redis).

---

# 6) SOLID Principles (Java Examples)

1. **S – Single Responsibility**: one reason to change.
```java
class InvoicePrinter { void print(Invoice i){} }
class InvoiceRepository { void save(Invoice i){} }
```
2. **O – Open/Closed**: open for extension, closed for modification.
```java
interface Discount { double apply(double amount); }
class DiwaliDiscount implements Discount { public double apply(double a){ return a*0.9; } }
```
3. **L – Liskov Substitution**: subtypes must be substitutable for base types.
```java
interface Bird { void fly(); } // Bad if some birds can't fly – model capabilities, not taxonomy.
```
4. **I – Interface Segregation**: many small interfaces > one fat interface.
```java
interface Printer { void print(); }
interface Scanner { void scan(); }
```
5. **D – Dependency Inversion**: depend on abstractions, not concretions.
```java
class ReportService {
  private final Repository repo; // interface
  ReportService(Repository r){ this.repo=r; }
}
```

---

# 7) Immutable Design Pattern

**What**: Objects whose **state cannot change** after construction.  
**Why**: Thread-safety, simpler reasoning, safe sharing, cache keys.  
**Rules**: all fields `final`, no setters, defensive copies, class `final` (or make constructors inaccessible).

```java
public final class Money {
  private final String currency;
  private final long cents;
  public Money(String currency, long cents){ this.currency=currency; this.cents=cents; }
  public String currency(){ return currency; }
  public long cents(){ return cents; }
  public Money add(Money other){
    if(!this.currency.equals(other.currency)) throw new IllegalArgumentException("mismatch");
    return new Money(currency, this.cents + other.cents);
  }
}
```
**Variations**: value objects, persistent data structures; Java `record` for concise immutable carriers.  
**Real usage**: `String`, `Integer`, `BigDecimal`, Java `record` types.

---

# 8) Applying Patterns & Principles Together

### How to decide **what** to apply **where**
- **Volatile algorithms?** → **Strategy** (pluggable logic).  
- **Multiple product families?** → **Abstract Factory** (consistency).  
- **Complex object construction?** → **Builder** (immutable outputs).  
- **Cross-cutting concerns?** → **Decorator/Proxy** (metrics, retry, caching).  
- **Pipelines/workflows?** → **Chain of Responsibility**, **Template Method**, or **Orchestrator**.  
- **Distributed transactions?** → **SAGA** (orchestration/choreography).  
- **Tree structures?** → **Composite**.  
- **Incompatible APIs?** → **Adapter**.  
- **Subsystem complexity?** → **Facade**.  
- **Memory pressure with many similar objects?** → **Flyweight**.  
- **Lifecycle/stateful flows?** → **State**.  
- **Extending operations across many element types?** → **Visitor**.

### Best Practices
- Lead with **SOLID**; patterns are a means, not a goal.  
- Prefer **composition over inheritance** (Decorator, Strategy, Bridge).  
- Keep objects **immutable** when feasible; use **Builder**.  
- Make edges **idempotent** and **retry-safe**; add **timeouts** and **circuit breakers** (Proxy/Decorator).  
- Separate **domain** from **infrastructure**; use **ports & adapters** (Hexagonal).  
- For microservices: use **SAGA**, **Outbox**, **CQRS** judiciously; central **orchestrator** for complex flows.  
- Document decisions with short **Architecture Decision Records (ADR)**.  
- Test at seams: swap strategies, inject fakes via factories, test workflow steps independently.

### Example: Combining in a Payment Flow
- **HLD**: API → Orchestrator → Payment/Inventory/Shipping; Kafka for events; Redis cache.  
- **LLD**:  
  - **Strategy** for payment providers (Stripe/PayPal).  
  - **Abstract Factory** for creating provider-specific clients.  
  - **Builder** for immutable requests.  
  - **Proxy/Decorator** adds retries/metrics/logging.  
  - **SAGA Orchestrator** coordinates steps; compensations on failure.  
  - **Observer** for domain events to notify email service.

---

# 9) Real-World Whereabouts (Java/Frameworks)

- **Spring**: singleton-scoped beans (Singleton), `BeanFactory` (Factory/Abstract Factory), `RestTemplate/WebClient` builders (Builder), AOP proxies (Proxy/Decorator), `HandlerInterceptor`/filter chains (Chain of Responsibility), application events (Observer), `JdbcTemplate` (Facade).  
- **JDBC**: API/Driver split (Bridge), `DriverManager` factories, connection pools (Singleton + Factory).  
- **Java IO/NIO**: decorators for buffering, readers/writers; adapters for streams/readers.  
- **Hibernate**: lazy proxies, Unit-of-Work (transactional Command-ish), factories for sessions.  
- **Reactive**: Project Reactor’s operator chains (Decorator/Chain), backpressure (Mediator-like scheduling).

---

# 10) Practice Prompts (Use Cases to Try)

1. Build a **rate-limited** API client using **Decorator** for retry + backoff + metrics.  
2. Implement a **feature flag** system with **Strategy** for toggled algorithms.  
3. Create a **modular UI** toolkit using **Abstract Factory** for themes + **Bridge** for renderers.  
4. Model an **order lifecycle** via **State** + **Observer** notifications.  
5. Implement an **e-commerce checkout** with **SAGA Orchestrator** (payments, inventory, shipping) and compensations.

---

## Next Steps
- Start with **SOLID + Immutability** on new code.  
- Introduce patterns where **pain** exists (duplication, tight coupling, rigidity).  
- Keep examples runnable and covered with tests.  
- Capture architecture choices with **ADRs**.  
- Revisit designs as requirements evolve—**evolutionary architecture**.

---

### Appendix: Quick Reference Table

| Problem | Pattern | Key Benefit |
|---|---|---|
| One global instance | Singleton | Single source of truth |
| Complex creation | Builder | Readable, immutable objects |
| Switch by type | Factory Method | Open for extension |
| Related families | Abstract Factory | Consistency across products |
| Incompatible APIs | Adapter | Integration without rewrite |
| Vary implementations | Bridge | Orthogonal variability |
| Tree structures | Composite | Uniform treatment |
| Add features | Decorator/Proxy | Composition-based extension |
| Simplify subsystem | Facade | Friendly API |
| Too many similar objects | Flyweight | Memory savings |
| Pluggable algorithms | Strategy | Replaceable behavior |
| Notify dependents | Observer | Event-driven design |
| Encapsulate operations | Command | Queue/undo/logging |
| Request pipeline | Chain of Responsibility | Flexible processing |
| Behavior by state | State | Explicit transitions |
| Algorithm template | Template Method | Reuse skeleton |
| New ops over elements | Visitor | Add ops without changing types |
| Long transactions | SAGA | Reliability with compensation |
| Step coordination | Orchestrator | Centralized flow control |

---

**Happy building!** 🚀
