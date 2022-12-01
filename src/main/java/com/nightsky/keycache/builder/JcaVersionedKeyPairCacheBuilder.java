package com.nightsky.keycache.builder;

import com.nightsky.keycache.JcaVersionedKeyPairCache;
import java.util.Map;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class JcaVersionedKeyPairCacheBuilder {

    private final JcaVersionedKeyPairCache target;

    public JcaVersionedKeyPairCacheBuilder() {
        this.target = new JcaVersionedKeyPairCache();
    }

    public JcaVersionedKeyPairCacheBuilder withKeyStoreResource(Resource resource) {
        target.setKeyStoreResource(resource);
        return this;
    }

    public JcaVersionedKeyPairCacheBuilder withKeyStorePasswordResource(Resource resource) {
        target.setKeyStorePasswordResource(resource);
        return this;
    }

    public JcaVersionedKeyPairCacheBuilder withKeyStorePassword(String password) {
        target.setKeyStorePassword(password);
        return this;
    }

    public JcaVersionedKeyPairCacheBuilder withKeyPasswords(Map<String, Resource> keyPasswords) {
        target.setKeyPasswords(keyPasswords);
        return this;
    }

    public JcaVersionedKeyPairCacheBuilder withKeyStoreType(String keyStoreType) {
        target.setKeyStoreType(keyStoreType);
        return this;
    }

    public JcaVersionedKeyPairCacheBuilder withKeyNamePattern(String keyNamePattern) {
        target.setKeyNamePattern(keyNamePattern);
        return this;
    }

    public JcaVersionedKeyPairCache build() {
        return target;
    }

}
