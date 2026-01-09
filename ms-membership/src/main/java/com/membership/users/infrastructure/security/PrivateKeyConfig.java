package com.membership.users.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class PrivateKeyConfig {

    @Bean
    public PrivateKey privateKey() {
        try {
            String pem = new String(
                    new ClassPathResource("private_key.pem").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                     .replace("-----END PRIVATE KEY-----", "")
                     .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePrivate(spec);

        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger private_key.pem", e);
        }
    }
}