package com.nightsky.keycache;

import javax.crypto.SecretKey;

/**
 *
 * @author Chris
 */
public interface VersionedSecretKey extends SecretKey {

    public int getVersion();

}
