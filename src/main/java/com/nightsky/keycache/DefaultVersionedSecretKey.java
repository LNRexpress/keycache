package com.nightsky.keycache;

import javax.crypto.SecretKey;

/**
 *
 * @author Chris
 */
public class DefaultVersionedSecretKey implements VersionedSecretKey {

    private final int version;

    private final SecretKey secretKey;

    public DefaultVersionedSecretKey(SecretKey secretKey, int version) {
        this.secretKey = secretKey;
        this.version = version;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public String getAlgorithm() {
        return secretKey.getAlgorithm();
    }

    @Override
    public String getFormat() {
        return secretKey.getFormat();
    }

    @Override
    public byte[] getEncoded() {
        return secretKey.getEncoded();
    }

}
