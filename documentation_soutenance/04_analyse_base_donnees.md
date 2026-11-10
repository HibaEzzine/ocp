# 04 — Analyse de la base de données et des données disponibles

## Base et mapping

Le backend utilise MySQL par défaut, base `pdr_db`, connexion configurable par variables `SPRING_DATASOURCE_*`. Spring Data JPA mappe les entités vers des tables; Hibernate ajuste automatiquement le schéma avec `spring.jpa.hibernate.ddl-auto=update`. Il n'y a pas de migrations versionnées observées. Les tables effectives dépendent donc du lancement, de la version des entités et de l'état de la base locale.

## Entités et tables

| Table | Entité | Rôle / champs métier visibles |
|---|---|---|
| `article_pdr` | `ArticlePDR` | Référentiel article : code SAP unique, code Oracle, description, référence, UDM, quantité installée, catégorie, groupe homogène, seuil min/max. |
| `stock` | `Stock` | Quantité et date de stock, fichier source, date d'import; clé étrangère vers article. |
| `consommation` | `Consommation` | Quantité/date, OT, source fichier, date import, type de dataset et période import; clé vers article. |
| `bom` | `FicheBOM` | Pièces/nomenclatures reliées à l'article et au secteur. |
| `backlog_ot` | `BacklogOT` | Ordre de travail, quantité non lancée et article. |
| `besoin_en_cours` | `BesoinEnCours` | Besoins ouverts/quantité/statut associés à un article. |
| `resultat_approvisionnement` | `ResultatApprovisionnement` | Mode, quantité à lancer, priorité, justification et date d'analyse; référence article. |
| `anomalie_consommation` | `AnomalieConsommation` | Anomalies du moteur statistique métier; liée à article. |
| `secteur` | `Secteur` | Référentiel des secteurs; association à BOM/articles selon modèle. |
| `demande_achat` | `DemandeAchat` | Données de DA/reporting, statut, quantité, fournisseur, secteurs et responsables. |
| `historique_da` | `HistoriqueDA` | Changements associés à une DA et éventuellement à un utilisateur. |
| `utilisateur` | `Utilisateur` | Compte, email, mot de passe haché et rôle. |
| `import_donnees` | `ImportDonnees` | Nom, date, statut, lignes et résultats d'import, utilisateur facultatif. |
| `historique_traitement` | `HistoriqueTraitement` | Opération, date, statut et message; parfois relié à un import. |
| `import_excel_da` | `ImportExcelDA` | Entité spécifique d'import DA; son service existe mais aucun contrôleur correspondant n'a été repéré. |

Sources : `backend/src/main/java/com/ocp/pdr/model/` et les repositories homonymes dans `backend/src/main/java/com/ocp/pdr/repository/`.

## Relations essentielles

```text
ArticlePDR 1 ── * Stock
           1 ── * Consommation
           1 ── * FicheBOM ── 1 Secteur
           1 ── * BacklogOT
           1 ── * BesoinEnCours
           1 ── * ResultatApprovisionnement
           1 ── * AnomalieConsommation

DemandeAchat * ── 1 Utilisateur
DemandeAchat 1 ── * HistoriqueDA ── 0..1 Utilisateur
ImportDonnees 0..1 ── Utilisateur
ImportDonnees 1 ── * HistoriqueTraitement (selon mapping)
```

Les clés relationnelles et cardinalités sont celles des annotations JPA. Plusieurs collections de `ArticlePDR` sont configurées en cascade avec `orphanRemoval=true`; supprimer un article peut donc entraîner la suppression de ses objets enfants selon la transaction.

## Rôle des données dans les calculs

- `stock` est agrégé par somme pour décider si le stock est inférieur au seuil minimal.
- `backlog_ot` est utilisé pour vérifier qu'il existe une quantité non lancée strictement positive dans le mode planifié.
- `consommation` alimente l'anomalie et les appels IA.
- Les seuils min/max et le groupe homogène résident sur `article_pdr`.
- Les `besoin_en_cours` et lignes BOM sont importés/stockés, mais ils ne sont pas exploités dans les trois conditions centrales d'approvisionnement telles qu'écrites.
- Les prévisions de l'API sont envoyées à Python depuis les articles et leur historique en base; l'entraînement Python du endpoint `/train` lit un fichier Excel distinct.

## Fichiers de données réellement présents

### `ai/dataset/consommation_historique.xlsx`

Classeur de 20 837 344 octets, deux feuilles observées :

| Feuille | Dimension Excel | En-têtes |
|---|---:|---|
| `Feuil1` | `A1:F1043651` | `CODE SAP`, `DESCRIPTION`, `Q CONSOMMEE`, `UDM`, `N OT`, `DATE` |
| `Feuil2` | `A1:I12439` | `CODE SAP`, `DESCRIPTION`, `CATEGORIE ACHAT`, `PMP`, `CONS 2025`, `CONS 2024`, `CONS 2023`, `CONS 2022`, `CONS 2021` |

Inspection structurelle XML du classeur (pas une validation métier) : la première feuille comprend 4 392 lignes avec code/quantité/date parseables, 1 834 codes SAP distincts et des dates allant du 17 janvier 2025 au 22 août 2026. La dimension annoncée par Excel est supérieure à un million de lignes, mais la plupart des lignes correspondantes sont vides dans le fichier lu. Après regroupement par mois comme dans le pipeline, on observe 2 819 couples article-mois et environ 314 fenêtres d'apprentissage possibles (somme des mois disponibles par article moins les trois mois initiaux). Ces comptes sont calculés structurellement; ils ne sont pas des métriques de performance.

La première feuille correspond à un historique de sorties avec n° OT et date. La seconde fournit des totaux annuels, sans date de mouvement mensuelle; elle ne peut pas directement fournir les séquences mensuelles attendues par la Random Forest.

### `backend/temp-stock.xlsx`

Classeur de 4 909 octets, une feuille `Stock`, dimension `A1:D2`, en-têtes `Code SAP`, `Description`, `Q Stock`, `Secteur`. La seule ligne de données lue ne contient pas de code/description/secteur (une cellule de quantité isolée). Ce fichier est un gabarit de démonstration, pas un référentiel stock hebdomadaire exploitable.

### Fichiers absents du dépôt

Aucun autre `.xlsx`, `.xls` ou `.csv` de données métier n'a été trouvé hors environnements générés. Les sources contiennent des entités et branches d'import pour BOM, backlog OT et besoins en cours, mais aucun classeur distinct stock hebdomadaire, backlog, BOM Ammoniac, BOM Soufre, BOM Export ou besoins en cours n'est fourni. Leurs valeurs et leur qualité ne peuvent donc pas être analysées ici.

## Incohérences de données et d'import

1. Le fichier d'historique IA a deux feuilles incompatibles, dont une feuille agrégée par année.
2. Le chargement Python de l'entraînement lit le fichier Excel séparément de la base. La métadonnée de modèle annonce 4 échantillons, alors que la structure de Feuil1 offre une estimation d'environ 314 fenêtres après agrégation. Le modèle et les métadonnées sont donc potentiellement anciens ou issus d'un autre état de données; l'écart n'est pas expliqué par le dépôt.
3. Le classeur de consommation ne contient pas de quantité installée; le chargeur Python met alors cette feature à zéro. En prédiction sur les données MySQL, la quantité installée peut être non nulle : cette différence de distribution est à documenter/traiter.
4. L'import streaming classifie le classeur entier par son nom de fichier et doit gérer chaque feuille. Les colonnes de consommation annuelle de Feuil2 ne sont pas des lignes datées; son traitement est susceptible de signaler des lignes invalides ou de ne pas produire de données.
5. Les valeurs d'une extraction SAP peuvent être négatives pour des sorties; le pipeline IA convertit en valeur absolue, mais l'import Java et les règles de nettoyage ont des traitements qui varient selon le flux.

## Exemple de lecture conceptuelle

Une ligne de mouvement associe un code SAP à une quantité, une unité, un OT et une date. Pour la prévision, les mouvements du même article dans le même mois sont additionnés. Pour l'approvisionnement, la somme des enregistrements de stock de cet article est comparée à ses seuils. Les deux calculs utilisent des champs distincts et ne s'alimentent pas automatiquement l'un l'autre.

## Sources principales

`backend/src/main/java/com/ocp/pdr/model/`; `backend/src/main/java/com/ocp/pdr/repository/`; `backend/src/main/resources/application.properties`; `ai/dataset/consommation_historique.xlsx`; `backend/temp-stock.xlsx`; `ai/predictor.py`; `backend/src/main/java/com/ocp/pdr/service/ExcelImportService.java`.
