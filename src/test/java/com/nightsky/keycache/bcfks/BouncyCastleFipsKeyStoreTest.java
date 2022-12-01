package com.nightsky.keycache.bcfks;

import com.nightsky.keycache.JcaVersionedSecretKeyCache;
import com.nightsky.keycache.VersionedSecretKey;
import com.nightsky.keycache.factory.KeyStoreFactory;
import com.nightsky.keycache.factory.SecretKeyFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
@RunWith(JUnit4.class)
public class BouncyCastleFipsKeyStoreTest {

    private JcaVersionedSecretKeyCache subject;

    @Before
    public void setUp()
        throws KeyStoreException, CertificateException,
               NoSuchAlgorithmException, IOException
    {
        // Initialize a new key store:
        String keyStorePassword = RandomStringUtils.randomAlphanumeric(16);
        KeyStore keyStore = KeyStoreFactory.createBouncyCastleFipsKeyStore(keyStorePassword);

        // Create a few random secret keys named according to the requirements
        // of the JcaVersionedSecretKeyCache:
        Map<String, Resource> keyPasswords =
            SecretKeyFactory.createRandomAesKeys(keyStore, 5);

        // Save the KeyStore into memory:
        byte [] rawKeyStore = null;
        try ( ByteArrayOutputStream os = new ByteArrayOutputStream() ) {
            keyStore.store(os, keyStorePassword.toCharArray());
            rawKeyStore = os.toByteArray();
        }

        // Create the test subject with the random key store data:
        subject = JcaVersionedSecretKeyCache.builder()
            .withKeyPasswords(keyPasswords)
            .withKeyStorePasswordResource(new ByteArrayResource(keyStorePassword.getBytes(StandardCharsets.UTF_8)))
            .withKeyStoreResource(new ByteArrayResource(rawKeyStore))
            .withKeyStoreType(KeyStoreFactory.BOUNCY_CASTLE_FIPS_KEYSTORE_TYPE)
                .build();
    }

    @Test
    public void shouldGetMostRecentVersionOfSecretKey() {
        VersionedSecretKey key = subject.getKey(SecretKeyFactory.KEY_NAME);
        assertThat(key).isNotNull();
        assertThat(key.getVersion()).isEqualTo(5);
    }

    @Test
    public void shouldGetSpecificVersionOfSecretKey() {
        int version = 3;
        VersionedSecretKey key = subject.getKey(SecretKeyFactory.KEY_NAME, version);
        assertThat(key).isNotNull();
        assertThat(key.getVersion()).isEqualTo(version);
    }

}
