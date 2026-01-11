package com.episen.ms_product.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaConfig {

    @Bean
    public PublicKey publicKey() throws Exception {
        var resource = getClass().getResource("/public_key.pem");
        if (resource == null) {
            throw new RuntimeException("Fichier public_key.pem introuvable dans les ressources !");
        }

        String key = new String(Files.readAllBytes(Paths.get(resource.toURI())));

        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);

        return keyFactory.generatePublic(keySpec);
    }
}