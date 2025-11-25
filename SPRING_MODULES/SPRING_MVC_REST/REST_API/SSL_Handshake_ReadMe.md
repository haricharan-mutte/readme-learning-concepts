# SSL Handshake and Certificates

## 🔐 What is SSL/TLS?

**SSL (Secure Sockets Layer)** and its successor **TLS (Transport Layer Security)** are cryptographic protocols that provide secure communication over a computer network. TLS is the modern standard and is used in **HTTPS** to secure web traffic.

## 🌐 How SSL/TLS Is Linked to HTTPS

- **HTTPS** = HTTP + SSL/TLS
- When you access a website using `https://`, your browser initiates a **TLS handshake** to securely exchange encryption keys and verify the server's identity before any HTTP data is transmitted.
- This ensures **confidentiality**, **integrity**, and **authenticity** of the communication.

---

## 🤝 SSL Handshake Overview

An **SSL/TLS handshake** is the process where:
1. The client (e.g., browser) connects to the server.
2. The server sends its **digital certificate**.
3. The client checks if the certificate is **trusted**.
4. If trusted, both exchange **keys** using public key cryptography.
5. A **secure symmetric key** is derived and used for encrypted communication.

---

## 🔐 What Are Certificates?

A **digital certificate**:
- Is issued by a **Certificate Authority (CA)**
- Verifies the ownership of a public key
- Contains: public key, domain, organization, CA signature

Types:
- **Self-signed certificate** (used in development)
- **CA-signed certificate** (used in production)

---

## 📁 Keystore vs Truststore

| Concept     | Keystore                        | Truststore                        |
|-------------|----------------------------------|-----------------------------------|
| Purpose     | Stores **your** private keys & certs | Stores **trusted third-party** certs |
| Common Usage| Server-side (SSL termination)   | Client-side (verifying server certs) |
| File Type   | `.jks`, `.p12`, etc.            | `.jks`, `.p12`, etc.              |

---

## 🛠️ How to Generate Certificates and Stores

### 1. Generate Keystore and Self-Signed Certificate
```bash
keytool -genkeypair -alias myserver -keyalg RSA -keystore keystore.jks -keysize 2048
```

### 2. Export the Certificate
```bash
keytool -exportcert -alias myserver -keystore keystore.jks -file myserver.crt
```

### 3. Import Certificate into Truststore
```bash
keytool -importcert -alias myserver -file myserver.crt -keystore truststore.jks
```

---

## 🔧 SSL in Spring Boot

Configure `application.properties`:

```properties
server.port=8443
server.ssl.key-store=classpath:keystore.jks
server.ssl.key-store-password=changeit
server.ssl.key-alias=myserver
server.ssl.enabled=true
```

---

## ✅ Summary

- TLS secures HTTPS.
- Certificates validate identities.
- Keystore holds your secrets; Truststore holds others' certs.
- The SSL handshake ensures secure, authenticated communication.