package com.episen.order.infrastructure.client;

import com.episen.order.application.dto.ProductDto;
import com.episen.order.application.dto.StockUpdateRequestDto;
import com.episen.order.infrastructure.exception.ServiceForbiddenException;
import com.episen.order.infrastructure.exception.ServiceUnauthorizedException;
import com.episen.order.infrastructure.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Client REST dédié à la communication avec le microservice ms-product.
 *
 * Rôles :
 *  - récupérer un produit par son identifiant (existence, prix, stock) ;
 *  - mettre à jour le stock d’un produit lors de la création d'une commande.
 *
 * Sécurisation (TP2 - JWT) :
 *  - propage le JWT reçu par ms-order vers ms-product via le header :
 *      Authorization: Bearer <token>
 *  - gère explicitement les erreurs d’authentification inter-services :
 *      * 401 Unauthorized  → token invalide
 *      * 403 Forbidden     → token expiré
 *      * autres erreurs    → service indisponible
 *
 * Particularités :
 *  - aucune logique métier : simple façade réseau ;
 *  - URL de base injectée via application.yml (bonne pratique) ;
 *  - permet à OrderService de rester indépendant du transport et de la sécurité.
 */
@Slf4j
@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productBaseUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${app.clients.product.base-url}") String productBaseUrl) {
        this.restTemplate = restTemplate;
        this.productBaseUrl = productBaseUrl;
    }

    /**
     * Récupère un produit depuis ms-product.
     *
     * Endpoint cible :
     *  GET /api/v1/products/{id}
     *
     * JWT :
     *  - récupère le header Authorization de la requête entrante (client → ms-order)
     *  - le propage vers ms-product (order → product)
     *
     * Gestion des erreurs :
     *  - 401 → token invalide → ServiceUnauthorizedException
     *  - 403 → token expiré → ServiceForbiddenException
     *  - autres erreurs → ServiceUnavailableException
     */
    public ProductDto getProductById(Long productId) {
        String url = productBaseUrl + "/api/v1/products/" + productId;

        HttpEntity<Void> entity =
                new HttpEntity<>(buildAuthHeadersFromIncomingRequest());

        try {
            ResponseEntity<ProductDto> res =
                    restTemplate.exchange(url, HttpMethod.GET, entity, ProductDto.class);
            return res.getBody();

        } catch (HttpClientErrorException.Unauthorized ex) {
            log.warn("PRODUCT_SERVICE rejected token (401) productId={}", productId);
            throw new ServiceUnauthorizedException("PRODUCT_SERVICE");

        } catch (HttpClientErrorException.Forbidden ex) {
            log.warn("PRODUCT_SERVICE rejected token (403) productId={}", productId);
            throw new ServiceForbiddenException("PRODUCT_SERVICE");

        } catch (RestClientException ex) {
            log.error("PRODUCT_SERVICE unavailable productId={}", productId, ex);
            throw new ServiceUnavailableException("PRODUCT_SERVICE");
        }
    }

    /**
     * Met à jour le stock d’un produit sur ms-product.
     *
     * Endpoint cible :
     *  PATCH /api/v1/products/{id}/stock
     *
     * JWT :
     *  - propage le header Authorization comme pour getProductById().
     *
     * Gestion des erreurs :
     *  - 401 → token invalide → ServiceUnauthorizedException
     *  - 403 → token expiré → ServiceForbiddenException
     *  - autres erreurs → ServiceUnavailableException
     */
    public void updateProductStock(Long productId, int newStock) {
        String url = productBaseUrl + "/api/v1/products/" + productId + "/stock";

        StockUpdateRequestDto body = StockUpdateRequestDto.builder()
                .newStock(newStock)
                .build();

        HttpEntity<StockUpdateRequestDto> entity =
                new HttpEntity<>(body, buildAuthHeadersFromIncomingRequest());

        try {
            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);

        } catch (HttpClientErrorException.Unauthorized ex) {
            log.warn("PRODUCT_SERVICE rejected token (401) stock update productId={}", productId);
            throw new ServiceUnauthorizedException("PRODUCT_SERVICE");

        } catch (HttpClientErrorException.Forbidden ex) {
            log.warn("PRODUCT_SERVICE rejected token (403) stock update productId={}", productId);
            throw new ServiceForbiddenException("PRODUCT_SERVICE");

        } catch (RestClientException ex) {
            log.error("PRODUCT_SERVICE unavailable (stock update) productId={}", productId, ex);
            throw new ServiceUnavailableException("PRODUCT_SERVICE");
        }
    }

    /**
     * Construit les headers HTTP à envoyer vers ms-product.
     *
     * Objectif :
     *  - récupérer le header "Authorization" de la requête entrante
     *  - le recopier tel quel (Bearer <token>) dans la requête sortante
     *
     * Remarque :
     *  - RequestContextHolder fonctionne car l’appel se fait
     *    dans le thread de la requête HTTP.
     */
    private HttpHeaders buildAuthHeadersFromIncomingRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            String auth = attrs.getRequest().getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                headers.set("Authorization", auth);
            }
        }

        return headers;
    }
}