package com.episen.order.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class PublicKeyConfig {

    @Bean
    public PublicKey publicKey() {
        try {
            String pem = new String(
                    new ClassPathResource("public_key.pem").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                     .replace("-----END PUBLIC KEY-----", "")
                     .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(pem);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(spec);

        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger public_key.pem", e);
        }
    }
}