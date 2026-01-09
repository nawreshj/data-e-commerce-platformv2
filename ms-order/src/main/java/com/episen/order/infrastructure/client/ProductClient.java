package com.episen.order.infrastructure.client;

import com.episen.order.application.dto.ProductDto;
import com.episen.order.application.dto.StockUpdateRequestDto;
import com.episen.order.infrastructure.exception.ServiceUnauthorizedException;
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
 *  - si ms-product refuse le token (401 Unauthorized), on remonte une exception dédiée
 *    pour pouvoir renvoyer une réponse cohérente côté ms-order.
 *
 * Particularités :
 *  - n’expose aucune logique métier : simple façade réseau ;
 *  - l’URL de base est injectée via application.yml (bonne pratique) ;
 *  - gère proprement les erreurs réseau et les cas d’authentification refusée.
 *
 * Objectif :
 *  isoler toutes les interactions HTTP ici, afin de garder OrderService propre
 *  et indépendant de la couche transport/sécurité inter-services.
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
     * Endpoint cible : GET /api/v1/products/{id}
     *
     * JWT :
     *  - récupère le header Authorization de la requête entrante (client -> ms-order)
     *  - le propage vers ms-product (order -> product)
     *
     * Erreurs :
     *  - 401 renvoyé par ms-product => ServiceUnauthorizedException (token rejeté)
     */
    public ProductDto getProductById(Long productId) {
        String url = productBaseUrl + "/api/v1/products/" + productId;

        // Propage Authorization si présent dans la requête entrante
        HttpEntity<Void> entity = new HttpEntity<>(buildAuthHeadersFromIncomingRequest());

        try {
            ResponseEntity<ProductDto> res =
                    restTemplate.exchange(url, HttpMethod.GET, entity, ProductDto.class);
            return res.getBody();

        } catch (HttpClientErrorException.Unauthorized ex) {
            // ms-product a rejeté le token
            throw new ServiceUnauthorizedException("PRODUCT_SERVICE");
        }
    }

    /**
     * Met à jour le stock d’un produit sur ms-product.
     *
     * Endpoint cible : PATCH /api/v1/products/{id}/stock
     *
     * JWT :
     *  - propage le header Authorization comme pour getProductById().
     *
     * Erreurs :
     *  - 401 renvoyé par ms-product => ServiceUnauthorizedException
     *  - erreurs réseau/timeout/5xx => IllegalStateException (service indisponible)
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
            // ms-product a rejeté le token
            throw new ServiceUnauthorizedException("PRODUCT_SERVICE");

        } catch (RestClientException ex) {
            // timeout, DNS, 5xx, etc.
            log.error("Erreur Product service (stock update) productId={}", productId, ex);
            throw new IllegalStateException("Service Product indisponible.", ex);
        }
    }

    /**
     * Construit les headers HTTP à envoyer vers ms-product.
     *
     * Objectif principal :
     *  - récupérer le header "Authorization" de la requête entrante (si présent)
     *  - le recopier tel quel dans la requête sortante
     *
     * Remarque :
     *  - RequestContextHolder fonctionne car l'appel se fait dans le thread de la requête HTTP.
     *  - si attrs == null, on n'ajoute pas Authorization (cas rare : exécution hors contexte web).
     */
    private HttpHeaders buildAuthHeadersFromIncomingRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            String auth = attrs.getRequest().getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                // On propage exactement ce que le client a envoyé (Bearer <token>)
                headers.set("Authorization", auth);
            }
        }

        return headers;
    }
}