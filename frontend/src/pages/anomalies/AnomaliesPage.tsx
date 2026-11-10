import React, { useState, useEffect } from 'react';
import {
  AlertTriangle,
  Brain,
  Download,
  Search,
  RefreshCw,
  TrendingUp,
  CheckCircle,
  Eye,
  X,
  Activity,
  AlertCircle,
  HelpCircle,
} from 'lucide-react';
import analyseService, {
  type PredictionResult,
  type AnomalieItem,
  type ModelEvaluation,
  type AnomalieStatistiqueIa,
} from '../../services/analyseService';
import importExportService from '../../services/importExportService';
import PdrTrendChart from '../../components/charts/PdrTrendChart';
import Alert from '../../components/common/Alert';

export const AnomaliesPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'inhabituelles' | 'ia' | 'regles'>('inhabituelles');
  const [statAnomalies, setStatAnomalies] = useState<AnomalieStatistiqueIa[]>([]);
  const [predictions, setPredictions] = useState<PredictionResult[]>([]);
  const [anomalies, setAnomalies] = useState<AnomalieItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [riskFilter, setRiskFilter] = useState('ALL');
  const [statLevelFilter, setStatLevelFilter] = useState('ALL');
  const [selectedArticle, setSelectedArticle] = useState<PredictionResult | null>(null);
  const [isExporting, setIsExporting] = useState(false);
  const [isTraining, setIsTraining] = useState(false);
  const [isDetecting, setIsDetecting] = useState(false);
  const [evaluation, setEvaluation] = useState<ModelEvaluation | null>(null);
  const [modelStatus, setModelStatus] = useState('Prêt');
  const [aiMessage, setAiMessage] = useState('');
  const [aiError, setAiError] = useState('');

  // Statistics for Random Forest predictions
  const [stats, setStats] = useState({
    total: 0,
    critique: 0,
    alertes: 0,
    moyen: 0,
    normal: 0,
  });

  // Statistics for Statistical Anomalies (Moyenne + 2*std)
  const [statStats, setStatStats] = useState({
    total: 0,
    anomalies: 0,
    critique: 0,
    inhabituelle: 0,
    normal: 0,
    nonEvaluable: 0,
  });

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'inhabituelles') {
        const res = await analyseService.getConsommationsInhabituelles();
        if (res.data && res.data.results) {
          setStatAnomalies(res.data.results);
          setStatStats({
            total: res.data.totalArticles || 0,
            anomalies: res.data.nbAnomalies || 0,
            critique: res.data.nbCritique || 0,
            inhabituelle: res.data.nbInhabituelle || 0,
            normal: res.data.nbNormal || 0,
            nonEvaluable: res.data.nbNonEvaluable || 0,
          });
        }
      } else if (activeTab === 'ia') {
        const res = await analyseService.getPredictionsIa();
        if (res.data && res.data.predictions) {
          setPredictions(res.data.predictions);
          setEvaluation(res.data.evaluation || null);
          setModelStatus(res.data.evaluation ? 'Entraîné et disponible' : 'Données insuffisantes');
          setStats({
            total: res.data.totalArticles || 0,
            critique: res.data.nbCritique || 0,
            alertes: res.data.nbAlertes || 0,
            moyen: res.data.nbRisqueMoyen || 0,
            normal: res.data.nbNormal || 0,
          });
        }
      } else {
        const res = await analyseService.getAnomalies({ search });
        const rows = Array.isArray(res.data) ? res.data : res.data?.content || [];
        if (rows.length >= 0) {
          setAnomalies(rows.map((item) => ({
            ...item,
            codeSAP: item.codeSAP || item.article?.codeSAP || '',
            designation: item.designation || item.article?.description || '',
            secteur: item.secteur || item.article?.secteur?.nom || '',
          })));
        }
      }
    } catch (err) {
      console.error('Erreur chargement anomalies/IA:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDetectInhabituelles = async () => {
    setIsDetecting(true);
    setAiMessage('');
    setAiError('');
    try {
      const res = await analyseService.detecterConsommationsInhabituelles();
      if (res.data && res.data.results) {
        setStatAnomalies(res.data.results);
        setStatStats({
          total: res.data.totalArticles || 0,
          anomalies: res.data.nbAnomalies || 0,
          critique: res.data.nbCritique || 0,
          inhabituelle: res.data.nbInhabituelle || 0,
          normal: res.data.nbNormal || 0,
          nonEvaluable: res.data.nbNonEvaluable || 0,
        });
        setAiMessage(res.data.message || 'Détection automatique des consommations inhabituelles terminée.');
      }
    } catch (err: any) {
      console.error('Erreur détection consommations inhabituelles:', err);
      setAiError(err?.response?.data?.message || 'Erreur lors de la détection statistique des anomalies.');
    } finally {
      setIsDetecting(false);
    }
  };

  const handleTrainModel = async () => {
    setIsTraining(true);
    setAiMessage('Entraînement du modèle en cours...');
    setAiError('');
    try {
      const res = await analyseService.trainModel();
      setPredictions(res.data.predictions || []);
      setEvaluation(res.data.evaluation || null);
      setModelStatus(res.data.evaluation ? 'Entraîné et disponible' : 'Données insuffisantes');
      setStats({
        total: res.data.totalArticles || 0,
        critique: res.data.nbCritique || 0,
        alertes: res.data.nbAlertes || 0,
        moyen: res.data.nbRisqueMoyen || 0,
        normal: res.data.nbNormal || 0,
      });
      setAiMessage('Modèle entraîné avec succès.');
    } catch (err) {
      setModelStatus('Service IA indisponible');
      setAiMessage('');
      setAiError("Dataset d'entraînement introuvable ou invalide. Vérifiez ai/dataset/consommation_historique.xlsx.");
      console.error('Erreur entraînement Random Forest:', err);
    } finally {
      setIsTraining(false);
    }
  };

  const handlePredictModel = async () => {
    setIsTraining(true);
    setAiMessage('');
    setAiError('');
    try {
      const res = await analyseService.predictModel();
      setPredictions(res.data.predictions || []);
      setEvaluation(res.data.evaluation || null);
      setModelStatus(res.data.evaluation ? 'Modèle chargé' : 'Données insuffisantes');
      setStats({
        total: res.data.totalArticles || 0,
        critique: res.data.nbCritique || 0,
        alertes: res.data.nbAlertes || 0,
        moyen: res.data.nbRisqueMoyen || 0,
        normal: res.data.nbNormal || 0,
      });
      setAiMessage(res.data.message || 'Prédiction terminée.');
    } catch (err: any) {
      setModelStatus('Modèle non disponible');
      setAiError(err?.response?.data?.message || 'Entraînez le modèle avant de lancer une prédiction.');
    } finally {
      setIsTraining(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const handleExport = async () => {
    setIsExporting(true);
    try {
      const res = await importExportService.exportAnomalies();
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'anomalies_consommation_pdr.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Erreur export anomalies:', err);
    } finally {
      setIsExporting(false);
    }
  };

  const filteredStatAnomalies = statAnomalies.filter((item) => {
    const matchesSearch =
      item.codeSAP?.toLowerCase().includes(search.toLowerCase()) ||
      item.description?.toLowerCase().includes(search.toLowerCase());
    const matchesLevel = statLevelFilter === 'ALL' || item.niveau === statLevelFilter;
    return matchesSearch && matchesLevel;
  });

  const filteredPredictions = predictions.filter((p) => {
    const matchesSearch =
      p.codeSAP?.toLowerCase().includes(search.toLowerCase()) ||
      p.description?.toLowerCase().includes(search.toLowerCase());
    const matchesRisk = riskFilter === 'ALL' || p.niveauRisque === riskFilter;
    return matchesSearch && matchesRisk;
  });

  const getStatLevelBadge = (niveau: string) => {
    switch (niveau) {
      case 'CRITIQUE':
        return (
          <span style={{ padding: '4px 10px', borderRadius: '6px', fontSize: '11px', fontWeight: 700, background: 'rgba(220, 38, 38, 0.15)', color: '#dc2626', border: '1px solid rgba(220, 38, 38, 0.3)' }}>
            CRITIQUE
          </span>
        );
      case 'INHABITUELLE':
        return (
          <span style={{ padding: '4px 10px', borderRadius: '6px', fontSize: '11px', fontWeight: 700, background: 'rgba(217, 119, 6, 0.15)', color: '#d97706', border: '1px solid rgba(217, 119, 6, 0.3)' }}>
            INHABITUELLE
          </span>
        );
      case 'NORMAL':
        return (
          <span style={{ padding: '4px 10px', borderRadius: '6px', fontSize: '11px', fontWeight: 700, background: 'rgba(5, 150, 105, 0.15)', color: '#059669', border: '1px solid rgba(5, 150, 105, 0.3)' }}>
            NORMAL
          </span>
        );
      default:
        return (
          <span style={{ padding: '4px 10px', borderRadius: '6px', fontSize: '11px', fontWeight: 700, background: 'rgba(100, 116, 139, 0.15)', color: '#64748b', border: '1px solid rgba(100, 116, 139, 0.3)' }}>
            NON ÉVALUABLE
          </span>
        );
    }
  };

  const getRiskBadge = (niveau: string) => {
    switch (niveau) {
      case 'CRITIQUE':
        return <span className="badge badge-error">CRITIQUE</span>;
      case 'ELEVE':
        return <span className="badge badge-warning">ÉLEVÉ</span>;
      case 'MOYEN':
        return <span className="badge badge-info">MOYEN</span>;
      default:
        return <span className="badge badge-success">FAIBLE / NORMAL</span>;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--ocp-text)', margin: 0 }}>
            Détection d'Anomalies & Intelligence Artificielle (PDR)
          </h1>
          <p style={{ margin: '4px 0 0', color: 'var(--ocp-text-muted)', fontSize: '13px' }}>
            Surveillance statistique des consommations inhabituelles et prévisions Machine Learning
          </p>
        </div>

        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          {activeTab === 'inhabituelles' ? (
            <button
              onClick={handleDetectInhabituelles}
              disabled={isDetecting}
              className="btn-ocp btn-ocp-primary"
            >
              <Activity size={16} />
              {isDetecting ? 'Détection...' : 'Détecter les anomalies'}
            </button>
          ) : activeTab === 'ia' ? (
            <>
              <button
                onClick={handleTrainModel}
                disabled={isTraining}
                className="btn-ocp btn-ocp-primary"
              >
                <Brain size={16} />
                {isTraining ? 'Entraînement...' : 'Entraîner Random Forest'}
              </button>
              <button
                onClick={handlePredictModel}
                disabled={isTraining}
                className="btn-ocp btn-ocp-secondary"
              >
                <TrendingUp size={16} />
                {isTraining ? 'Prédiction...' : 'Prédire mois suivant'}
              </button>
            </>
          ) : null}

          <button
            onClick={handleExport}
            disabled={isExporting}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              border: '1px solid var(--ocp-border)',
              background: 'var(--ocp-card-bg)',
              color: 'var(--ocp-text)',
              cursor: 'pointer',
              fontWeight: 500,
            }}
          >
            <Download size={16} />
            {isExporting ? 'Export...' : 'Exporter Excel'}
          </button>

          <button
            onClick={fetchData}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              background: '#059669',
              color: '#fff',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 500,
            }}
          >
            <RefreshCw size={16} />
            Actualiser
          </button>
        </div>
      </div>

      {aiMessage && <Alert variant="success">{aiMessage}</Alert>}
      {aiError && <Alert variant="danger">{aiError}</Alert>}

      {activeTab === 'ia' && (
        <div className="card-ocp" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '16px' }}>
          <div>
            <span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>MODÈLE</span>
            <strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{modelStatus}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>MAE</span>
            <strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{evaluation?.mae ?? '-'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>RMSE</span>
            <strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{evaluation?.rmse ?? '-'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>R²</span>
            <strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{evaluation?.r2 ?? '-'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>ÉCHANTILLONS</span>
            <strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{evaluation?.nbSamplesTrained ?? 0}</strong>
          </div>
        </div>
      )}

      {/* KPI Cards per Tab */}
      {activeTab === 'inhabituelles' ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid var(--ocp-border)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--ocp-text-muted)', fontSize: '13px', fontWeight: 600 }}>ARTICLES ANALYSÉS</span>
              <Activity size={20} color="#3b82f6" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--ocp-text)', marginTop: '8px' }}>
              {statStats.total}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(220, 38, 38, 0.3)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#dc2626', fontSize: '13px', fontWeight: 600 }}>CRITIQUE (&gt; μ+3σ)</span>
              <AlertTriangle size={20} color="#dc2626" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#dc2626', marginTop: '8px' }}>
              {statStats.critique}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(217, 119, 6, 0.3)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#d97706', fontSize: '13px', fontWeight: 600 }}>INHABITUELLE (&gt; μ+2σ)</span>
              <AlertCircle size={20} color="#d97706" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#d97706', marginTop: '8px' }}>
              {statStats.inhabituelle}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(5, 150, 105, 0.3)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#059669', fontSize: '13px', fontWeight: 600 }}>CONFORME / NORMAL</span>
              <CheckCircle size={20} color="#059669" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#059669', marginTop: '8px' }}>
              {statStats.normal}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid var(--ocp-border)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#64748b', fontSize: '13px', fontWeight: 600 }}>NON ÉVALUABLE (&lt;3 mois)</span>
              <HelpCircle size={20} color="#64748b" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#64748b', marginTop: '8px' }}>
              {statStats.nonEvaluable}
            </div>
          </div>
        </div>
      ) : activeTab === 'ia' ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid var(--ocp-border)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--ocp-text-muted)', fontSize: '13px', fontWeight: 600 }}>ARTICLES ÉVALUÉS</span>
              <Brain size={20} color="#3b82f6" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--ocp-text)', marginTop: '8px' }}>
              {stats.total}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(220, 38, 38, 0.3)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#dc2626', fontSize: '13px', fontWeight: 600 }}>RISQUE CRITIQUE</span>
              <AlertTriangle size={20} color="#dc2626" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#dc2626', marginTop: '8px' }}>
              {stats.critique}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(217, 119, 6, 0.3)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#d97706', fontSize: '13px', fontWeight: 600 }}>RISQUE ÉLEVÉ</span>
              <TrendingUp size={20} color="#d97706" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#d97706', marginTop: '8px' }}>
              {stats.alertes}
            </div>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(5, 150, 105, 0.3)', boxShadow: 'var(--shadow-sm)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#059669', fontSize: '13px', fontWeight: 600 }}>CONFORME / NORMAL</span>
              <CheckCircle size={20} color="#059669" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#059669', marginTop: '8px' }}>
              {stats.normal}
            </div>
          </div>
        </div>
      ) : null}

      {/* Tabs and Filters */}
      <div style={{ background: 'var(--ocp-card-bg)', padding: '16px 20px', borderRadius: '12px', border: '1px solid var(--ocp-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <button
            onClick={() => setActiveTab('inhabituelles')}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              border: 'none',
              background: activeTab === 'inhabituelles' ? 'var(--ocp-primary)' : 'var(--ocp-surface-soft)',
              color: activeTab === 'inhabituelles' ? '#fff' : 'var(--ocp-text-muted)',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <Activity size={16} />
            Consommations Inhabituelles (IA Statistique) ({statAnomalies.length})
          </button>

          <button
            onClick={() => setActiveTab('ia')}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              border: 'none',
              background: activeTab === 'ia' ? 'var(--ocp-primary)' : 'var(--ocp-surface-soft)',
              color: activeTab === 'ia' ? '#fff' : 'var(--ocp-text-muted)',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <Brain size={16} />
            Prédictions IA M+1 (Random Forest) ({predictions.length})
          </button>

          <button
            onClick={() => setActiveTab('regles')}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              border: 'none',
              background: activeTab === 'regles' ? 'var(--ocp-primary)' : 'var(--ocp-surface-soft)',
              color: activeTab === 'regles' ? '#fff' : 'var(--ocp-text-muted)',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <AlertTriangle size={16} />
            Anomalies Règle Métier OCP (200%)
          </button>
        </div>

        <div style={{ display: 'flex', gap: '12px', alignItems: 'center', flexWrap: 'wrap' }}>
          <div style={{ position: 'relative' }}>
            <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '10px', top: '10px' }} />
            <input
              type="text"
              placeholder="Rechercher code SAP, désignation..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{
                padding: '8px 12px 8px 32px',
                borderRadius: '8px',
                border: '1px solid var(--ocp-border)',
                background: 'var(--ocp-card-bg)',
                color: 'var(--ocp-text)',
                fontSize: '13px',
                width: '240px',
              }}
            />
          </div>

          {activeTab === 'inhabituelles' && (
            <select
              value={statLevelFilter}
              onChange={(e) => setStatLevelFilter(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid var(--ocp-border)',
                background: 'var(--ocp-card-bg)',
                fontSize: '13px',
                color: 'var(--ocp-text)',
              }}
            >
              <option value="ALL">Tous les niveaux</option>
              <option value="CRITIQUE">Critique (&gt; μ+3σ)</option>
              <option value="INHABITUELLE">Inhabituelle (&gt; μ+2σ)</option>
              <option value="NORMAL">Normal (≤ μ+2σ)</option>
              <option value="NON_EVALUABLE">Non Évaluable (&lt; 3 mois)</option>
            </select>
          )}

          {activeTab === 'ia' && (
            <select
              value={riskFilter}
              onChange={(e) => setRiskFilter(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid var(--ocp-border)',
                background: 'var(--ocp-card-bg)',
                fontSize: '13px',
                color: 'var(--ocp-text)',
              }}
            >
              <option value="ALL">Tous les risques</option>
              <option value="CRITIQUE">Critique</option>
              <option value="ELEVE">Élevé</option>
              <option value="MOYEN">Moyen</option>
              <option value="FAIBLE">Faible / Normal</option>
            </select>
          )}
        </div>
      </div>

      {/* Table Data */}
      <div style={{ background: 'var(--ocp-card-bg)', borderRadius: '12px', border: '1px solid var(--ocp-border)', overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--ocp-text-muted)' }}>
            <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 8px' }} />
            <span>Chargement des analyses en cours...</span>
          </div>
        ) : activeTab === 'inhabituelles' ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
              <thead style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: 600 }}>
                <tr>
                  <th style={{ padding: '12px 16px' }}>Code SAP</th>
                  <th style={{ padding: '12px 16px' }}>Désignation Article</th>
                  <th style={{ padding: '12px 16px' }}>Conso Récente</th>
                  <th style={{ padding: '12px 16px' }}>Moyenne Hist.</th>
                  <th style={{ padding: '12px 16px' }}>Écart-Type (σ)</th>
                  <th style={{ padding: '12px 16px' }}>Seuil (μ + 2σ)</th>
                  <th style={{ padding: '12px 16px' }}>Augmentation</th>
                  <th style={{ padding: '12px 16px' }}>Niveau</th>
                  <th style={{ padding: '12px 16px' }}>Justification</th>
                </tr>
              </thead>
              <tbody>
                {filteredStatAnomalies.length === 0 ? (
                  <tr>
                    <td colSpan={9} style={{ padding: '32px', textAlign: 'center', color: 'var(--ocp-text-muted)' }}>
                      Aucune anomalie statistique ne correspond à vos critères.
                    </td>
                  </tr>
                ) : (
                  filteredStatAnomalies.map((item) => (
                    <tr key={item.codeSAP} style={{ borderBottom: '1px solid var(--ocp-border-light)' }}>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: 'var(--ocp-text)' }}>{item.codeSAP}</td>
                      <td style={{ padding: '12px 16px', color: 'var(--ocp-text)' }}>{item.description || '-'}</td>
                      <td style={{ padding: '12px 16px', fontWeight: 700, color: item.anomalie ? '#dc2626' : 'var(--ocp-text)' }}>
                        {item.consommationRecente?.toFixed(2)}
                      </td>
                      <td style={{ padding: '12px 16px' }}>{item.moyenneHistorique?.toFixed(2)}</td>
                      <td style={{ padding: '12px 16px' }}>{item.ecartType?.toFixed(2)}</td>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: '#d97706' }}>
                        {item.seuil?.toFixed(2)}
                      </td>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: item.augmentationPourcentage > 0 ? '#dc2626' : '#059669' }}>
                        {item.augmentationPourcentage > 0 ? `+${item.augmentationPourcentage.toFixed(1)}%` : `${item.augmentationPourcentage.toFixed(1)}%`}
                      </td>
                      <td style={{ padding: '12px 16px' }}>{getStatLevelBadge(item.niveau)}</td>
                      <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--ocp-text-muted)', maxWidth: '320px' }}>
                        {item.justification}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        ) : activeTab === 'ia' ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
              <thead style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: 600 }}>
                <tr>
                  <th style={{ padding: '12px 16px' }}>Code SAP</th>
                  <th style={{ padding: '12px 16px' }}>Désignation Article</th>
                  <th style={{ padding: '12px 16px' }}>Qté Installée</th>
                  <th style={{ padding: '12px 16px' }}>Conso Actuelle</th>
                  <th style={{ padding: '12px 16px' }}>Prédiction IA (M+1)</th>
                  <th style={{ padding: '12px 16px' }}>Seuil OCP</th>
                  <th style={{ padding: '12px 16px' }}>Niveau Risque</th>
                  <th style={{ padding: '12px 16px' }}>Score</th>
                  <th style={{ padding: '12px 16px', textAlign: 'center' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredPredictions.length === 0 ? (
                  <tr>
                    <td colSpan={9} style={{ padding: '32px', textAlign: 'center', color: 'var(--ocp-text-muted)' }}>
                      Aucune prédiction ne correspond à vos critères.
                    </td>
                  </tr>
                ) : (
                  filteredPredictions.map((p) => (
                    <tr key={p.codeSAP} style={{ borderBottom: '1px solid var(--ocp-border-light)' }}>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: 'var(--ocp-text)' }}>{p.codeSAP}</td>
                      <td style={{ padding: '12px 16px', color: 'var(--ocp-text)' }}>{p.description || '-'}</td>
                      <td style={{ padding: '12px 16px' }}>{p.quantiteInstallee ?? 0}</td>
                      <td style={{ padding: '12px 16px', fontWeight: 500 }}>{p.consommationActuelle?.toFixed(1) ?? 0}</td>
                      <td style={{ padding: '12px 16px', fontWeight: 700, color: '#6366f1' }}>
                        {p.consommationPredite?.toFixed(1) ?? '-'}
                      </td>
                      <td style={{ padding: '12px 16px', color: '#ef4444', fontWeight: 500 }}>{p.seuil ?? 0}</td>
                      <td style={{ padding: '12px 16px' }}>{getRiskBadge(p.niveauRisque)}</td>
                      <td style={{ padding: '12px 16px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <div style={{ width: '45px', height: '6px', background: 'var(--ocp-border)', borderRadius: '3px', overflow: 'hidden' }}>
                            <div
                              style={{
                                width: `${p.scoreRisque ?? 0}%`,
                                height: '100%',
                                background: p.scoreRisque > 70 ? '#ef4444' : p.scoreRisque > 40 ? '#f59e0b' : '#10b981',
                              }}
                            />
                          </div>
                          <span style={{ fontSize: '11px', color: 'var(--ocp-text-muted)' }}>{p.scoreRisque}%</span>
                        </div>
                      </td>
                      <td style={{ padding: '12px 16px', textAlign: 'center' }}>
                        <button
                          onClick={() => setSelectedArticle(p)}
                          style={{
                            padding: '6px 12px',
                            borderRadius: '6px',
                            border: '1px solid var(--ocp-border)',
                            background: 'var(--ocp-card-bg)',
                            color: 'var(--ocp-text)',
                            cursor: 'pointer',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '4px',
                            fontSize: '12px',
                          }}
                        >
                          <Eye size={14} /> Courbe
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
              <thead style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: 600 }}>
                <tr>
                  <th style={{ padding: '12px 16px' }}>Code SAP</th>
                  <th style={{ padding: '12px 16px' }}>Désignation Article</th>
                  <th style={{ padding: '12px 16px' }}>Secteur</th>
                  <th style={{ padding: '12px 16px' }}>Conso Mensuelle</th>
                  <th style={{ padding: '12px 16px' }}>Qté Installée</th>
                  <th style={{ padding: '12px 16px' }}>Taux Conso</th>
                  <th style={{ padding: '12px 16px' }}>Seuil OCP</th>
                  <th style={{ padding: '12px 16px' }}>Statut</th>
                </tr>
              </thead>
              <tbody>
                {anomalies.length === 0 ? (
                  <tr>
                    <td colSpan={8} style={{ padding: '32px', textAlign: 'center', color: 'var(--ocp-text-muted)' }}>
                      Aucune anomalie enregistrée selon la règle métier OCP (Conso &gt; 2 × Quantité installée).
                    </td>
                  </tr>
                ) : (
                  anomalies.map((a) => (
                    <tr key={a.id} style={{ borderBottom: '1px solid var(--ocp-border-light)' }}>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: 'var(--ocp-text)' }}>{a.codeSAP}</td>
                      <td style={{ padding: '12px 16px', color: 'var(--ocp-text)' }}>{a.designation}</td>
                      <td style={{ padding: '12px 16px', color: 'var(--ocp-text)' }}>{a.secteur || '-'}</td>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: '#dc2626' }}>{a.consommationMensuelle}</td>
                      <td style={{ padding: '12px 16px', color: 'var(--ocp-text)' }}>{a.quantiteInstallee}</td>
                      <td style={{ padding: '12px 16px', fontWeight: 600, color: 'var(--ocp-text)' }}>{(a.tauxConsommation * 100).toFixed(0)}%</td>
                      <td style={{ padding: '12px 16px', color: 'var(--ocp-text-muted)' }}>{a.seuil}</td>
                      <td style={{ padding: '12px 16px' }}>
                        <span className="badge badge-warning">{a.statut || 'DÉTECTÉE'}</span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal for Chart & Details */}
      {selectedArticle && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(15, 23, 42, 0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 999,
            padding: '20px',
          }}
        >
          <div
            style={{
              background: 'var(--ocp-card-bg)',
              borderRadius: '16px',
              maxWidth: '750px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '24px',
              boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700, color: 'var(--ocp-text)' }}>
                  Analyse Prédictive : {selectedArticle.codeSAP}
                </h3>
                <p style={{ margin: '4px 0 0', color: 'var(--ocp-text-muted)', fontSize: '13px' }}>
                  {selectedArticle.description}
                </p>
              </div>
              <button
                onClick={() => setSelectedArticle(null)}
                style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: '4px' }}
              >
                <X size={20} color="#64748b" />
              </button>
            </div>

            <PdrTrendChart
              historique={selectedArticle.historique || []}
              seuil={selectedArticle.seuil}
              previsionM1={selectedArticle.consommationPredite}
              quantiteInstallee={selectedArticle.quantiteInstallee}
            />

            <div style={{ marginTop: '20px', background: 'var(--ocp-surface-soft)', padding: '16px', borderRadius: '12px' }}>
              <h4 style={{ margin: '0 0 8px', fontSize: '14px', fontWeight: 600, color: 'var(--ocp-text)' }}>
                Explications du modèle IA :
              </h4>
              <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', color: 'var(--ocp-text-muted)' }}>
                {selectedArticle.explications?.map((exp, i) => (
                  <li key={i} style={{ marginBottom: '4px' }}>{exp}</li>
                )) || <li>Aucune explication spécifique générée.</li>}
              </ul>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AnomaliesPage;
