import React, { useState, useEffect } from 'react';
import {
  Boxes,
  Play,
  Download,
  Search,
  RefreshCw,
  CheckCircle2,
  Calendar,
  Layers,
} from 'lucide-react';
import analyseService, {
  type ResultatAppro,
  type AnalysisSummary,
} from '../../services/analyseService';
import importExportService from '../../services/importExportService';

export const ApprovisionnementPage: React.FC = () => {
  const [resultats, setResultats] = useState<ResultatAppro[]>([]);
  const [summary, setSummary] = useState<AnalysisSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCalculating, setIsCalculating] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [selectedMode, setSelectedMode] = useState('ALL');
  const [search, setSearch] = useState('');

  const triggerDownload = (blob: Blob, filename: string) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const [sumRes, resRes] = await Promise.all([
        analyseService.getSummary(),
        analyseService.getResultatsAppro({
          mode: selectedMode === 'ALL' ? undefined : selectedMode,
          search: search || undefined,
        }),
      ]);
      setSummary(sumRes.data);
      const payload = resRes.data;
      const nextResultats = Array.isArray(payload)
        ? payload
        : payload && 'content' in payload
          ? payload.content
          : [];
      setResultats(nextResultats);
    } catch (err) {
      console.error('Erreur chargement approvisionnement:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedMode]);

  const handleLancerCalcul = async () => {
    setIsCalculating(true);
    try {
      await analyseService.lancerRegles();
      await loadData();
    } catch (err) {
      console.error('Erreur lancement calcul règles approvisionnement:', err);
    } finally {
      setIsCalculating(false);
    }
  };

  const handleExportMode = async (mode: 'ALL' | 'MIN_MAX' | 'PLANIFIE' | 'SUR_DEMANDE') => {
    setIsExporting(true);
    try {
      let response;

      switch (mode) {
        case 'MIN_MAX':
          response = await importExportService.exportMinMax();
          triggerDownload(response.data, 'regle_min_max.xlsx');
          return;
        case 'PLANIFIE':
          response = await importExportService.exportPlanifie();
          triggerDownload(response.data, 'regle_mode_planifie.xlsx');
          return;
        case 'SUR_DEMANDE':
          response = await importExportService.exportSurDemande();
          triggerDownload(response.data, 'regle_sur_demande.xlsx');
          return;
        default:
          response = await importExportService.exportResultatsAppro();
          triggerDownload(response.data, 'resultats_approvisionnement_pdr.xlsx');
          return;
      }
    } catch (err) {
      console.error(`Erreur export mode approvisionnement ${mode}:`, err);
    } finally {
      setIsExporting(false);
    }
  };

  const getModeBadge = (mode: string) => {
    switch (mode) {
      case 'MIN_MAX':
        return (
          <span style={{ background: '#dbeafe', color: '#1d4ed8', padding: '4px 8px', borderRadius: '6px', fontWeight: 600, fontSize: '11px' }}>
            MIN-MAX
          </span>
        );
      case 'PLANIFIE':
        return (
          <span style={{ background: '#fef3c7', color: '#b45309', padding: '4px 8px', borderRadius: '6px', fontWeight: 600, fontSize: '11px' }}>
            PLANIFIÉ
          </span>
        );
      case 'SUR_DEMANDE':
        return (
          <span style={{ background: '#f3e8ff', color: '#6b21a8', padding: '4px 8px', borderRadius: '6px', fontWeight: 600, fontSize: '11px' }}>
            SUR DEMANDE
          </span>
        );
      default:
        return <span>{mode}</span>;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--ocp-text)', margin: 0 }}>
            Moteur d'Approvisionnement PDR
          </h1>
        </div>

        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <button
            onClick={() => handleExportMode(selectedMode as 'ALL' | 'MIN_MAX' | 'PLANIFIE' | 'SUR_DEMANDE')}
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
            {isExporting ? 'Exportation...' : selectedMode === 'ALL' ? 'Exporter tout' : `Exporter ${selectedMode.replace('_', ' ')}`}
          </button>

          <button
            onClick={() => handleExportMode('MIN_MAX')}
            disabled={isExporting}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 12px',
              borderRadius: '8px',
              border: '1px solid rgba(37, 99, 235, 0.25)',
              background: 'var(--ocp-card-bg)',
              color: '#1d4ed8',
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            Min-Max
          </button>

          <button
            onClick={() => handleExportMode('PLANIFIE')}
            disabled={isExporting}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 12px',
              borderRadius: '8px',
              border: '1px solid rgba(217, 119, 6, 0.25)',
              background: 'var(--ocp-card-bg)',
              color: '#b45309',
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            Planifié
          </button>

          <button
            onClick={() => handleExportMode('SUR_DEMANDE')}
            disabled={isExporting}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 12px',
              borderRadius: '8px',
              border: '1px solid rgba(124, 58, 237, 0.25)',
              background: 'var(--ocp-card-bg)',
              color: '#6b21a8',
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            Sur demande
          </button>

          <button
            onClick={handleLancerCalcul}
            disabled={isCalculating}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              background: '#2563eb',
              color: '#fff',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            <Play size={16} />
            {isCalculating ? 'Calcul en cours...' : 'Exécuter le Moteur'}
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      {summary && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid var(--ocp-border)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--ocp-text-muted)', fontSize: '13px', fontWeight: 600 }}>TOTAL ARTICLES</span>
              <Boxes size={20} color="#3b82f6" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--ocp-text)', marginTop: '8px' }}>
              {summary.totalArticles}
            </div>
            <span style={{ fontSize: '12px', color: 'var(--ocp-text-muted)' }}>Stock : {summary.articlesEnStock} en magasin</span>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(37, 99, 235, 0.25)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#1d4ed8', fontSize: '13px', fontWeight: 600 }}>RÈGLE MIN-MAX</span>
              <Layers size={20} color="#1d4ed8" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#1d4ed8', marginTop: '8px' }}>
              {summary.minMaxCount}
            </div>
            <span style={{ fontSize: '12px', color: '#2563eb' }}>{summary.minMaxRate?.toFixed(1)}% du catalogue</span>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(217, 119, 6, 0.25)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#b45309', fontSize: '13px', fontWeight: 600 }}>RÈGLE PLANIFIÉ</span>
              <Calendar size={20} color="#b45309" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#b45309', marginTop: '8px' }}>
              {summary.planifieCount}
            </div>
            <span style={{ fontSize: '12px', color: '#d97706' }}>{summary.planifieRate?.toFixed(1)}% des besoins</span>
          </div>

          <div style={{ background: 'var(--ocp-card-bg)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(124, 58, 237, 0.25)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#6b21a8', fontSize: '13px', fontWeight: 600 }}>SUR DEMANDE</span>
              <CheckCircle2 size={20} color="#6b21a8" />
            </div>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#6b21a8', marginTop: '8px' }}>
              {summary.surDemandeCount}
            </div>
            <span style={{ fontSize: '12px', color: '#7c3aed' }}>{summary.surDemandeRate?.toFixed(1)}% ponctuel</span>
          </div>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div style={{ background: 'var(--ocp-card-bg)', padding: '16px 20px', borderRadius: '12px', border: '1px solid var(--ocp-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ display: 'flex', gap: '8px' }}>
          {['ALL', 'MIN_MAX', 'PLANIFIE', 'SUR_DEMANDE'].map((mode) => (
            <button
              key={mode}
              onClick={() => setSelectedMode(mode)}
              style={{
                padding: '8px 16px',
                borderRadius: '8px',
                border: 'none',
                background: selectedMode === mode ? 'var(--ocp-primary)' : 'var(--ocp-surface-soft)',
                color: selectedMode === mode ? '#fff' : 'var(--ocp-text-muted)',
                fontWeight: 600,
                fontSize: '13px',
                cursor: 'pointer',
              }}
            >
              {mode === 'ALL' ? 'Tous les modes' : mode}
            </button>
          ))}
        </div>

        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '10px', top: '10px' }} />
            <input
              type="text"
              placeholder="Filtrer code SAP, désignation..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && loadData()}
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

          <button
            onClick={loadData}
            style={{
              padding: '8px 12px',
              borderRadius: '8px',
              border: '1px solid var(--ocp-border)',
              background: 'var(--ocp-card-bg)',
              color: 'var(--ocp-text)',
              cursor: 'pointer',
            }}
          >
            <RefreshCw size={14} />
          </button>
        </div>
      </div>

      {/* Results Table */}
      <div style={{ background: 'var(--ocp-card-bg)', borderRadius: '12px', border: '1px solid var(--ocp-border)', overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--ocp-text-muted)' }}>
            <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 8px' }} />
            <span>Chargement des résultats du moteur d'approvisionnement...</span>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead style={{ background: 'var(--ocp-surface-soft)', borderBottom: '1px solid var(--ocp-border)', color: 'var(--ocp-text-muted)', fontWeight: 600 }}>
              <tr>
                <th style={{ padding: '12px 16px' }}>Code SAP</th>
                <th style={{ padding: '12px 16px' }}>Désignation PDR</th>
                <th style={{ padding: '12px 16px' }}>Mode Décidé</th>
                <th style={{ padding: '12px 16px' }}>Quantité à Lancer</th>
                <th style={{ padding: '12px 16px' }}>Priorité</th>
                <th style={{ padding: '12px 16px' }}>Justification Métier</th>
                <th style={{ padding: '12px 16px' }}>Date Analyse</th>
              </tr>
            </thead>
            <tbody>
              {resultats.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ padding: '32px', textAlign: 'center', color: 'var(--ocp-text-muted)' }}>
                    Aucun résultat d'approvisionnement disponible. Cliquez sur "Exécuter le Moteur" pour lancer le calcul.
                  </td>
                </tr>
              ) : (
                resultats.map((r) => (
                  <tr key={r.id} style={{ borderBottom: '1px solid var(--ocp-border-light)' }}>
                    <td style={{ padding: '12px 16px', fontWeight: 600, color: 'var(--ocp-text)' }}>{r.codeSAP}</td>
                    <td style={{ padding: '12px 16px', color: 'var(--ocp-text)' }}>{r.designation || '-'}</td>
                    <td style={{ padding: '12px 16px' }}>{getModeBadge(r.mode)}</td>
                    <td style={{ padding: '12px 16px', fontWeight: 700, color: '#059669' }}>
                      {r.quantiteALancer}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <span
                        style={{
                          background: r.priorite === 1 ? '#fee2e2' : r.priorite === 2 ? '#ffedd5' : '#f1f5f9',
                          color: r.priorite === 1 ? '#dc2626' : r.priorite === 2 ? '#d97706' : '#64748b',
                          padding: '2px 8px',
                          borderRadius: '12px',
                          fontWeight: 600,
                          fontSize: '11px',
                        }}
                      >
                        P{r.priorite}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', color: 'var(--ocp-text-muted)', maxWidth: '320px' }}>
                      {r.justification}
                    </td>
                    <td style={{ padding: '12px 16px', color: 'var(--ocp-text-muted)' }}>
                      {r.dateAnalyse || '-'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ApprovisionnementPage;
