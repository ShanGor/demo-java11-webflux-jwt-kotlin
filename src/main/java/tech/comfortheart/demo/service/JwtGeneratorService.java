package tech.comfortheart.demo.service;


import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;

/**
 * Token API filter
 */
@Service
public class JwtGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(JwtGeneratorService.class);
    private static final int EXPIRY_SECONDS = 600;

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${jwt.keystore.password}")
    String keystorePassword;
    @Value("${jwt.keystore.alias}")
    String keyAlias;
    @Value("${jwt.keystore.path}")
    String keystorePath;
    @Value("${jwt.keystore.type}")
    String keystoreType;

    public String generateJwt() {
        try {
            Date notBefore = new Date();
            Date issueAt = new Date();
            Date expiresAt = new Date(issueAt.getTime() + EXPIRY_SECONDS * 1000);

            return Jwts.builder()
                    .setNotBefore(notBefore)
                    .setExpiration(expiresAt)
                    .signWith(SignatureAlgorithm.ES256, getKey())
                    .compact();
        } catch (JwtException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException e) {
            logger.info("JWT generation failed: " + e.getMessage());
            return null;
        }
    }

    PrivateKey getKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        InputStream ins = resourceLoader.getResource(keystorePath).getInputStream();
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        keyStore.load(ins, keystorePassword.toCharArray());

        PrivateKey key = (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
        ins.close();
        return key;
    }
}
