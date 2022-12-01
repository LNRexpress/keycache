package com.nightsky.keycache.factory;

import com.nightsky.keycache.factory.exception.SecretKeyCreationException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.KeyGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class SecretKeyFactory {

    public static final String KEY_NAME = "test-key";

    public static Map<String, Resource> createRandomAesKeys(KeyStore keyStore, int count) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);

            Map<String, Resource> keyPasswords = new HashMap<>();
            for (int i = 1; i <= count; i++) {
                String keyName = String.format("%s-v%d", KEY_NAME, i);
                String keyPassword = RandomStringUtils.randomAlphanumeric(16);
                keyPasswords.put(keyName,
                    new ByteArrayResource(keyPassword.getBytes(StandardCharsets.UTF_8)));
                KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(kg.generateKey());
                KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(keyPassword.toCharArray());
                keyStore.setEntry(keyName, entry, param);
            }

            return keyPasswords;
        } catch (Exception e) {
            throw new SecretKeyCreationException(e);
        }
    }

}
