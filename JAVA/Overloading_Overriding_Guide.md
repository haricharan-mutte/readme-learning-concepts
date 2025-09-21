# Method Overloading & Overriding in Java

## Method Overriding Rules
When a subclass redefines a superclass method:

### 1. Signature Match
- Method name, parameters, and return type must match exactly (or be covariant return type).

**Example:**
```java
class Super { 
    Number get() { return 1; } 
}  
class Sub extends Super { 
    @Override Integer get() { return 2; } // ✓ Covariant return 
}
```

### 2. Access Modifier
- Cannot be more restrictive than the superclass method.  
- Allowed: `protected → public`  
- Not Allowed: `public → private`

### 3. Exception Handling
- If parent class declares an exception, the subclass can:
  - Throw the same exception
  - Throw a subclass exception
  - Throw no exception
  - Throw unchecked exceptions (even if not declared by parent)
- If parent class does not declare an exception:
  - Subclass **cannot** throw new checked exceptions
  - But subclass **can** throw unchecked exceptions

**Allowed:** `IOException → FileNotFoundException`  
**Not Allowed:** `IOException → Exception`

### 4. final & static Methods
- `final` methods cannot be overridden.  
- `static` and `private` methods are hidden, not overridden.

### 5. @Override Annotation
- Always use `@Override` to enforce signature checks.

---

## Method Overloading Rules
Defining multiple methods with the same name but different parameters:

### 1. Parameter Distinction
Methods must differ by:
- Number of parameters  
- Type of parameters  
- Order of parameters  

**Example:**
```java
class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}
```

### 2. Return Type
- Return type can be the same or different.  
- But return type alone is **not enough** for overloading.

### 3. Access Modifiers & Exceptions
- Can have different access modifiers (`public`, `private`, etc.).  
- Can throw different exceptions.

---

## Key Differences Between Overloading & Overriding

| Feature                  | Overloading | Overriding |
|--------------------------|-------------|------------|
| Parameters               | Must differ | Must be same |
| Return Type              | Can differ (but not only basis) | Must be same or covariant |
| Access Modifier          | Can differ | Cannot be more restrictive |
| Exceptions               | Can differ | Cannot throw broader checked exceptions |
| Static/final/private     | Allowed | Not applicable (they can’t be overridden) |
| Compile-time vs Run-time | Compile-time polymorphism | Run-time polymorphism |

---

✅ **Overloading = Compile-time Polymorphism**  
✅ **Overriding = Run-time Polymorphism**
