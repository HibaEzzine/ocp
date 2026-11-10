# 08 — Questions possibles du jury et réponses

## Contexte et besoin

### 1. Quel est le sujet de votre PFA?
La conception d'une application d'aide à la décision pour gérer les pièces de rechange, détecter des consommations inhabituelles, prévoir la consommation et proposer des quantités de réapprovisionnement.

### 2. Quelle est la problématique industrielle?
Éviter autant que possible les ruptures et les stocks excessifs en rapprochant les données de stock, consommation et maintenance. Les gains concrets doivent encore être mesurés avec le service métier.

### 3. Pourquoi les pièces de rechange sont-elles importantes?
Elles sont nécessaires à la maintenance des équipements. Une indisponibilité peut compliquer une intervention; trop de stock immobilise des ressources.

### 4. Qui sont les utilisateurs prévus?
Le code distingue un administrateur et un consultant. Les écrans de gestion sont destinés à l'administrateur; le consultant a accès au tableau de bord et au profil dans les routes frontend.

### 5. Le projet est-il une solution OCP en production?
Le dépôt décrit une application locale et des services configurés sur localhost. Il ne prouve pas un déploiement industriel ou une validation officielle par OCP.

## Fonctionnalités

### 6. Quelles sont les principales fonctions terminées?
Gestion des DA/reportings, import/export Excel, gestion d'articles, moteur de règles d'approvisionnement, suivi des historiques et appels IA de prévision/anomalie.

### 7. Le système achète-t-il automatiquement les pièces?
Non. Il stocke et affiche une proposition de quantité; aucune émission automatique d'achat ou intégration SAP n'est démontrée.

### 8. Comment importer les référentiels?
Par un téléversement Excel multipart depuis l'écran d'import. Le backend classe les feuilles selon les en-têtes et leur nom, puis les mappe aux entités.

### 9. Quels formats d'import sont acceptés?
Le code valide les extensions `.xls` et `.xlsx`, avec une limite de taille annoncée de 500 Mo. La compatibilité réelle varie selon le chemin de lecture; le streaming utilise le format XLSX.

### 10. Les feuilles BOM Ammoniac, Soufre et Export sont-elles fournies?
Non, aucun classeur de ces BOM n'a été trouvé dans le dépôt. Le backend possède une entité BOM et des branches d'import génériques, mais cela ne prouve pas ces fichiers.

### 11. Le stock hebdomadaire est-il présent?
Pas sous forme de fichier exploitable. Le classeur `backend/temp-stock.xlsx` est un gabarit presque vide.

### 12. Comment les demandes d'achat sont-elles suivies?
Les demandes sont créées, modifiées, filtrées, exportées et historisées par les services backend et les tables DA associées.

## Architecture et technologie

### 13. Quelles sont les couches de l'architecture?
React/TypeScript pour le web, Spring Boot pour l'API et les règles métier, MySQL pour la persistance, FastAPI/Python pour la prévision et la détection statistique.

### 14. Pourquoi séparer l'IA en microservice?
Cela isole les dépendances Python de l'application Java et expose des endpoints HTTP dédiés. Cela ajoute aussi une dépendance réseau et un service à exploiter.

### 15. Comment les systèmes communiquent-ils?
Le frontend appelle Spring en HTTP/JSON avec Axios; Spring appelle FastAPI avec HTTP/JSON. Les données persistent dans MySQL via JPA.

### 16. Quelles sont les technologies du backend?
Java 21, Spring Boot 3.4.2, Spring Web, Security, Data JPA, validation, Apache POI et MySQL.

### 17. Quelles sont celles du frontend?
React, TypeScript, Vite, Axios, React Router et Recharts.

### 18. Où se trouve la documentation des API?
Le backend configure OpenAPI/Swagger, accessible selon le README à `/swagger-ui.html`.

### 19. Quelle est la base de données?
MySQL par défaut, base `pdr_db`, configurable par variables d'environnement. Les tests déclarent H2.

### 20. Comment le schéma SQL évolue-t-il?
Hibernate est configuré en `ddl-auto=update`. Des migrations versionnées ne sont pas visibles; pour une mise en production, une stratégie de migration explicite serait préférable.

## Sécurité

### 21. Comment l'authentification est-elle faite?
Le backend authentifie le compte et émet un JWT ainsi qu'un refresh token. Un filtre vérifie le bearer token à chaque requête protégée.

### 22. Les mots de passe sont-ils stockés en clair?
Le code configure BCrypt pour encoder les mots de passe.

### 23. Le contrôle des rôles est-il sécurisé côté serveur?
La navigation frontend vérifie les rôles, mais les sources backend consultées ne montrent pas d'annotations de contrôle d'accès par rôle. Il faut donc renforcer ou démontrer ce contrôle côté API.

### 24. Quelles améliorations de sécurité sont nécessaires?
Restreindre CORS, gérer le secret JWT par configuration sûre, revoir les comptes de démonstration, appliquer les autorisations par rôle côté serveur et protéger les jetons contre les scripts du navigateur.

### 25. Pourquoi désactiver CSRF?
Le backend utilise une API stateless avec JWT dans l'en-tête Authorization. La désactivation est cohérente avec ce choix d'architecture, à condition de ne pas utiliser des cookies d'authentification sans protection CSRF.

## Intelligence artificielle

