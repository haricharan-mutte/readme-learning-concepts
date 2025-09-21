
# Design Patterns (Creational + Structural)

This document summarizes the design patterns we have discussed so far, with explanations and Java examples.

---

## Creational Patterns

### 1. Factory Pattern
**What is it & Why?**
- A Factory Pattern is used to create objects without exposing the creation logic to the client.
- Instead of calling constructors directly, the client asks a factory class to give the required object.

**Structure:**
- Factory class with a static method that returns different types of objects based on input.
- Hides object creation complexity.

**Example in Java:**
```java
interface Shape {
    void draw();
}

class Circle implements Shape {
    public void draw() {
        System.out.println("Drawing Circle");
    }
}

class Square implements Shape {
    public void draw() {
        System.out.println("Drawing Square");
    }
}

class ShapeFactory {
    public static Shape getShape(String type) {
        if ("CIRCLE".equalsIgnoreCase(type)) {
            return new Circle();
        } else if ("SQUARE".equalsIgnoreCase(type)) {
            return new Square();
        }
        return null;
    }
}

public class FactoryDemo {
    public static void main(String[] args) {
        Shape shape1 = ShapeFactory.getShape("CIRCLE");
        shape1.draw();

        Shape shape2 = ShapeFactory.getShape("SQUARE");
        shape2.draw();
    }
}
```

**Real-time Examples:**
- `Calendar.getInstance()` in Java
- JDBC `DriverManager.getConnection()`

**Use Cases:**
- When you need flexibility in creating objects
- When object creation logic is complex or based on conditions

---

### 2. Abstract Factory Pattern
**What is it & Why?**
- Provides an interface for creating families of related or dependent objects without specifying their concrete classes.
- Factory of factories.

**Structure:**
- Abstract factory interface
- Concrete factories implementing interface
- Clients use the abstract factory to get families of objects

**Example in Java:**
```java
interface Button {
    void paint();
}

interface Checkbox {
    void paint();
}

interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

class WinButton implements Button {
    public void paint() { System.out.println("Windows Button"); }
}

class MacButton implements Button {
    public void paint() { System.out.println("Mac Button"); }
}

class WinCheckbox implements Checkbox {
    public void paint() { System.out.println("Windows Checkbox"); }
}

class MacCheckbox implements Checkbox {
    public void paint() { System.out.println("Mac Checkbox"); }
}

class WinFactory implements GUIFactory {
    public Button createButton() { return new WinButton(); }
    public Checkbox createCheckbox() { return new WinCheckbox(); }
}

class MacFactory implements GUIFactory {
    public Button createButton() { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

public class AbstractFactoryDemo {
    public static void main(String[] args) {
        GUIFactory factory = new WinFactory();
        Button b = factory.createButton();
        Checkbox c = factory.createCheckbox();
        b.paint();
        c.paint();
    }
}
```

**Real-time Examples:**
- `javax.xml.parsers.DocumentBuilderFactory`
- `javax.xml.transform.TransformerFactory`

**Use Cases:**
- UI toolkits (Windows vs MacOS styles)
- Families of products where consistency matters

---

## Structural Patterns

### 1. Adapter Pattern
**What is it & Why?**
- Used to make two incompatible interfaces work together.
- Converts the interface of one class into another interface clients expect.

**Structure:**
- Target interface (what client expects)
- Adaptee (existing class with incompatible interface)
- Adapter (bridge between the two)

**Example in Java:**
```java
interface MediaPlayer {
    void play(String audioType, String filename);
}

class AdvancedMediaPlayer {
    public void playVlc(String filename) {
        System.out.println("Playing vlc file: " + filename);
    }
}

class MediaAdapter implements MediaPlayer {
    AdvancedMediaPlayer advancedPlayer = new AdvancedMediaPlayer();
    public void play(String audioType, String filename) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advancedPlayer.playVlc(filename);
        }
    }
}

class AudioPlayer implements MediaPlayer {
    MediaAdapter adapter;
    public void play(String audioType, String filename) {
        if (audioType.equalsIgnoreCase("mp3")) {
            System.out.println("Playing mp3: " + filename);
        } else if (audioType.equalsIgnoreCase("vlc")) {
            adapter = new MediaAdapter();
            adapter.play(audioType, filename);
        }
    }
}
```

