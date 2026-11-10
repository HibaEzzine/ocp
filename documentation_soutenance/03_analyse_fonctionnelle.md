# 03 — Analyse fonctionnelle

## Parcours et fonctionnalités

### 1. Connexion, profil et rôles

- **Objectif :** accéder à l'application et différencier administrateur et consultant.
- **Fonctionnement :** login par email/identifiant et mot de passe; le backend renvoie un JWT. Le frontend protège les routes et envoie le JWT par Axios. L'administrateur voit les écrans de gestion; le consultant est autorisé au tableau de bord et au profil.
- **Étapes :** `/login` → `POST /api/auth/login` → stockage du token → chargement de `/api/utilisateurs/me` → affichage de la route protégée.
- **Fichiers/classes :** `frontend/src/pages/auth/LoginPage.tsx`, `frontend/src/context/AuthContext.tsx`, `frontend/src/components/ProtectedRoute.tsx`, `frontend/src/routes/AppRoutes.tsx`; backend `AuthController`, `UtilisateurController`, `JwtUtils`, `AuthTokenFilter`, `UtilisateurService`, entité `Utilisateur`.
- **Tables :** `utilisateur`.
- **Exemple :** un administrateur ouvre l'écran d'approvisionnement; un consultant est renvoyé vers une page non autorisée pour les routes frontend marquées administrateur.
- **État/limite :** la restriction par rôle côté API n'est pas prouvée; le backend exige une authentification, mais aucune règle `@PreAuthorize` par rôle n'a été trouvée. Le seeding met à jour les comptes à chaque lancement avec des mots de passe prédéfinis dans le code : ne pas considérer ces valeurs comme sûres pour la production.

### 2. Tableau de bord

- **Objectif :** visualiser les demandes d'achat/reportings et leur répartition.
- **Fonctionnement réel :** charge les demandes, filtre et affiche indicateurs, graphiques et tableaux; permet l'export.
- **Étapes :** ouverture de `Dashboard.tsx` → appels `GET /api/reportings` et `GET /api/reportings/export` → rendu composants/graphes.
- **Fichiers/classes :** `frontend/src/pages/dashboard/Dashboard.tsx`, `frontend/src/components/charts/PdrTrendChart.tsx`, `frontend/src/services/reportingService.ts`; backend `DemandeAchatController`, `DemandeAchatService`, DTOs et `DemandeAchatMapper`.
- **Tables :** `demande_achat`, avec les lignes d'historique d'achat associées.
- **Exemple :** filtrer les demandes d'un secteur et télécharger le tableau au format demandé.
- **État :** implémenté en interface et API; pas d'indicateur d'amélioration de stock ou de résultat métier mesuré fourni avec le dépôt.

### 3. Demandes d'achat / reporting

- **Objectif :** consulter, rechercher, créer, modifier, supprimer et importer des demandes.
- **Fonctionnement :** CRUD REST; liste filtrée par statut, secteur, responsable, fournisseur, commande ou recherche; export Excel/CSV; journalisation des changements par le service.
- **Étapes :** saisir/filtrer → requête `/api/reportings` (alias `/api/demandes`) → mapper DTO/entity → mise à jour par `DemandeAchatService` → consultation de l'historique.
- **Fichiers/classes :** `frontend/src/pages/reporting/ReportingPage.tsx`, `frontend/src/services/reportingService.ts`, `backend/.../controller/DemandeAchatController.java`, `DemandeAchatService`, `HistoriqueDAService`, `DemandeAchatMapper`, `HistoriqueDAMapper`.
- **Tables :** `demande_achat`, `historique_da`, `utilisateur`.
- **Exemple :** changer le statut d'une DA puis vérifier l'entrée historique rattachée.
- **État :** implémenté; validation du classeur DA et erreurs visibles côté écran.

### 4. Import et export Excel

- **Objectif :** alimenter les référentiels depuis les extractions Excel et réutiliser les résultats.
- **Fonctionnement :** l'écran reçoit un fichier DA ou jusqu'à quatre fichiers PDR (stock, consommation, BOM, backlog); téléversement multipart vers Spring; Apache POI lit les feuilles, normalise les en-têtes et appelle des fonctions d'import dédiées. Le traitement peut être streaming pour les classeurs volumineux.
- **Étapes :** sélectionner → envoyer → valider extension/taille et éventuellement colonnes → classifier la feuille par nom et en-têtes → enregistrer les entités → produire bilan et historique.
- **Fichiers/classes :** `frontend/src/pages/import/ImportPage.tsx`, `frontend/src/services/importExportService.ts`, `backend/.../controller/ImportExportController.java`, `ExcelImportService`, `ExcelExportService`.
- **Tables :** selon le type : `article_pdr`, `stock`, `consommation`, `bom`, `backlog_ot`, `besoin_en_cours`, `import_donnees`, `historique_traitement`; import DA séparé vers `demande_achat` et `import_excel_da`.
- **Exemple :** un fichier stock avec `Code SAP` et `Q Stock` associe une quantité à l'article identifié par son code SAP.
- **État/limite :** fonction réalisée, mais la classification par noms/en-têtes dépend de la structure Excel. Les formats d'exemple cités dans les écrans ne sont pas tous livrés. Le classeur de consommation fourni a une seconde feuille annuelle sans colonne de date; ce n'est pas un historique mensuel compatible avec le pipeline.

