package com.nightsky.keycache;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author Chris
 */
public interface VersionedKeyPair {

    public PublicKey getPublic();

    public PrivateKey getPrivate();

    public int getVersion();

}
