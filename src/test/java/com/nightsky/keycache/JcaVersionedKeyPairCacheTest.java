package com.nightsky.keycache;

import com.nightsky.keycache.factory.KeyPairFactory;
import com.nightsky.keycache.factory.KeyStoreFactory;
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
public class JcaVersionedKeyPairCacheTest {

    private JcaVersionedKeyPairCache subject;

    @Before
    public void setUp()
        throws KeyStoreException, CertificateException,
               NoSuchAlgorithmException, IOException
    {
        // Initialize a new key store:
        String keyStorePassword = RandomStringUtils.randomAlphanumeric(16);
        KeyStore keyStore = KeyStoreFactory.createJceKeyStore(keyStorePassword);

        // Create a few random key pairs named according to the requirements
        // of the JcaVersionedKeyPairCache:
        Map<String, Resource> keyPasswords =
            KeyPairFactory.createRandomRsaKeyPairs(keyStore, 5);


        // Save the KeyStore into memory:
        byte [] rawKeyStore = null;
        try ( ByteArrayOutputStream os = new ByteArrayOutputStream() ) {
            keyStore.store(os, keyStorePassword.toCharArray());
            rawKeyStore = os.toByteArray();
        }

        // Create the test subject with the random key store data:
        subject = JcaVersionedKeyPairCache.builder()
            .withKeyPasswords(keyPasswords)
            .withKeyStorePasswordResource(new ByteArrayResource(keyStorePassword.getBytes(StandardCharsets.UTF_8)))
            .withKeyStoreResource(new ByteArrayResource(rawKeyStore))
            .withKeyStoreType(KeyStoreFactory.JCE_KEYSTORE_TYPE)
                .build();
    }

    @Test
    public void shouldGetMostRecentVersionOfKeyPair() {
        VersionedKeyPair keyPair = subject.getKeyPair(KeyPairFactory.KEY_NAME);
        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getVersion()).isEqualTo(5);
    }

    @Test
    public void shouldGetSpecificVersionOfKeyPair() {
        int version = 3;
        VersionedKeyPair keyPair = subject.getKeyPair(KeyPairFactory.KEY_NAME, version);
        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getVersion()).isEqualTo(version);
    }

}
