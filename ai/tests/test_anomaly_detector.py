import unittest
import numpy as np
from pdr_schemas import AnomalyArticleInput, ConsommationPoint, AnomalyLevel
from anomaly_detector import (
    clean_and_aggregate_monthly_consumption,
    detect_anomaly_single_article,
    detect_anomalies_for_articles
)

class TestAnomalyDetector(unittest.TestCase):

    def test_cas_1_consommation_normale(self):
        """Cas 1 — Consommation récente inférieure ou égale au seuil (moyenne + 2*std) -> NORMAL"""
        article = AnomalyArticleInput(
            codeSAP="ART001",
            description="ROULEMENT A BILLES",
            historique=[
                ConsommationPoint(date="2026-01-15", quantite=10.0),
                ConsommationPoint(date="2026-02-15", quantite=12.0),
                ConsommationPoint(date="2026-03-15", quantite=11.0),
                ConsommationPoint(date="2026-04-15", quantite=10.0),
                ConsommationPoint(date="2026-05-15", quantite=11.0),
            ]
        )
        res = detect_anomaly_single_article(article)
        self.assertEqual(res.niveau, AnomalyLevel.NORMAL)
        self.assertFalse(res.anomalie)
        self.assertLessEqual(res.consommationRecente, res.seuil)

    def test_cas_2_consommation_inhabituelle(self):
        """Cas 2 — Consommation récente supérieure à moyenne + 2*std mais <= moyenne + 3*std -> INHABITUELLE"""
        # Exemple donné par l'utilisateur pour l'article TAMBOUR 80169568
        article = AnomalyArticleInput(
            codeSAP="80169568",
            description="TAMBOUR",
            historique=[
                ConsommationPoint(date="2026-01-15", quantite=2.0),
                ConsommationPoint(date="2026-02-15", quantite=3.0),
                ConsommationPoint(date="2026-03-15", quantite=2.0),
                ConsommationPoint(date="2026-04-15", quantite=4.0),
                ConsommationPoint(date="2026-05-15", quantite=5.0),
                ConsommationPoint(date="2026-06-15", quantite=8.0),
                ConsommationPoint(date="2026-07-15", quantite=12.0),
                ConsommationPoint(date="2026-08-15", quantite=18.0),
            ]
        )
        res = detect_anomaly_single_article(article)
        self.assertEqual(res.codeSAP, "80169568")
        self.assertEqual(res.consommationRecente, 18.0)
        self.assertEqual(res.moyenneHistorique, 6.75)
        self.assertEqual(res.niveau, AnomalyLevel.INHABITUELLE)
        self.assertTrue(res.anomalie)
        self.assertGreater(res.consommationRecente, res.seuil)
        self.assertIn("seuil statistique", res.justification)

    def test_cas_3_consommation_critique(self):
        """Cas 3 — Consommation récente supérieure à moyenne + 3*std -> CRITIQUE"""
        # Historique régulier de 11 mois à 2.0 puis saut brutal à 30.0
        historique = [
            ConsommationPoint(date=f"2025-{m:02d}-10", quantite=2.0)
            for m in range(1, 12)
        ]
        historique.append(ConsommationPoint(date="2025-12-10", quantite=30.0))

        article = AnomalyArticleInput(
            codeSAP="ART_CRITIQUE",
            description="JOINT D ETANCHEITE",
            historique=historique
        )
        res = detect_anomaly_single_article(article)
        self.assertEqual(res.niveau, AnomalyLevel.CRITIQUE)
        self.assertTrue(res.anomalie)
        self.assertIn("critique", res.justification.lower())

    def test_cas_4_historique_insuffisant(self):
        """Cas 4 — Moins de 3 mois d'historique -> NON_EVALUABLE"""
        article = AnomalyArticleInput(
            codeSAP="ART_NOUVEAU",
            description="PIECE RECENTE",
            historique=[
                ConsommationPoint(date="2026-07-01", quantite=5.0),
                ConsommationPoint(date="2026-08-01", quantite=8.0),
            ]
        )
        res = detect_anomaly_single_article(article)
        self.assertEqual(res.niveau, AnomalyLevel.NON_EVALUABLE)
        self.assertFalse(res.anomalie)
        self.assertEqual(res.augmentationPourcentage, 0.0)
        self.assertIn("insuffisant", res.justification.lower())

    def test_cas_5_moyenne_egale_a_zero(self):
        """Cas 5 — Moyenne égale à zéro : aucune division par zéro ne doit se produire"""
        article = AnomalyArticleInput(
            codeSAP="ART_ZERO",
            description="PIECE SANS CONSOMMATION",
            historique=[
                ConsommationPoint(date="2026-01-01", quantite=0.0),
                ConsommationPoint(date="2026-02-01", quantite=0.0),
                ConsommationPoint(date="2026-03-01", quantite=0.0),
            ]
        )
        res = detect_anomaly_single_article(article)
        self.assertEqual(res.moyenneHistorique, 0.0)
        self.assertEqual(res.augmentationPourcentage, 0.0)
        self.assertEqual(res.niveau, AnomalyLevel.NORMAL)
        self.assertFalse(res.anomalie)

    def test_gestion_donnees_sap_agregation_et_negatifs(self):
        """Cas 6 — Gestion des données SAP : plusieurs sorties le même mois, valeurs négatives"""
        article = AnomalyArticleInput(
            codeSAP="ART_SAP",
            description="COURROIE TRAPEZOIDALE",
            historique=[
                # Mois 1 : deux sorties négatives SAP (-2 et -3) -> Somme mois 1 = 5
                ConsommationPoint(date="2026-01-05", quantite=-2.0),
                ConsommationPoint(date="2026-01-20", quantite=-3.0),
                # Mois 2 : une sortie de 5 -> Somme mois 2 = 5
                ConsommationPoint(date="2026-02-14", quantite=5.0),
                # Mois 3 : une sortie de 5 -> Somme mois 3 = 5
                ConsommationPoint(date="2026-03-10", quantite=5.0),
                # Entrée avec date invalide ou quantité nulle (doivent être ignorées)
                ConsommationPoint(date="INVALID_DATE", quantite=10.0),
                ConsommationPoint(date="2026-04-01", quantite=None),
            ]
        )
        res = detect_anomaly_single_article(article)
        self.assertEqual(res.consommationRecente, 5.0)
        self.assertEqual(res.moyenneHistorique, 5.0)
        self.assertEqual(res.ecartType, 0.0)
        self.assertEqual(res.niveau, AnomalyLevel.NORMAL)

if __name__ == "__main__":
    unittest.main()
