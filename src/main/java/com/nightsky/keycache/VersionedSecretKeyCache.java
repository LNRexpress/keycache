package com.nightsky.keycache;

/**
 *
 * @author Chris
 */
public interface VersionedSecretKeyCache {

    public VersionedSecretKey getKey(String keyName);

    public VersionedSecretKey getKey(String keyName, Integer keyVersion);

}
