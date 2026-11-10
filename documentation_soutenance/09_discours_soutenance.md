# 09 — Proposition de discours de soutenance

> Durée indicative : environ 8 à 10 minutes. Adapter la durée, le vocabulaire métier et les résultats après validation avec l'encadrant et le service JPH I/J. Les éléments entre crochets sont à compléter uniquement avec des faits confirmés.

## Introduction

Bonjour Mesdames et Messieurs les membres du jury.

Mon projet de fin d'année porte sur la conception et le développement d'un système d'aide à la décision pour la gestion des pièces de rechange. Il s'intéresse à trois questions : comment centraliser les données de pièces, comment repérer une consommation inhabituelle et comment proposer un réapprovisionnement cohérent avec le stock et les règles métier.

Le projet s'inscrit dans le contexte industriel OCP et du service JPH I/J, tel qu'il m'a été présenté. Les pièces de rechange jouent un rôle important dans la maintenance : une indisponibilité peut compliquer une intervention, tandis qu'un stock trop important mobilise des ressources. L'objectif est donc d'améliorer la visibilité et d'aider l'utilisateur, sans automatiser la décision d'achat.

## Problématique et objectifs

La problématique peut se formuler ainsi : comment exploiter les historiques de consommation et les données de stock pour anticiper un besoin, détecter une variation inhabituelle et aider à proposer un réapprovisionnement explicable?

J'ai poursuivi plusieurs objectifs : gérer les articles et demandes d'achat, importer les fichiers Excel, appliquer des règles MIN_MAX, PLANIFIE et SUR_DEMANDE, calculer une prévision mensuelle et afficher les alertes dans une interface.

## État de l'art et choix

Pour la prévision de séries temporelles, des approches simples comme la moyenne mobile peuvent servir de référence; des modèles d'ensemble comme la forêt aléatoire peuvent apprendre des relations non linéaires à partir de features. Pour la détection d'anomalies, une méthode statistique fondée sur moyenne et écart-type est simple à expliquer, mais suppose que l'historique soit suffisamment représentatif.

Le code du projet met en œuvre ces deux familles de façon distincte : une Random Forest pour la prévision et des seuils statistiques pour les consommations inhabituelles. Je ne présente donc pas la détection comme un modèle ML entraîné. La baseline de moyenne mobile des trois derniers mois est présente dans le code de prévision.

## Méthodologie

J'ai structuré l'application en trois composants. Le frontend est développé avec React et TypeScript. Le backend Java Spring Boot expose les APIs REST, applique les règles et persiste les informations dans MySQL. Un microservice FastAPI en Python traite les prédictions et la détection statistique.

Les données sont importées par classeurs Excel. Les lignes de consommation sont regroupées par article et par mois. Le modèle de prévision utilise les retards, des moyennes récentes, la dispersion, une tendance et des informations sur la quantité installée.

## Démonstration fonctionnelle

Le parcours commence par l'authentification. Un administrateur peut importer des référentiels PDR ou des demandes d'achat, consulter le tableau de bord, lancer le moteur d'approvisionnement et ouvrir l'analyse des anomalies et prédictions.

Pour l'approvisionnement, le backend calcule le stock total. Pour un groupe prioritaire en dessous du minimum, il applique la règle Min-Max. Pour les autres articles, il distingue la présence d'un backlog positif du cas sans backlog. La quantité proposée est le seuil maximal moins le stock, bornée à zéro.

La prévision est calculée pour le mois suivant si l'historique est suffisant et que le modèle est disponible. La détection statistique compare le dernier mois à la moyenne historique augmentée de deux ou trois écarts-types. Les résultats sont présentés avec leur niveau et une justification.

## Architecture

Le navigateur appelle l'API Spring en JSON avec un token JWT. Spring utilise JPA pour échanger avec MySQL. Pour la prévision, le service Java transmet l'historique à FastAPI. L'entraînement Python, lui, lit un fichier Excel dédié et persiste le modèle sous forme joblib.

Cette distinction est importante : les prédictions de l'application partent des historiques en base, tandis que le réentraînement du modèle Python repose sur le fichier local `ai/dataset/consommation_historique.xlsx`.

## Données et résultats observables

Le dépôt contient un classeur de consommation avec une feuille de mouvements datés et une feuille d'agrégats annuels; un deuxième classeur, de stock, est essentiellement un gabarit vide. Les autres fichiers annoncés dans le parcours d'import — BOM, backlog, stock complet — ne sont pas présents dans le dépôt examiné.

Les métadonnées du modèle indiquent un MAE de 0,29, un RMSE de 0,33 et un R² de 0,91 sur quatre échantillons. Je dois préciser que le code calcule ces valeurs sur les mêmes fenêtres utilisées pour entraîner la Random Forest. Elles ne constituent donc pas une mesure indépendante de la performance future. Le projet comporte des tests unitaires fonctionnels, mais pas de test temporel indépendant.

## Limites

Les limites principales sont la petite taille de l'évaluation enregistrée, l'absence de split temporel, la divergence entre le nombre de fenêtres estimé du classeur courant et la valeur de quatre échantillons metadata, et l'absence de quantité installée dans le dataset d'entraînement. Le moteur d'approvisionnement ne prend pas en compte le prix, le délai fournisseur, les besoins en cours, les commandes ouvertes ou le coût de rupture. Enfin, les protections par rôle doivent être confirmées côté backend, pas seulement dans l'interface.

## Conclusion et perspectives

En conclusion, le projet fournit une base applicative concrète : gestion des données, règles de réapprovisionnement, prévision par Random Forest et détection statistique. Il s'agit d'un système d'aide à la décision, pas encore d'un optimiseur autonome ni d'un outil de commande SAP.

Les perspectives sont de valider les règles avec les utilisateurs métier, compléter les données historiques, mettre en place une validation temporelle rigoureuse, sécuriser les autorisations serveur, intégrer les besoins nets et délais d'approvisionnement, puis mesurer l'impact par un pilote contrôlé.

Je vous remercie pour votre attention et je suis prêt à répondre à vos questions.
