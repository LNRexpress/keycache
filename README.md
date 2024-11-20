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

## Deploying to Artifactory

```
mvn -Dmaven.wagon.http.ssl.insecure=true deploy:deploy-file \
    -Dfile=target/keycache-1.2.1.jar -DpomFile=pom.xml \
    -DrepositoryId=artifactory-kloudsigning \
    -Durl=https://artifactory.kloudsigning.com/artifactory/libs-release-local
```

## Pushing to GitHub

### Add Your SSH Key to Your GitHub Account

Assuming you have generated a valid SSH key, you need to add your SSH key to your GitHub account. This can be done in your GitHub account settings under "SSH and GPG keys". Follow the instructions provided by GitHub to add your key.

### From Windows, Using Git Bash

#### Cache the GitHub Host Key

This is done by opening PuTTY and attempting to SSH to `github.com`. Accept the host key when prompted. *This step is only necessary the first time you attempt to push code to GitHub.*

#### Start the PuTTY SSH Agent

Locate the `pageant` executable among your installed programs and run it. `pageant` is an SSH agent, which PuTTY uses to manage your SSH keys. You should see a small icon in the Windows system tray indicating that the agent is running.

#### Add Your GitHub SSH Key to the PuTTY Agent

Right-click on the `pageant` icon in the system tray and select "Add Key". Navigate to your `~/.ssh` directory, which contains your private key file (e.g. `id_rsa`), and add it. If prompted for a passphrase, enter it and press "OK".

#### Configure Git to Use the PuTTY SSH Agent for Authentication

From the Git Bash shell, run:

```
export GIT_SSH='/c/path/to/plink.exe'
```

#### Push Your Code to GitHub

```
git push -u github master
git push -u github master --tags
```

#### Disable Usage of the PuTTY SSH Agent

```
unset GIT_SSH
```