**Real-time Examples:**
- `InputStreamReader` (adapts byte stream to character stream)
- `Arrays.asList()`

---

### 2. Bridge Pattern
**What is it & Why?**
- Decouples abstraction from its implementation.
- Lets you vary abstraction and implementation independently.

**Structure:**
- Abstraction (interface)
- Refined Abstraction (extends abstraction)
- Implementor (interface for implementations)
- Concrete Implementors

**Example in Java:**
```java
interface DrawAPI {
    void drawCircle(int radius, int x, int y);
}

class RedCircle implements DrawAPI {
    public void drawCircle(int radius, int x, int y) {
        System.out.println("Drawing red circle at (" + x + "," + y + ") with radius " + radius);
    }
}

class GreenCircle implements DrawAPI {
    public void drawCircle(int radius, int x, int y) {
        System.out.println("Drawing green circle at (" + x + "," + y + ") with radius " + radius);
    }
}

abstract class Shape {
    protected DrawAPI drawAPI;
    protected Shape(DrawAPI drawAPI) { this.drawAPI = drawAPI; }
    public abstract void draw();
}

class Circle extends Shape {
    private int x, y, radius;
    public Circle(int x, int y, int radius, DrawAPI drawAPI) {
        super(drawAPI);
        this.x = x; this.y = y; this.radius = radius;
    }
    public void draw() {
        drawAPI.drawCircle(radius, x, y);
    }
}

public class BridgePatternDemo {
    public static void main(String[] args) {
        Shape redCircle = new Circle(100, 100, 10, new RedCircle());
        Shape greenCircle = new Circle(100, 100, 10, new GreenCircle());
        redCircle.draw();
        greenCircle.draw();
    }
}
```

**Real-time Examples:**
- JDBC Driver (abstraction) + Database implementation
- Spring JPA (abstraction) with Hibernate/JDBC (implementation)

---

### 3. Composite Pattern
**What is it & Why?**
- Composes objects into tree structures to represent part-whole hierarchies.
- Allows clients to treat individual objects and compositions uniformly.

**Structure:**
- Component (interface)
- Leaf (individual object)
- Composite (collection of components)

**Example in Java:**
```java
interface Employee {
    void showDetails();
}

class Developer implements Employee {
    private String name;
    public Developer(String name) { this.name = name; }
    public void showDetails() {
        System.out.println("Developer: " + name);
    }
}

class Manager implements Employee {
    private String name;
    private List<Employee> subordinates = new ArrayList<>();
    public Manager(String name) { this.name = name; }
    public void add(Employee e) { subordinates.add(e); }
    public void showDetails() {
        System.out.println("Manager: " + name);
        for (Employee e : subordinates) {
            e.showDetails();
        }
    }
}
```

**Real-time Examples:**
- Directory/File structure in OS
- Swing UI components

---

### 4. Decorator Pattern
**What is it & Why?**
- Adds new functionality to an object dynamically without altering its structure.

**Structure:**
- Component interface
- Concrete component
- Decorator (abstract class implementing component)
- Concrete decorators adding features

