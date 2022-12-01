# keycache

## Description

`keycache` is a lightweight library for caching and retrieving symmetric and asymmetric keys from a Java-compatible key store. In addition, `keycache` supports key versioning so that you can access past versions of your keys.

## Motivation

When working with symmetric and asymmetric keys, it is not always desirable to use a heavy secrets manager, such as HashiCorp Vault. These secrets managers add an extra point of failure and require monitoring and management by your dev ops team. That said, one really useful feature of secrets managers is that they store past versions of your secret keys.

In situations where you want to easily cache and retrieve symmetric and asymmetric keys along with past versions of those keys, `keycache` can fill that need. `keycache` provides a simple, minimal interface for accessing your symmetric and asymmetric keys and past versions of those keys.

## Requirements

* Java 8 or higher
* Apache Maven 3.6 or higher
* org.springframework:spring-core, version 5.3.18

## Compilation

```
mvn clean package
```

## Test Execution

```
mvn test
```

## Installation

```
mvn install
```

## Usage

### Declare Dependency (Apache Maven)

```
<dependency>
    <groupId>com.nightsky</groupId>
    <artifactId>keycache</artifactId>
    <version>1.2.1</version>
</dependency>
```

### Common Set-up

```
// Configure the location of the key store:
Resource keyStoreResource = new FileSystemResource("/path/to/keyStore.ks");

// Configure the location of the key store password:
Resource keyStorePasswordResource = new FileSystemResource("/path/to/keyStore.password");

// Load the locations of the key passwords into memory:
Map<String, Resource> keyPasswords =
    Collections.singletonMap("test_key-v1", new FileSystemResource("/path/to/key.password"));
```

In the above set-up code, `test_key-v1` is the name, or alias, of the key in the key store. Key names, or aliases, in the key store must follow the following naming pattern: `([\\p{Alnum}_-]+)-v(\\d+)`

### Creating a Cache for Versioned Symmetric Keys

```
JcaVersionedSecretKeyCache versionedSecretKeyCache = JcaVersionedSecretKeyCache.builder()
    .withKeyPasswords(keyPasswords)
    .withKeyStorePasswordResource(keyStorePasswordResource)
    .withKeyStoreResource(keyStoreResource)
    .withKeyStoreType("JCEKS")
    .withExpireAfterWriteDuration(Duration.ofMinutes(60L))
        .build();
```

### Creating a Cache for Versioned Asymmetric Keys

```
JcaVersionedKeyPairCache versionedKeyPairCache = JcaVersionedKeyPairCache.builder()
    .withKeyPasswords(keyPasswords)
    .withKeyStorePasswordResource(keyStorePasswordResource)
    .withKeyStoreResource(keyStoreResource)
    .withKeyStoreType("JCEKS")
    .withExpireAfterWriteDuration(Duration.ofMinutes(60L))
        .build();
```

### Retrieving the Current Version of a Symmetric Key

```
VersionedSecretKey key = versionedSecretKeyCache.getKey("test_key");
```

### Retrieving a Specific Version of a Symmetric Key

```
VersionedSecretKey key = versionedSecretKeyCache.getKey("test_key", 1);
```

### Retrieving the Current Version of an Asymmetric Key

```
VersionedKeyPair keyPair = versionedKeyPairCache.getKeyPair("test_key");
```

### Retrieving a Specific Version of an Asymmetric Key

```
VersionedKeyPair keyPair = versionedKeyPairCache.getKeyPair("test_key", 1);
```