### 26. Quel algorithme est utilisé pour prévoir?
Un `RandomForestRegressor` scikit-learn avec 50 arbres et une profondeur maximale de 5.

### 27. Que prédit-il?
La consommation mensuelle suivante d'un article, à partir de son historique mensuel et de features calculées.

### 28. Quelles sont les principales features?
Retards de consommation (lags 1 à 3), moyennes 3/6 mois, minimum, maximum, volatilité, pente de tendance, variation et quantité installée/ratio.

### 29. Comment les mouvements sont-ils regroupés?
Les consommations sont agrégées par mois pour chaque code SAP. Les valeurs reçues du flux backend sont passées en valeur absolue avant la prévision.

### 30. Quel historique minimum faut-il?
Trois mois pour produire une prédiction d'un article; moins de trois mois donne `INSUFFISANT`. L'entraînement en fenêtres requiert quatre mois par article pour produire au moins une cible.

### 31. Comment fonctionne la détection des anomalies?
Elle compare le dernier mois à la moyenne et l'écart-type de l'historique : au-dessus de moyenne + 2 écarts-types, inhabituelle; au-dessus de +3, critique.

### 32. La détection d'anomalies utilise-t-elle du machine learning?
Non dans le code observé. C'est une règle statistique sans entraînement de modèle.

### 33. Comment le score de risque est-il calculé?
Par une combinaison pondérée de proximité au seuil, tendance, variation et volatilité, puis classée de faible à critique.

### 34. Le seuil 200 % représente quoi?
Dans la prévision, le seuil est codé comme deux fois la quantité installée. Sa pertinence opérationnelle doit être confirmée par le métier.

### 35. Existe-t-il un jeu test?
Non, pas dans le code d'entraînement. Les métriques sont calculées sur les mêmes fenêtres que celles utilisées pour ajuster le modèle.

### 36. Les métriques du fichier metadata prouvent-elles une précision de 91 %?
Non. Le R² indiqué est 0,91 sur des données d'entraînement et n'est pas une « précision ». Il ne démontre pas la qualité sur des données inconnues.

### 37. Quels sont les résultats métriques effectivement présents?
Le fichier de métadonnées annonce MAE 0,29, RMSE 0,33, R² 0,91, baseline MAE/RMSE 2,0 et quatre échantillons. Ces valeurs doivent être présentées avec la réserve d'évaluation in-sample.

### 38. Pourquoi les quatre échantillons sont-ils préoccupants?
Un très petit échantillon ne permet pas de généraliser. De plus, la feuille historique actuelle laisse estimer environ 314 fenêtres mensuelles; l'écart avec le metadata demande une vérification de provenance.

### 39. Quel est le rôle de la baseline?
Le code compare la forêt à la moyenne des trois derniers mois. La comparaison est utile comme point de référence, mais elle est calculée sur les mêmes fenêtres que l'entraînement.

### 40. Que se passe-t-il si le service Python est arrêté?
La prévision backend renvoie une erreur; l'anomalie utilise un repli statistique local Java. Le statut ML renvoyé par le contrôleur est toutefois constant et ne suffit pas à détecter cette panne.

## Approvisionnement, données et limites

### 41. Quelles règles de réapprovisionnement existent?
MIN_MAX pour groupes prioritaires, PLANIFIE pour les autres avec backlog positif, et SUR_DEMANDE pour les autres sans backlog. Elles sont testées dans cet ordre.

### 42. Quelle formule calcule la quantité à lancer?
Le maximum entre zéro et seuil maximum moins stock total.

### 43. Le moteur tient-il compte des besoins en cours?
Pas dans les conditions ni dans la formule des trois règles centrales, même si les besoins en cours peuvent être importés et stockés.

### 44. Le moteur tient-il compte des quantités BOM?
L'import et le stockage existent, mais le calcul de réapprovisionnement examiné n'utilise pas ces quantités.

### 45. Que se passe-t-il à chaque analyse globale?
Le service supprime d'abord les anciens résultats d'approvisionnement et anomalies, puis recalcule les articles. Il conserve un historique de traitement synthétique.

### 46. Quelles données Excel sont livrées?
Un historique de consommation en deux feuilles et un fichier de stock gabarit presque vide. Aucun fichier indépendant pour backlog OT, BOM métier ou besoins en cours n'a été trouvé.

### 47. Pourquoi les deux feuilles d'historique ne sont-elles pas interchangeables?
La première a des dates et des mouvements; la deuxième contient des consommations annuelles par année. Le modèle a besoin de points datés mensuels pour construire des séquences.

### 48. Quelles sont les limites du prétraitement?
Les mois sans mouvement ne sont pas réinsérés explicitement comme des zéros; les valeurs absolues peuvent changer la signification d'une sortie négative; les dates invalides sont ignorées.

### 49. Comment améliorer l'évaluation du modèle?
Faire un split temporel par date, entraîner sur les périodes anciennes, tester sur les périodes ultérieures, comparer à une baseline naïve, calculer MAE/RMSE et analyser l'erreur par article/groupe.

### 50. Quelles sont les perspectives prioritaires?
Valider les règles avec JPH I/J, compléter les référentiels, sécuriser les API, effectuer une validation temporelle, ajouter des fonctions de stock net/délais et mesurer les KPI en pilote avant toute intégration ERP.
