package com.nightsky.keycache.builder;

import com.nightsky.keycache.JcaVersionedSecretKeyCache;
import java.util.Map;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class JcaVersionedSecretKeyCacheBuilder {

    private final JcaVersionedSecretKeyCache target;

    public JcaVersionedSecretKeyCacheBuilder() {
        this.target = new JcaVersionedSecretKeyCache();
    }

    public JcaVersionedSecretKeyCacheBuilder withKeyStoreResource(Resource resource) {
        target.setKeyStoreResource(resource);
        return this;
    }

    public JcaVersionedSecretKeyCacheBuilder withKeyStorePasswordResource(Resource resource) {
        target.setKeyStorePasswordResource(resource);
        return this;
    }

    public JcaVersionedSecretKeyCacheBuilder withKeyStorePassword(String password) {
        target.setKeyStorePassword(password);
        return this;
    }

    public JcaVersionedSecretKeyCacheBuilder withKeyPasswords(Map<String, Resource> keyPasswords) {
        target.setKeyPasswords(keyPasswords);
        return this;
    }

    public JcaVersionedSecretKeyCacheBuilder withKeyStoreType(String keyStoreType) {
        target.setKeyStoreType(keyStoreType);
        return this;
    }

    public JcaVersionedSecretKeyCacheBuilder withKeyNamePattern(String keyNamePattern) {
        target.setKeyNamePattern(keyNamePattern);
        return this;
    }

    public JcaVersionedSecretKeyCache build() {
        return target;
    }

}
