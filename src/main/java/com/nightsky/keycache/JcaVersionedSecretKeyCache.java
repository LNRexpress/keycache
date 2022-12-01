package com.nightsky.keycache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteStreams;
import com.nightsky.keycache.builder.JcaVersionedSecretKeyCacheBuilder;
import java.io.InputStream;
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
public class JcaVersionedSecretKeyCache implements VersionedSecretKeyCache {

    private static final String KEY_NAME_PATTERN = "([\\p{Alnum}_-]+)-v(\\d+)";

    private final LoadingCache<String, VersionedSecretKey> cache;

    private final Logger log;

    private Resource keyStoreResource;

    private Resource keyStorePasswordResource;

    private String keyStorePassword;

    private Map<String, Resource> keyPasswords;

    private String keyStoreType;

    private String keyNamePattern;

    public JcaVersionedSecretKeyCache() {
        CacheLoader<String, VersionedSecretKey> loader = new CacheLoader<String, VersionedSecretKey>() {
            @Override
            public VersionedSecretKey load(String id) throws Exception {
                return retrieveVersionedSecretKey(id);
            }
        };

        cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(30L)).build(loader);
        log = LoggerFactory.getLogger(getClass());

        keyNamePattern = KEY_NAME_PATTERN;
    }

    public static JcaVersionedSecretKeyCacheBuilder builder() {
        return new JcaVersionedSecretKeyCacheBuilder();
    }

    @Override
    public VersionedSecretKey getKey(String keyName) {
        checkKeyStorePassword();

        try ( InputStream keyStoreInputStream = keyStoreResource.getInputStream() )
        {
            Pattern pattern = Pattern.compile(keyNamePattern);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
            Iterator<String> aliases = Iterators.forEnumeration(keyStore.aliases());

            // Get the current version number of the key
            int currentVersion = -1;
            while ( aliases.hasNext() ) {
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
        checkKeyStorePassword();

        Pattern pattern = Pattern.compile(keyNamePattern);
        Matcher m = pattern.matcher(alias);

        if ( m.matches() ) {
            try ( InputStream keyStoreInputStream = keyStoreResource.getInputStream();
                  InputStream keyPasswordInputStream = keyPasswords.get(alias).getInputStream() )
            {
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());

                String keyPassword = new String(
                    ByteStreams.toByteArray(keyPasswordInputStream),
                    StandardCharsets.UTF_8);

                return new DefaultVersionedSecretKey((SecretKey) keyStore.getKey(alias, keyPassword.toCharArray()), Integer.parseInt(m.group(2)));
            } catch (Exception e) {
                throw e;
            }
        }

        return null;
    }

    private void checkKeyStorePassword() {
        if ( keyStorePassword != null || keyStorePasswordResource == null )
            return;

        try ( InputStream is = keyStorePasswordResource.getInputStream() ) {
            keyStorePassword = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load key store password from resource", e);
        }
    }

    /**
     * @return the keyStoreResource
     */
    public Resource getKeyStoreResource() {
        return keyStoreResource;
    }

    /**
     * @param keyStoreResource the keyStoreResource to set
     */
    public void setKeyStoreResource(Resource keyStoreResource) {
        this.keyStoreResource = keyStoreResource;
    }

    /**
     * @return the keyStorePasswordResource
     */
    public Resource getKeyStorePasswordResource() {
        return keyStorePasswordResource;
    }

    /**
     * @param keyStorePasswordResource the keyStorePasswordResource to set
     */
    public void setKeyStorePasswordResource(Resource keyStorePasswordResource) {
        this.keyStorePasswordResource = keyStorePasswordResource;
    }

    /**
     * @return the keyStorePassword
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * @param keyStorePassword the keyStorePassword to set
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * @return the keyPasswords
     */
    public Map<String, Resource> getKeyPasswords() {
        return keyPasswords;
    }

    /**
     * @param keyPasswords the keyPasswords to set
     */
    public void setKeyPasswords(Map<String, Resource> keyPasswords) {
        this.keyPasswords = keyPasswords;
    }

    /**
     * @return the keyStoreType
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * @param keyStoreType the keyStoreType to set
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * @return the keyNamePattern
     */
    public String getKeyNamePattern() {
        return keyNamePattern;
    }

    /**
     * @param keyNamePattern the keyNamePattern to set
     */
    public void setKeyNamePattern(String keyNamePattern) {
        this.keyNamePattern = keyNamePattern;
    }

}