### 5. Analyse d'approvisionnement

- **Objectif :** classer les articles en mode de réapprovisionnement et proposer une quantité.
- **Fonctionnement :** l'utilisateur clique « Exécuter le moteur »; le backend analyse tous les articles, supprime les précédents résultats/anomalies, applique Min-Max, sinon Planifié, sinon Sur demande, puis persiste les résultats positifs.
- **Étapes :** `POST /api/analyse/executer` → chargement articles → tests de règles → `quantité = max(seuilMax - stock total, 0)` → persistance → rafraîchissement du résumé et de la table.
- **Fichiers/classes :** `frontend/src/pages/approvisionnement/ApprovisionnementPage.tsx`, `frontend/src/services/analyseService.ts`, `MoteurApprovisionnementService`, `RegleMinMaxService`, `ReglePlanifieService`, `RegleSurDemandeService`.
- **Tables :** `article_pdr`, `stock`, `backlog_ot`, `resultat_approvisionnement`, `anomalie_consommation`, `historique_traitement`.
- **Exemple :** groupe prioritaire et stock total 3 sous seuil minimum 5 : Min-Max; si le seuil maximum vaut 8, la quantité suggérée est 5.
- **État/limite :** calcul déterministe et explicable, pas d'ordre d'achat émis. Le besoin en cours et la taille réelle du backlog ne sont pas retranchés de la quantité à lancer.

### 6. Anomalies et prévisions IA

- **Objectif :** afficher les anomalies statistiques et le risque/prévision de consommation.
- **Fonctionnement :** l'onglet « consommations inhabituelles » appelle le endpoint statistique; l'onglet prédiction appelle la génération IA; un autre onglet montre les anomalies de règle. Le frontend filtre les résultats et propose l'export prévu par son code.
- **Étapes :** sélection de l'onglet → `GET` ou `POST /api/analyse/...` → service Java construit historique article → appel FastAPI → conversion DTO → tableaux/indicateurs.
- **Fichiers/classes :** `frontend/src/pages/anomalies/AnomaliesPage.tsx`, `frontend/src/services/analyseService.ts`, backend `AnalyseController`, `IaPredictionService`, DTOs de réponse; Python `main.py`, `predictor.py`, `anomaly_detector.py`.
- **Tables :** `article_pdr`, `consommation`; l'anomalie statistique IA est calculée sur la requête et n'est pas l'entité persistée `anomalie_consommation`.
- **Exemple :** si la consommation du dernier mois dépasse moyenne historique + 2 écarts-types, classer INHABITUELLE; au-delà de +3 écarts-types, CRITIQUE.
- **État/limite :** détection IA répliquée par un repli statistique Java si Python échoue; prévision non disponible si modèle non entraîné ou service indisponible. Des erreurs de certaines pages sont seulement écrites dans la console.

### 7. Historique et traçabilité

- **Objectif :** consulter les événements des demandes et les traitements/imports.
- **Fonctionnement :** l'écran affiche les historiques renvoyés par `GET /api/historiques`; le backend possède aussi `historique_traitement` pour opérations globales/imports.
- **Fichiers/classes :** `frontend/src/pages/historique/HistoriquePage.tsx`, `HistoriqueController`, `HistoriqueDAService`, `HistoriqueDA`, `HistoriqueTraitement`.
- **Tables :** `historique_da`, `historique_traitement`, `import_donnees`.
- **Exemple :** consulter les changements associés à une demande via `GET /api/historiques/demande/{demandeId}`.
- **État :** consultation implémentée; la route historique affichée concerne surtout les DA. Une traçabilité métier complète de la décision de prévision n'est pas démontrée.

### 8. Profil et gestion des utilisateurs

- **Objectif :** mettre à jour les informations personnelles, le mot de passe et les comptes.
- **Fichiers/classes :** `frontend/src/pages/auth/ProfilePage.tsx`, `frontend/src/services/userService.ts`, `UtilisateurController`, `UtilisateurService`, `Utilisateur`.
- **API :** `/api/utilisateurs/me`, `/me/mot-de-passe`, collection et opérations de gestion.
- **État/limite :** formulaires et endpoints présents; contrôle des rôles côté API à renforcer ou démontrer.

## Fonctionnalités déclarées mais non raccordées

`frontend/src/services/analyseService.ts` déclare des appels pour `/analyse/anomalies/{id}`, `/analyse/ia/predictions/{codeSAP}` et leur historique; les mappings correspondants n'apparaissent pas dans `AnalyseController`, et les pages examinées ne les appellent pas. À présenter comme méthodes de service non utilisées / API absentes, pas comme fonctionnalité opérationnelle.

## Références principales

`frontend/src/pages/`; `frontend/src/services/`; `frontend/src/routes/AppRoutes.tsx`; `backend/src/main/java/com/ocp/pdr/controller/`; `backend/src/main/java/com/ocp/pdr/service/`.
