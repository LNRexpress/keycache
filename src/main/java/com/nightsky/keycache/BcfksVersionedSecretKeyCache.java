package com.nightsky.keycache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteStreams;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class BcfksVersionedSecretKeyCache implements VersionedSecretKeyCache {

    private final LoadingCache<String, VersionedSecretKey> cache;

    private final Logger log;

    private final Resource keystoreResource;

    private final String keystorePassword;

    private final Map<String, Resource> keyPasswords;

    public BcfksVersionedSecretKeyCache(Resource keystoreResource, String keystorePassword, Map<String, Resource> keyPasswords) {
        this.keystoreResource = keystoreResource;
        this.keystorePassword = keystorePassword;
        this.keyPasswords = keyPasswords;

        CacheLoader<String, VersionedSecretKey> loader = new CacheLoader<String, VersionedSecretKey>() {
            @Override
            public VersionedSecretKey load(String id) throws Exception {
                return retrieveVersionedSecretKey(id);
            }
        };

        cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(30L)).build(loader);
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public VersionedSecretKey getKey(String keyName) {
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
                    if ( name.equals(keyName) && version > currentVersion ) {
                        currentVersion = version;
                    }
                }
            }

            return cache.get(String.format("%s-v%d", keyName, currentVersion));
        } catch (Exception e) {
            log.error("Failed to retrieve key from cache", e);
            return null;
        }
    }

    @Override
    public VersionedSecretKey getKey(String keyName, Integer keyVersion) {
        if ( keyVersion == null )
            return getKey(keyName);

        try {
            return cache.get(String.format("%s-v%d", keyName, keyVersion));
        } catch (Exception e) {
            log.error("Failed to retrieve key from cache", e);
            return null;
        }
    }

    private VersionedSecretKey retrieveVersionedSecretKey(String alias) throws Exception {
        Pattern pattern = Pattern.compile("([\\p{Alnum}_-]+)-v(\\d+)");
        Matcher m = pattern.matcher(alias);

        if ( m.matches() ) {
            KeyStore keyStore = KeyStore.getInstance("BCFKS");
            keyStore.load(keystoreResource.getInputStream(), keystorePassword.toCharArray());

            String keyPassword = new String(
                ByteStreams.toByteArray(keyPasswords.get(alias).getInputStream()),
                StandardCharsets.UTF_8);

            return new DefaultVersionedSecretKey((SecretKey) keyStore.getKey(alias, keyPassword.toCharArray()), Integer.parseInt(m.group(2)));
        } else {
            return null;
        }
    }

}
