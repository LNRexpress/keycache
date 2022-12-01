package com.nightsky.keycache.factory;

import com.nightsky.keycache.factory.exception.KeyStoreCreationException;
import java.security.KeyStore;
import java.security.Security;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

/**
 *
 * @author Chris
 */
public class KeyStoreFactory {

    public static final String JCE_KEYSTORE_TYPE = "JCEKS";

    public static final String BOUNCY_CASTLE_FIPS_KEYSTORE_TYPE = "BCFKS";

    /**
     * Creates and loads a new <code>KeyStore</code>.
     *
     * @param keyStorePassword The password used to secure the key store
     * @return A newly created and loaded <code>KeyStore</code>
     */
    public static KeyStore createJceKeyStore(String keyStorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance(JCE_KEYSTORE_TYPE);
            keyStore.load(null, keyStorePassword.toCharArray());

            return keyStore;
        } catch (Exception e) {
            throw new KeyStoreCreationException(e);
        }
    }

    /**
     * Uses the Bouncy Castle FIPS library to create and load a new
     * FIPS-compliant <code>KeyStore</code>.
     *
     * @param keyStorePassword The password used to secure the key store
     * @return A newly created and loaded <code>KeyStore</code>
     */
    public static KeyStore createBouncyCastleFipsKeyStore(String keyStorePassword) {
        try {
            // Add the Bouncy Castle FIPS security provider, if needed:
            if ( Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME) == null ) {
                Security.addProvider(new BouncyCastleFipsProvider());
            }

            KeyStore keyStore = KeyStore.getInstance(BOUNCY_CASTLE_FIPS_KEYSTORE_TYPE);
            keyStore.load(null, keyStorePassword.toCharArray());

            return keyStore;
        } catch (Exception e) {
            throw new KeyStoreCreationException(e);
        }
    }

}