**Example in Java (Car Decoration):**
```java
interface Car {
    void assemble();
}

class BasicCar implements Car {
    public void assemble() {
        System.out.println("Basic Car.");
    }
}

class CarDecorator implements Car {
    protected Car decoratedCar;
    public CarDecorator(Car c) { this.decoratedCar = c; }
    public void assemble() {
        decoratedCar.assemble();
    }
}

class SportsCar extends CarDecorator {
    public SportsCar(Car c) { super(c); }
    public void assemble() {
        super.assemble();
        System.out.println("Adding features of Sports Car.");
    }
}

class LuxuryCar extends CarDecorator {
    public LuxuryCar(Car c) { super(c); }
    public void assemble() {
        super.assemble();
        System.out.println("Adding features of Luxury Car.");
    }
}

public class DecoratorPatternDemo {
    public static void main(String[] args) {
        Car sportsCar = new SportsCar(new BasicCar());
        sportsCar.assemble();

        Car luxuryCar = new LuxuryCar(new SportsCar(new BasicCar()));
        luxuryCar.assemble();
    }
}
```

**Real-time Examples:**
- `BufferedReader` wrapping `FileReader`
- `Collections.synchronizedList()`

---

### 5. Facade Pattern
**What is it & Why?**
- Provides a simplified interface to a complex subsystem.

**Structure:**
- Facade class delegates requests to subsystem classes.
- Hides system complexities from client.

**Example in Java:**
```java
class CPU {
    public void start() { System.out.println("CPU started"); }
}

class Memory {
    public void load() { System.out.println("Memory loaded"); }
}

class HardDrive {
    public void read() { System.out.println("HardDrive read"); }
}

class ComputerFacade {
    private CPU cpu;
    private Memory memory;
    private HardDrive hd;
    public ComputerFacade() {
        cpu = new CPU();
        memory = new Memory();
        hd = new HardDrive();
    }
    public void start() {
        cpu.start();
        memory.load();
        hd.read();
        System.out.println("Computer Started.");
    }
}
```

**Real-time Examples:**
- Spring Boot `SpringApplication.run()` (hides multiple complex startup processes)
- JDBC with helper classes

---

### 6. Flyweight Pattern
**What is it & Why?**
- Reduces memory usage by sharing common parts of objects instead of creating new ones.

**Structure:**
- Flyweight interface
- Concrete Flyweight (shared)
- Flyweight Factory (manages shared objects)

**Example in Java:**
```java
interface Shape {
    void draw();
}

class Circle implements Shape {
    private String color;
    private int x, y, radius;
    public Circle(String color) { this.color = color; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setRadius(int radius) { this.radius = radius; }
    public void draw() {
        System.out.println("Circle: Draw() [Color=" + color + ", x=" + x + ", y=" + y + ", radius=" + radius);
    }
}

class ShapeFactory {
    private static final Map<String, Shape> circleMap = new HashMap<>();
    public static Shape getCircle(String color) {
        Circle circle = (Circle) circleMap.get(color);
        if (circle == null) {
            circle = new Circle(color);
            circleMap.put(color, circle);
            System.out.println("Creating circle of color: " + color);
        }
        return circle;
    }
}
```

**Real-time Examples:**
- String pool in Java (`String.intern()`)
- Integer cache (`Integer.valueOf()`)

---

### 7. Proxy Pattern
**What is it & Why?**
- Provides a surrogate/placeholder object to control access to another object.

**Structure:**
- Subject interface
- Real Subject (actual object)
- Proxy (controls access to real subject)

**Example in Java:**
```java
interface Image {
    void display();
}

class RealImage implements Image {
    private String filename;
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();
    }
    private void loadFromDisk() {
        System.out.println("Loading " + filename);
    }
    public void display() {
        System.out.println("Displaying " + filename);
    }
}

class ProxyImage implements Image {
    private RealImage realImage;
    private String filename;
    public ProxyImage(String filename) {
        this.filename = filename;
    }
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}

public class ProxyPatternDemo {
    public static void main(String[] args) {
        Image image = new ProxyImage("test.jpg");
        image.display();
        image.display(); // second time doesn't reload
    }
}
```

**Real-time Examples:**
- Hibernate Lazy Loading (proxy objects)
- Spring AOP Proxies
- RMI (Remote Proxy)

---

## Conclusion
We have covered:
- **Creational Patterns**: Factory, Abstract Factory
- **Structural Patterns**: Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy
