# 06 — Analyse du moteur d'approvisionnement

## Nature de la fonctionnalité

Le code contient un **moteur de règles** qui classe un article dans un mode de réapprovisionnement et calcule une quantité indicative. Il ne contient pas d'optimisation combinatoire, d'optimisation sous contrainte, de calcul économique de lot, ni de commande automatique. Le mot « optimisation » du titre doit être présenté comme un objectif d'amélioration future ou comme une aide à la rationalisation, pas comme un algorithme démontré.

## Entrées de calcul

- `ArticlePDR`: groupe homogène, seuil minimum, seuil maximum, quantités installées.
- `Stock`: une ou plusieurs quantités, additionnées pour donner stock total.
- `BacklogOT`: présence d'une quantité non lancée positive.

Les BOM, consommations et besoins en cours sont importables et persistés, mais ne sont pas utilisés par les prédicats des trois règles. Le nombre d'OT et la quantité totale du backlog ne modifient pas la quantité calculée.

## Ordre et conditions exactes

Le service `MoteurApprovisionnementService.analyser()` teste les règles dans l'ordre MIN_MAX, PLANIFIE, SUR_DEMANDE. La première règle vraie gagne.

| Mode | Condition du prédicat | Priorité |
|---|---|---:|
| MIN_MAX | Groupe `CURATIF`, `CRITIQUE` ou `CONDITIONNEL`; stock total strictement inférieur à `seuilMin`. | 1 |
| PLANIFIE | Groupe non prioritaire; stock total sous `seuilMin`; au moins un OT a `quantiteNonLancee > 0`. | 2 |
| SUR_DEMANDE | Groupe non prioritaire; stock sous `seuilMin`; aucun enregistrement de backlog (`backlogOTs` vide). | 3 |

Dans les trois cas, quantité proposée :

```text
max(seuilMax - somme(quantités en stock), 0)
```

Un résultat est sauvegardé uniquement si la quantité obtenue est strictement supérieure à zéro. Si `seuilMax` est nul, la quantité est 0 et aucune proposition n'est persistée.

## Exemple concret

Supposons un article `CRITIQUE`, un stock total de 3, seuil minimum 5 et seuil maximum 8. Il passe la règle MIN_MAX; la proposition est `8 - 3 = 5` unités, priorité 1. La présence d'un backlog n'influence pas ce premier classement.

Pour un groupe non prioritaire avec stock 3 et les mêmes seuils, un OT ayant une quantité non lancée positive conduit au mode PLANIFIE (priorité 2). Sans aucune ligne de backlog, il passe en SUR_DEMANDE (priorité 3). La quantité reste 5 dans les deux cas.

## Chaîne API et persistance

1. L'administrateur déclenche `POST /api/analyse/executer`.
2. `MoteurApprovisionnementService.executerAnalyseGlobale()` supprime l'ensemble des résultats d'approvisionnement et anomalies précédents.
3. Charge les articles du référentiel.
4. Pour chaque article, tente le calcul d'approvisionnement et la détection statistique métier.
5. Une erreur sur un article est journalisée; les articles suivants sont traités.
6. Les résultats, anomalies et un `HistoriqueTraitement` sont persistés.
7. Le frontend recharge résumé, résultats et exports.

API de consultation : `GET /api/analyse/resultats?mode=...&search=...`, `GET /api/analyse/summary`, `GET /api/analyse/statistiques`; exports par mode sous `/api/excel` ou `/api/import-export`.

Classes et tables principales :

- `MoteurApprovisionnementService`, `RegleMinMaxService`, `ReglePlanifieService`, `RegleSurDemandeService`.
- `ArticlePDR`, `Stock`, `BacklogOT`, `ResultatApprovisionnement`, `HistoriqueTraitement`, `AnomalieConsommation`.
- UI : `frontend/src/pages/approvisionnement/ApprovisionnementPage.tsx`.

## Paramètres et décisions manquants pour une véritable optimisation

- Niveau de service/cible de disponibilité et coût de rupture.
- Délais de livraison, calendrier, lots minimums, prix et coûts de commande.
- Quantités commandées ou en transit, stock réservé et disponibilité réelle des emplacements.
- Prévision de consommation, saisonnalité et incertitude intégrées à la quantité cible.
- Consommations BOM ou besoins d'OT intégrés au calcul de la demande nette.
- Contraintes budgétaires, priorité équipement et optimisation entre articles.
- Mesure comparative sur ruptures, surstock, coût ou temps opérateur.

## Risques opérationnels/limites du code

- Recalcul global efface les résultats précédents avant de reconstruire les nouvelles propositions; il n'existe pas d'historique de versions de chaque résultat dans ce flux.
- La transaction externe englobe le calcul, mais les exceptions d'article sont attrapées dans la boucle et ne font pas échouer tout le processus. Un calcul partiel peut donc se terminer avec des erreurs individuelles consignées.
- Le calcul de quantité utilise le stock courant et seuil maximum, sans déduire demandes d'achat ouvertes, besoins en cours ou backlog.
- La règle Planifié n'exige qu'une ligne OT positive; elle ne vérifie pas que la quantité de l'OT correspond à l'article au-delà de son association.
- Les imports peuvent créer des stocks multiples; leur somme représente le stock total dans les règles, mais le traitement de doublons et de snapshots doit être contrôlé par l'opérateur.

## Sources principales

`backend/src/main/java/com/ocp/pdr/service/MoteurApprovisionnementService.java`; `RegleMinMaxService.java`; `ReglePlanifieService.java`; `RegleSurDemandeService.java`; `backend/src/main/java/com/ocp/pdr/model/ArticlePDR.java`; `Stock.java`; `BacklogOT.java`; `ResultatApprovisionnement.java`; `frontend/src/pages/approvisionnement/ApprovisionnementPage.tsx`.
