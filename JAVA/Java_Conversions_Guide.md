# Java Conversions – Complete Guide

This guide covers all types of conversions in Java: primitives, wrappers, widening/narrowing, autoboxing, upcasting/downcasting, arrays, collections, streams, and more. Useful for interviews and quick reference.

---

## 1. Primitives ↔ Wrappers

### ✅ Primitives → Wrappers (Boxing / Autoboxing)
```java
int a = 10;

// Manual boxing
Integer i1 = Integer.valueOf(a);

// Autoboxing
Integer i2 = a;
```

### ✅ Wrappers → Primitives (Unboxing / Auto-unboxing)
```java
Integer i = 20;

// Manual unboxing
int x1 = i.intValue();

// Auto-unboxing
int x2 = i;
```

⚡ **Note:** `null` wrapper → primitive → `NullPointerException`.

---

## 2. Widening vs Narrowing

### ✅ Widening (Safe, automatic)
```java
int a = 100;
long l = a;     // int → long
float f = l;    // long → float
double d = f;   // float → double
```

### ✅ Narrowing (Explicit, may lose data)
```java
double d = 123.456;
int i = (int) d;   // fractional part lost
byte b = (byte) 130; // overflow (-126)
```

---

## 3. Autoboxing + Widening

- Widening preferred over boxing.  

```java
void m(long x) {}
void m(Integer x) {}

m(10); // calls long, not Integer
```

---

## 4. Upcasting & Downcasting

### ✅ Upcasting (safe, automatic)
```java
class Animal {}
class Dog extends Animal {}

Dog d = new Dog();
Animal a = d;  // Upcasting
```

### ✅ Downcasting (explicit, risky)
```java
Animal a = new Dog();
Dog d = (Dog) a;  // OK at runtime

Animal a2 = new Animal();
Dog d2 = (Dog) a2; // ClassCastException
```

---

## 5. Collections ↔ Arrays

### ✅ Collection → Array
```java
List<String> list = List.of("A", "B", "C");

// To Object[]
Object[] arr1 = list.toArray();

// To String[]
String[] arr2 = list.toArray(new String[0]);
```

### ✅ Array → Collection
```java
String[] arr = {"X", "Y", "Z"};
List<String> list = Arrays.asList(arr); // fixed-size list
List<String> list2 = new ArrayList<>(Arrays.asList(arr)); // modifiable
```

---

## 6. Collections ↔ Streams

### ✅ Collection → Stream
```java
List<Integer> list = List.of(1, 2, 3, 4);
Stream<Integer> s = list.stream();
```

### ✅ Stream → Collection
```java
List<Integer> collected = s.collect(Collectors.toList());
Set<Integer> set = list.stream().collect(Collectors.toSet());
```

---

## 7. Arrays ↔ Streams

### ✅ Array → Stream
```java
int[] arr = {1, 2, 3};
IntStream intStream = Arrays.stream(arr);

String[] strArr = {"a", "b"};
Stream<String> stream = Arrays.stream(strArr);
```

### ✅ Stream → Array
```java
String[] strArr2 = stream.toArray(String[]::new);
Integer[] intArr = list.stream().toArray(Integer[]::new);
```

---

## 8. String Conversions

### ✅ String ↔ Primitive/Wrapper
```java
String s = "123";
int x = Integer.parseInt(s);
Integer i = Integer.valueOf(s);

String s2 = String.valueOf(x);
String s3 = i.toString();
```

### ✅ String ↔ Array
```java
String s = "hello";
char[] chars = s.toCharArray();
String back = new String(chars);
```

### ✅ String ↔ Collection
```java
List<String> chars = Arrays.asList("h","e","l","l","o");
String joined = String.join("", chars); // "hello"
```

---

## 9. Map Conversions

### ✅ Map → Set/List
```java
Map<Integer, String> map = Map.of(1, "A", 2, "B");

Set<Integer> keys = map.keySet();
Collection<String> values = map.values();
Set<Map.Entry<Integer,String>> entries = map.entrySet();
```

### ✅ Map → Stream
```java
map.entrySet().stream()
   .forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()));
```

---

## 10. Miscellaneous

### ✅ Object → String
```java
Object o = 42;
String s = String.valueOf(o);
```

### ✅ String → Object (via parsing/casting)
```java
Object o = Integer.parseInt("100");
```

### ✅ Arrays Deep Conversion
```java
int[][] arr = {{1,2}, {3,4}};
System.out.println(Arrays.deepToString(arr)); // [[1, 2], [3, 4]]
```

---

# 🔑 Cheat Sheet – Conversion Priorities

1. **Exact Match**  
2. **Widening (primitive → bigger primitive)**  
3. **Boxing/Unboxing**  
4. **Varargs**  
5. **Downcasting (runtime check)**  
6. **Ambiguity → Compile-time Error**  

---

✅ This is your **complete Java conversion reference**. 

---
# Java Method Overloading: Widening, Boxing, and Var-args

This README provides a concise summary of Java's method overload resolution order when dealing with primitives and reference types. Examples are included for clarity.

## Precedence Order
Java prefers widening over narrowing when method overloading with primitives. If widening isn't possible, it chooses auto-boxing.

**Method Overload Hierarchy:**
- Exact match ⇒ Widening ⇒ Boxing ⇒ Var-args

**Primitive Widening Order:**
- byte ⇒ short ⇒ int ⇒ long ⇒ float ⇒ double

## Reference Types
For overloaded methods with object types, Java will select the most relevant type. For example, if both `String` and `Object` are present, `String` is chosen since it's a subclass of `Object`.
- If both `String` and `Integer` objects are present and `null` is passed as an argument, compilation fails due to ambiguity.

## Example: Ambiguous Widening
