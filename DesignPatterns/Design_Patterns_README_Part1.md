# Design Patterns, Principles, and Best Practices

## What are Design Patterns?
Design patterns are proven, reusable solutions to common problems in software design. They are not finished code but rather templates or guidelines that help solve recurring issues in a structured way.

### Why do we need Design Patterns?
- They provide **standard solutions** that are widely understood by developers.  
- Improve **code reusability** and **maintainability**.  
- Enhance **communication** between developers (shared vocabulary).  
- Reduce development time by applying **tried-and-tested** solutions.  

### Types of Design Patterns
1. **Creational Patterns** – Deal with object creation mechanisms.  
   Examples: Singleton, Factory, Abstract Factory, Builder, Prototype.  
2. **Structural Patterns** – Deal with class and object composition.  
   Examples: Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy.  
3. **Behavioral Patterns** – Deal with object collaboration and responsibilities.  
   Examples: Strategy, Observer, Command, Chain of Responsibility, State, Template Method, Visitor, Mediator, Memento, Iterator.  

---

# Creational Patterns

## Singleton Pattern
### What is it?
Ensures that a class has only **one instance** and provides a global access point to it.

### Why is it?
- Useful when exactly one object is needed (e.g., configuration, logging).  
- Prevents multiple instances that may lead to conflicts or resource overuse.  

### Structure
- Private constructor  
- Static instance  
- Global access method  

### Java Implementation
```java
public class Singleton {
    private static Singleton instance;
    
    private Singleton() {}
    
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

### Variations
- **Eager Initialization**  
- **Lazy Initialization (Thread-safe)**  
- **Bill Pugh Singleton (Inner static helper class)**  

### Real-time Examples
- `java.lang.Runtime`  
- `java.awt.Desktop`  

### Use Cases
- Logging frameworks  
- Database connection pool manager  
- Caches  

---

## Factory Pattern
### What is it?
Defines an interface for creating objects but lets subclasses decide which class to instantiate.

### Why is it?
- Removes tight coupling between client and implementation.  
- Promotes **loose coupling** and **open/closed principle**.  

### Structure
- Creator (Factory)  
- Product (Interface/Abstract class)  
- Concrete Products  

### Java Implementation
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
    public Shape getShape(String type) {
        if (type.equalsIgnoreCase("CIRCLE")) return new Circle();
        if (type.equalsIgnoreCase("SQUARE")) return new Square();
        return null;
    }
}
```

### Real-time Examples
- `java.util.Calendar#getInstance()`  
- `javax.xml.parsers.DocumentBuilderFactory`  

### Use Cases
- UI frameworks (buttons, dialogs).  
- Different payment gateways.  

---

# (More patterns will follow in next part...)
