package com.nightsky.keycache;

/**
 *
 * @author Chris
 */
public interface VersionedKeyPairCache {

    public VersionedKeyPair getKeyPair(String keyPairName);

    public VersionedKeyPair getKeyPair(String keyPairName, Integer keyVersion);

}
