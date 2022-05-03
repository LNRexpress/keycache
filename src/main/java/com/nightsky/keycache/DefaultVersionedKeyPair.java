package com.nightsky.keycache;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author Chris
 */
public class DefaultVersionedKeyPair implements VersionedKeyPair {

    private final KeyPair keyPair;

    private final int version;

    public DefaultVersionedKeyPair(KeyPair keyPair, int version) {
        this.keyPair = keyPair;
        this.version = version;
    }

    @Override
    public PublicKey getPublic() {
        return keyPair.getPublic();
    }

    @Override
    public PrivateKey getPrivate() {
        return keyPair.getPrivate();
    }

    @Override
    public int getVersion() {
        return this.version;
    }

}
