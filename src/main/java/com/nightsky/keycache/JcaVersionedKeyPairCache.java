package com.nightsky.keycache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteStreams;
import com.nightsky.keycache.builder.JcaVersionedKeyPairCacheBuilder;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class JcaVersionedKeyPairCache implements VersionedKeyPairCache {

    private static final String KEY_NAME_PATTERN = "([\\p{Alnum}_-]+)-v(\\d+)";

    private final Logger log;

    private LoadingCache<String, VersionedKeyPair> cache;

    private Duration expireAfterWrite;

    private Resource keyStoreResource;

    private Resource keyStorePasswordResource;

    private String keyStorePassword;

    private Map<String, Resource> keyPasswords;

    private String keyStoreType;

    private String keyNamePattern;

    public JcaVersionedKeyPairCache() {
        log = LoggerFactory.getLogger(getClass());
        keyNamePattern = KEY_NAME_PATTERN;
        expireAfterWrite = Duration.ofMinutes(30L);
    }

    public void initialize() {
        CacheLoader<String, VersionedKeyPair> loader = new CacheLoader<String, VersionedKeyPair>() {
            @Override
            public VersionedKeyPair load(String id) throws Exception {
                return retrieveVersionedKeyPair(id);
            }
        };

        cache = CacheBuilder.newBuilder().expireAfterWrite(expireAfterWrite).build(loader);
    }

    public static JcaVersionedKeyPairCacheBuilder builder() {
        return new JcaVersionedKeyPairCacheBuilder();
    }

    @Override
    public VersionedKeyPair getKeyPair(String keyPairName) {
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

                Key key = keyStore.getKey(alias, keyPassword.toCharArray());

                if ( key instanceof PrivateKey ) {
                    Certificate cert = keyStore.getCertificate(alias);
                    PublicKey publicKey = cert.getPublicKey();
                    KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
                    return new DefaultVersionedKeyPair(keyPair, Integer.parseInt(m.group(2)));
                }
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

    /**
     * @return the expireAfterWrite
     */
    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    /**
     * @param expireAfterWrite the expireAfterWrite to set
     */
    public void setExpireAfterWrite(Duration expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

}
