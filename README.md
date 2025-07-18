# OSHapp Backend - Lancement Automatique

## Lancement rapide (tout Dockerisé)

1. **Prérequis** :
   - [Docker](https://www.docker.com/products/docker-desktop) installé
   - [Docker Compose](https://docs.docker.com/compose/) installé

2. **Lancer tout l'écosystème** :
   ```sh
   cd backend
   docker-compose up --build
   ```
   Cela démarre :
   - Le backend Spring Boot (API REST)
   - Keycloak (authentification, OAuth2, Google)
   - PostgreSQL (données structurées)
   - MongoDB (documents médicaux)
   - MinIO (stockage fichiers)

3. **Accès aux services** :
   - **API Backend** : https://localhost:8443 (Swagger : https://localhost:8443/swagger-ui.html)
   - **Keycloak** : http://localhost:8080 (admin/admin)
   - **MinIO** : http://localhost:9001 (minioadmin/minioadmin)
   - **PostgreSQL** : localhost:5432 (oshuser/oshpass)
   - **MongoDB** : localhost:27017

4. **Keycloak - Import du Realm**
   - Aller sur http://localhost:8080 > Administration
   - Importer le fichier `infra/keycloak/realms/oshapp-realm.json`
   - Ajouter vos credentials Google dans le provider Google du realm (clientId/clientSecret)
   - Le client `oshapp-backend` est déjà configuré

5. **MinIO - Initialisation des buckets**
   - Installer [mc (MinIO Client)](https://docs.min.io/docs/minio-client-quickstart-guide.html)
   - Lancer le script :
     ```sh
     sh infra/minio-init.sh
     ```
   - Buckets créés : `documents`, `rapports`, `fichiers-medicaux`

6. **Scénario de prise de rendez-vous**
   - **POST /api/v1/appointments** : Demande de RDV par le salarié
   - **POST /api/v1/appointments/obligatory** : RDV obligatoire par le RH
   - **Notifications** : Email, SMS, notification interne (selon config)
   - **Rôles** : RH, Médecin, Infirmier, Salarié, HSE, N+1, N+2

7. **Sécurité**
   - Authentification OAuth2/JWT via Keycloak (Google ou login classique)
   - Gestion des rôles et permissions automatique
   - HTTPS activé (port 8443)

8. **Documentation API**
   - Swagger UI : https://localhost:8443/swagger-ui.html
   - OpenAPI : https://localhost:8443/api-docs

9. **Développement**
   - Code source dans `src/`
   - Configs dans `src/main/resources/`
   - Ajoutez vos entités, services, contrôleurs selon vos besoins métier

---

## Pour toute personnalisation
- Modifier les rôles, clients ou providers dans `infra/keycloak/realms/oshapp-realm.json`
- Modifier la configuration des notifications dans `application.yaml` ou `application-docker.yml`
- Ajouter des buckets MinIO dans `infra/minio-init.sh`

---

## Contact
Pour toute question, contactez l'équipe OSHapp. 