package com.nightsky.keycache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.Duration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class BcfksVersionedKeyPairCache implements VersionedKeyPairCache {

    private final LoadingCache<String, VersionedKeyPair> cache;

    private final Logger log;

    private final Resource keystoreResource;

    private final String keystorePassword;

    public BcfksVersionedKeyPairCache(Resource keystoreResource, String keystorePassword) {
        this.keystoreResource = keystoreResource;
        this.keystorePassword = keystorePassword;

        CacheLoader<String, VersionedKeyPair> loader = new CacheLoader<String, VersionedKeyPair>() {
            @Override
            public VersionedKeyPair load(String id) throws Exception {
                return retrieveVersionedKeyPair(id);
            }
        };

        cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(30L)).build(loader);
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public VersionedKeyPair getKeyPair(String keyPairName) {
        try {
            Pattern pattern = Pattern.compile("([\\p{Alnum}_-]+)-v(\\d+)");
            KeyStore keyStore = KeyStore.getInstance("BCFKS");
            keyStore.load(keystoreResource.getInputStream(), keystorePassword.toCharArray());
            Iterator<String> aliases = Iterators.forEnumeration(keyStore.aliases());

            // Get the current version number of the key
            int currentVersion = -1;
            while (aliases.hasNext()) {
                String alias = aliases.next();
                Matcher m = pattern.matcher(alias);
                if ( m.matches() ) {
                    String name = m.group(1);
                    int version = Integer.parseInt(m.group(2));
                    if ( name.equals(keyPairName) && version > currentVersion ) {
                        currentVersion = version;
                    }
                }
            }

            return cache.get(String.format("%s-v%d", keyPairName, currentVersion));
        } catch (Exception e) {
            log.error("Failed to retrieve key from cache", e);
            return null;
        }
    }

    @Override
    public VersionedKeyPair getKeyPair(String keyPairName, Integer keyVersion) {
        if ( keyVersion == null )
            return getKeyPair(keyPairName);

        try {
            return cache.get(String.format("%s-v%d", keyPairName, keyVersion));
        } catch (Exception e) {
            log.error("Failed to retrieve key from cache", e);
            return null;
        }
    }

    private VersionedKeyPair retrieveVersionedKeyPair(String alias) throws Exception {
        Pattern pattern = Pattern.compile("([\\p{Alnum}_-]+)-v(\\d+)");
        Matcher m = pattern.matcher(alias);

        if ( m.matches() ) {
            KeyStore keyStore = KeyStore.getInstance("BCFKS");
            keyStore.load(keystoreResource.getInputStream(), keystorePassword.toCharArray());

            Key key = keyStore.getKey(alias, keystorePassword.toCharArray());

            if ( key instanceof PrivateKey ) {
                Certificate cert = keyStore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
                return new DefaultVersionedKeyPair(keyPair, Integer.parseInt(m.group(2)));
            }
        }

        return null;
    }

}
