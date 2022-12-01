package com.nightsky.keycache.factory;

import com.nightsky.keycache.factory.exception.KeyPairCreationException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Chris
 */
public class KeyPairFactory {

    public static final String KEY_NAME = "test-key";

    public static Map<String, Resource> createRandomRsaKeyPairs(KeyStore keyStore, int count) {
        try {
            KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
            kg.initialize(1024);

            Map<String, Resource> keyPasswords = new HashMap<>();
            for (int i = 1; i <= count; i++) {
                String keyName = String.format("%s-v%d", KEY_NAME, i);
                String keyPassword = RandomStringUtils.randomAlphanumeric(16);
                keyPasswords.put(keyName,
                    new ByteArrayResource(keyPassword.getBytes(StandardCharsets.UTF_8)));
                KeyPair keyPair = kg.genKeyPair();
                PrivateKey privateKey = keyPair.getPrivate();
                PublicKey publicKey = keyPair.getPublic();

                // Create a self-signed certificate for the public key
                X500Name issuer = new X500Name("C=US, ST=NC, O=NightSky, CN=none@none.com");
                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WITHRSA").build(privateKey);
                LocalDateTime now = LocalDateTime.now().minusSeconds(1);

                JcaX509v3CertificateBuilder x509Builder = new JcaX509v3CertificateBuilder(
                    issuer,
                    new BigInteger(String.format("%d", i)),
                    Date.from(now.atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(now.plusYears(1).atZone(ZoneId.systemDefault()).toInstant()),
                    issuer,
                    publicKey);

                KeyStore.PrivateKeyEntry entry = new KeyStore.PrivateKeyEntry(
                    privateKey,
                    new Certificate[] {
                        new JcaX509CertificateConverter().getCertificate(x509Builder.build(contentSigner))
                    });
                KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(keyPassword.toCharArray());
                keyStore.setEntry(keyName, entry, param);
            }

            return keyPasswords;
        } catch (Exception e) {
            throw new KeyPairCreationException(e);
        }
    }

}
