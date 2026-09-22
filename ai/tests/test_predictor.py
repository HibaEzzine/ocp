import tempfile
import unittest
from pathlib import Path

import pandas as pd

import predictor
from pdr_schemas import ArticleData, ConsommationPoint, RiskLevel
from predictor import (
    clean_and_aggregate_history,
    predict_all_articles,
    predict_articles_with_trained_model,
    predict_single_article,
)


class TestPredictor(unittest.TestCase):

    def test_consommation_normale_faible_risque(self):
        article = ArticleData(
            codeSAP="SAP-101",
            description="Roulement à billes standard",
            quantiteInstallee=10.0,
            historique=[
                ConsommationPoint(date="2024-01-15", quantite=4.0),
                ConsommationPoint(date="2024-02-15", quantite=4.0),
                ConsommationPoint(date="2024-03-15", quantite=4.0),
                ConsommationPoint(date="2024-04-15", quantite=4.0),
            ],
        )
        result = predict_single_article(article)
        self.assertEqual(result.niveauRisque, RiskLevel.FAIBLE)
        self.assertFalse(result.alerte)
        self.assertEqual(result.seuil, 20.0)
        self.assertLessEqual(result.scoreRisque, 39)
        self.assertIsNotNone(result.features)
        self.assertGreater(len(result.explications), 0)

    def test_consommation_predite_inhabituelle(self):
        article = ArticleData(
            codeSAP="SAP-102",
            quantiteInstallee=10.0,
            historique=[
                ConsommationPoint(date="2024-01-10", quantite=10.0),
                ConsommationPoint(date="2024-02-10", quantite=16.0),
                ConsommationPoint(date="2024-03-10", quantite=22.0),
                ConsommationPoint(date="2024-04-10", quantite=30.0),
            ],
        )
        result = predict_single_article(article)
        self.assertTrue(result.alerte)
        self.assertIn(result.niveauRisque, [RiskLevel.ELEVE, RiskLevel.CRITIQUE])
        self.assertGreaterEqual(result.scoreRisque, 70)
        self.assertGreater(result.consommationPredite, 20.0)

    def test_consommation_proche_du_seuil(self):
        article = ArticleData(
            codeSAP="SAP-103",
            quantiteInstallee=10.0,
            historique=[
                ConsommationPoint(date="2024-01-05", quantite=12.0),
                ConsommationPoint(date="2024-02-05", quantite=13.0),
                ConsommationPoint(date="2024-03-05", quantite=15.0),
            ],
        )
        result = predict_single_article(article)
        self.assertEqual(result.niveauRisque, RiskLevel.MOYEN)
        self.assertGreaterEqual(result.scoreRisque, 40)
        self.assertLessEqual(result.scoreRisque, 69)

    def test_historique_insuffisant(self):
        article = ArticleData(
            codeSAP="SAP-104",
            quantiteInstallee=5.0,
            historique=[
                ConsommationPoint(date="2024-01-01", quantite=4.0),
                ConsommationPoint(date="2024-02-01", quantite=5.0),
            ],
        )
        result = predict_single_article(article)
        self.assertEqual(result.statut, "INSUFFISANT")
        self.assertEqual(result.niveauRisque, RiskLevel.NON_EVALUABLE)
        self.assertFalse(result.alerte)
        self.assertIsNone(result.consommationPredite)

    def test_quantite_installee_nulle(self):
        article = ArticleData(
            codeSAP="SAP-105",
            quantiteInstallee=0.0,
            historique=[
                ConsommationPoint(date="2024-01-01", quantite=10.0),
                ConsommationPoint(date="2024-02-01", quantite=12.0),
                ConsommationPoint(date="2024-03-01", quantite=14.0),
            ],
        )
        result = predict_single_article(article)
        self.assertEqual(result.statut, "SANS_QUANTITE_INSTALLEE")
        self.assertEqual(result.niveauRisque, RiskLevel.NON_EVALUABLE)
        self.assertFalse(result.alerte)

    def test_nettoyage_donnees_aberrantes(self):
        points = [
            ConsommationPoint(date="2024-01-05", quantite=10.0),
            ConsommationPoint(date="2024-01-20", quantite=5.0),
            ConsommationPoint(date="2024-02-10", quantite=-8.0),
            ConsommationPoint(date="2024-02-15", quantite=0.0),
            ConsommationPoint(date="2024-03-01", quantite=12.0),
            ConsommationPoint(date=None, quantite=5.0),
        ]
        monthly = clean_and_aggregate_history(points)
        self.assertEqual(monthly, {"2024-01": 15.0, "2024-03": 12.0})

    def test_prediction_jamais_negative(self):
        article = ArticleData(
            codeSAP="SAP-106",
            quantiteInstallee=10.0,
            historique=[
                ConsommationPoint(date="2024-01-01", quantite=20.0),
                ConsommationPoint(date="2024-02-01", quantite=10.0),
                ConsommationPoint(date="2024-03-01", quantite=2.0),
            ],
        )
        result = predict_single_article(article)
        self.assertGreaterEqual(result.consommationPredite, 0.0)

    def test_prediction_globale_et_evaluation(self):
        articles = [
            ArticleData(
                codeSAP="SAP-G1",
                quantiteInstallee=10.0,
                historique=[
                    ConsommationPoint(date=f"2024-{month:02d}-01", quantite=5.0 + month % 2)
                    for month in range(1, 6)
                ],
            ),
            ArticleData(
                codeSAP="SAP-G2",
                quantiteInstallee=5.0,
                historique=[
                    ConsommationPoint(date=f"2024-{month:02d}-01", quantite=6.0 + month * 2)
                    for month in range(1, 6)
                ],
            ),
        ]
        predictions, evaluation = predict_all_articles(articles)
        self.assertEqual(len(predictions), 2)
        self.assertIsNotNone(evaluation)
        self.assertGreater(evaluation.nbSamplesTrained, 0)

    def test_prediction_utilise_un_modele_deja_entraine(self):
        article = ArticleData(
            codeSAP="SAP-TRAINED",
            quantiteInstallee=10.0,
            historique=[
                ConsommationPoint(date=f"2024-{month:02d}-01", quantite=float(month + 4))
                for month in range(1, 8)
            ],
        )
        model, evaluation = predictor.train_global_random_forest([article], force=True)
        predictions, _ = predict_articles_with_trained_model([article], model, evaluation)
        self.assertEqual(predictions[0].modeleUtilise, "RANDOM_FOREST_PYTHON")

    def test_prediction_gere_decembre_vers_janvier(self):
        article = ArticleData(
            codeSAP="SAP-DEC",
            quantiteInstallee=10.0,
            historique=[
                ConsommationPoint(date=f"2026-{month:02d}-01", quantite=float(month))
                for month in range(6, 13)
            ],
        )
        model, evaluation = predictor.train_global_random_forest([article], force=True)
        predictions, _ = predict_articles_with_trained_model([article], model, evaluation, "2026-12")
        self.assertEqual(predictions[0].moisActuel, "2026-12")
        self.assertEqual(predictions[0].moisPrevu, "2027-01")

    def test_modele_est_persistant(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            old_model_file = predictor.MODEL_FILE
            old_metadata_file = predictor.METADATA_FILE
            predictor.MODEL_FILE = Path(temporary_directory) / "model.joblib"
            predictor.METADATA_FILE = Path(temporary_directory) / "metadata.json"
            article = ArticleData(
                codeSAP="SAP-PERSIST",
                quantiteInstallee=10.0,
                historique=[
                    ConsommationPoint(date=f"2024-{month:02d}-01", quantite=float(month))
                    for month in range(1, 8)
                ],
            )
            model, evaluation = predictor.train_global_random_forest([article], force=True)
            self.assertTrue(predictor.MODEL_FILE.exists())
            self.assertTrue(predictor.METADATA_FILE.exists())
            self.assertIsNotNone(model)
            self.assertIsNotNone(evaluation)
            predictor.MODEL_CACHE["model"] = None
            predictor.MODEL_CACHE["evaluation"] = None
            predictor.load_persisted_model()
            self.assertIsNotNone(predictor.MODEL_CACHE["model"])
            predictor.MODEL_FILE = old_model_file
            predictor.METADATA_FILE = old_metadata_file

    def test_charge_le_dataset_excel_manuel(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            dataset_file = Path(temporary_directory) / "consommation_historique.xlsx"
            pd.DataFrame([
                {"CODE SAP": "SAP-001", "Q CONSOMMEE": 4, "DATE": "2024-01-01"},
                {"CODE SAP": "SAP-001", "Q CONSOMMEE": 5, "DATE": "2024-02-01"},
            ]).to_excel(dataset_file, index=False)
            old_dataset_file = predictor.DATASET_FILE
            predictor.DATASET_FILE = dataset_file
            articles, info = predictor.load_manual_training_dataset()
            self.assertEqual(info["articles"], 1)
            self.assertEqual(info["rows"], 2)
            self.assertEqual(articles[0].codeSAP, "SAP-001")
            predictor.DATASET_FILE = old_dataset_file

    def test_dataset_manuel_absent(self):
        old_dataset_file = predictor.DATASET_FILE
        predictor.DATASET_FILE = Path("dataset-inexistant.xlsx")
        with self.assertRaises(FileNotFoundError):
            predictor.load_manual_training_dataset()
        predictor.DATASET_FILE = old_dataset_file


if __name__ == "__main__":
    unittest.main()
