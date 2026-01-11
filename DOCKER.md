# DOCKER – Containerisation et Déploiement

## 1. Images Docker des microservices

Les trois microservices de la plateforme e-commerce ont été containerisés et publiés sur Docker Hub :
username de dockerhub : newsagesse
- `newsagesse/ecommerce-membership:1.0`
- `newsagesse/ecommerce-product:1.0`
- `newsagesse/ecommerce-order:1.0`

Chaque image correspond à un microservice indépendant, conformément à l’architecture microservices demandée.

/warning!!/
En raison des limitations du compte Docker Hub gratuit (un seul dépôt privé autorisé !), il n’a pas été possible de créer trois repositories privés distincts.

Les images Docker ont donc été publiées en public, uniquement à des fins pédagogiques, afin de respecter le découpage par microservice et permettre les tests du TP.

---

## 2. Dockerfiles

Chaque microservice dispose de son propre `Dockerfile` basé sur une image OpenJDK 

---

## 3. Scripts de build et de déploiement

Les scripts suivants sont disponibles dans le dossier "docker" 

### 3.1 build-all.sh

Ce script :
- Compile les trois microservices avec Maven
- Génère les fichiers JAR
- Construit les images Docker locales

Commande : ./build-all.sh

### publish-all.sh
Ce script :

1. Tag les images Docker locales
2. Publie les images sur Docker Hub

./publish-all.sh newsagesse(username)


### 3.3 deploy.sh

Ce script :

1. Récupère les images depuis Docker Hub
2. Lance l’ensemble de la plateforme avec Docker Compose

Commande :

./deploy.sh

### 4. Docker Compose

Le fichier docker-compose.yml permet d’orchestrer :
Les trois microservices (membership, product, order)
Prometheus (monitoring)
Grafana (visualisation)

Les images des microservices sont récupérées directement depuis Docker Hub.