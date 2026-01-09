package com.episen.order.infrastructure.client;

import com.episen.order.application.dto.UserDto;
import com.episen.order.infrastructure.exception.ServiceUnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Client REST responsable de la communication avec le microservice ms-users (ms-membership/users).
 *
 * Rôles :
 *  - interroger ms-users pour vérifier l'existence d’un utilisateur ;
 *  - isoler la logique réseau hors du service métier (OrderService).
 *
 * Sécurisation (TP2 - JWT) :
 *  - propage le JWT reçu par ms-order vers ms-users via le header :
 *      Authorization: Bearer <token>
 *  - si ms-users refuse le token (401 Unauthorized), on remonte une exception dédiée.
 *
 * Particularités :
 *  - l’URL de base est injectée via application.yml (bonne pratique) ;
 *  - aucune logique métier : uniquement transport HTTP + propagation du JWT.
 */
@Slf4j
@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userBaseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${app.clients.user.base-url}") String userBaseUrl) {
        this.restTemplate = restTemplate;
        this.userBaseUrl = userBaseUrl;
    }

    /**
     * Récupère un utilisateur depuis ms-users.
     *
     * Endpoint cible : GET /api/v1/users/{id}
     *
     * JWT :
     *  - récupère le header Authorization de la requête entrante (client -> ms-order)
     *  - le propage vers ms-users (order -> user)
     *
     * Erreurs :
     *  - 401 renvoyé par ms-users => ServiceUnauthorizedException (token rejeté)
     *  - les autres exceptions HTTP sont gérées plus haut (OrderService) selon ton choix
     *    (ex: 404 => UserNotFoundException, etc.)
     */
    public UserDto getUserById(Long id) {
        String url = userBaseUrl + "/api/v1/users/" + id;

        HttpEntity<Void> entity = new HttpEntity<>(buildAuthHeadersFromIncomingRequest());

        try {
            ResponseEntity<UserDto> res =
                    restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);
            return res.getBody();

        } catch (HttpClientErrorException.Unauthorized ex) {
            // ms-users a rejeté le token
            throw new ServiceUnauthorizedException("USER_SERVICE");
        }
    }

    /**
     * Construit les headers HTTP à envoyer vers ms-users.
     * Objectif : recopier le header Authorization (Bearer <token>) si présent.
     */
    private HttpHeaders buildAuthHeadersFromIncomingRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            String auth = attrs.getRequest().getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                headers.set("Authorization", auth); // propagation tel quel
            }
        }
        return headers;
    }
}