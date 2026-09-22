import React, { useState } from 'react';
import reportingService from '../../services/reportingService';
import importExportService from '../../services/importExportService';
import Alert from '../../components/common/Alert';
import {
  FileSpreadsheet,
  UploadCloud,
  FileText,
  Database,
} from 'lucide-react';

const expectedColumnsDA = [
  'DA', 'Dossier', 'N°', 'Code Oracle', 'Code SAP', 'Description',
  'UDM', 'Q. Retenue', 'Secteur', 'Fournisseur', 'CMD', '% Livraison',
  'Délai livraison', 'Date notification', 'Date Prévisionnelle', 'Commentaire', 'Statut Livraison', 'Responsable'
];

export const ImportPage: React.FC = () => {
  const [activeImportType, setActiveImportType] = useState<'da' | 'pdr'>('da');

  // DA state
  const [selectedFileDA, setSelectedFileDA] = useState<File | null>(null);
  const [uploadingDA, setUploadingDA] = useState(false);
  const [dragActiveDA, setDragActiveDA] = useState(false);
  const [messageDA, setMessageDA] = useState('');
  const [errorDA, setErrorDA] = useState('');

  // PDR multi-files state
  const [stockFile, setStockFile] = useState<File | null>(null);
  const [consoFile, setConsoFile] = useState<File | null>(null);
  const [bomFile, setBomFile] = useState<File | null>(null);
  const [backlogFile, setBacklogFile] = useState<File | null>(null);
  const [uploadingPdr, setUploadingPdr] = useState(false);
  const [messagePdr, setMessagePdr] = useState('');
  const [errorPdr, setErrorPdr] = useState('');
  const [pdrSummary, setPdrSummary] = useState<{
    lignesImportees?: number;
    erreurs?: number;
    detailsErreurs?: string[];
  } | null>(null);

  const handleUploadDA = async (event: React.FormEvent) => {
    event.preventDefault();
    setMessageDA('');
    setErrorDA('');

    if (!selectedFileDA) {
      setErrorDA('Veuillez choisir un fichier Excel DA à importer.');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFileDA);

    setUploadingDA(true);
    try {
      const response = await reportingService.importFile(formData);
      setMessageDA(response.data || 'Fichier Excel DA importé avec succès.');
      setSelectedFileDA(null);
    } catch (uploadError: any) {
      setErrorDA(uploadError?.response?.data?.message || 'Erreur lors de l’importation du fichier DA.');
    } finally {
      setUploadingDA(false);
    }
  };

  const handleUploadPDR = async (event: React.FormEvent) => {
    event.preventDefault();
    setMessagePdr('');
    setErrorPdr('');
    setPdrSummary(null);

    if (!stockFile && !consoFile && !bomFile && !backlogFile) {
      setErrorPdr('Veuillez sélectionner au moins un fichier PDR à importer.');
      return;
    }

    const formData = new FormData();
    if (stockFile) formData.append('stockFile', stockFile);
    if (consoFile) formData.append('consommationFile', consoFile);
    if (bomFile) formData.append('bomFile', bomFile);
    if (backlogFile) formData.append('backlogFile', backlogFile);

    setUploadingPdr(true);
    try {
      const response = await importExportService.importMultiFiles(formData);
      setMessagePdr(response.data?.message || 'Données PDR importées et synchronisées avec succès.');
      setPdrSummary({
        lignesImportees: response.data?.lignesImportees,
        erreurs: response.data?.erreurs,
        detailsErreurs: response.data?.detailsErreurs || [],
      });
      setStockFile(null);
      setConsoFile(null);
      setBomFile(null);
      setBacklogFile(null);
    } catch (err: any) {
      setErrorPdr(err?.response?.data?.message || 'Erreur lors de l’importation des données PDR.');
    } finally {
      setUploadingPdr(false);
    }
  };

  return (
    <div className="app-content" style={{ maxWidth: '900px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div>
        <h2 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--ocp-text)' }}>Centre d'Importation des Données</h2>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '12px', borderBottom: '1px solid var(--ocp-border)', paddingBottom: '12px' }}>
        <button
          onClick={() => setActiveImportType('da')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '10px 20px',
            borderRadius: '8px',
            border: 'none',
            background: activeImportType === 'da' ? '#059669' : 'var(--ocp-surface-soft)',
            color: activeImportType === 'da' ? '#fff' : 'var(--ocp-text-muted)',
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          <FileSpreadsheet size={18} />
          Demandes d'Achat (DA)
        </button>

        <button
          onClick={() => setActiveImportType('pdr')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '10px 20px',
            borderRadius: '8px',
            border: 'none',
            background: activeImportType === 'pdr' ? '#059669' : 'var(--ocp-surface-soft)',
            color: activeImportType === 'pdr' ? '#fff' : 'var(--ocp-text-muted)',
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          <Database size={18} />
          Référentiels PDR (Stock, Conso, BOM, Backlog)
        </button>
      </div>

      {activeImportType === 'da' ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {messageDA && <Alert variant="success">{messageDA}</Alert>}
          {errorDA && <Alert variant="danger">{errorDA}</Alert>}

          <form onSubmit={handleUploadDA} className="card" style={{ padding: '24px' }}>
            <div
              className={`dropzone ${dragActiveDA ? 'active' : ''}`}
              onDragEnter={() => setDragActiveDA(true)}
              onDragLeave={() => setDragActiveDA(false)}
              onDragOver={(e) => e.preventDefault()}
              onDrop={(e) => {
                e.preventDefault();
                setDragActiveDA(false);
                if (e.dataTransfer.files?.[0]) setSelectedFileDA(e.dataTransfer.files[0]);
              }}
              style={{
                border: '2px dashed var(--ocp-border)',
                borderRadius: '12px',
                padding: '40px 20px',
                textAlign: 'center',
                background: 'var(--ocp-surface-soft)',
                cursor: 'pointer',
              }}
            >
              <UploadCloud size={40} color="#059669" style={{ margin: '0 auto 12px' }} />
              <p style={{ fontWeight: 600, color: 'var(--ocp-text)', margin: '0 0 4px' }}>
                Glissez-déposez le fichier Excel DA ici
              </p>
              <p style={{ fontSize: '12px', color: 'var(--ocp-text-muted)', margin: '0 0 16px' }}>
                Formats acceptés : .xlsx, .xls
              </p>
              <input
                type="file"
                id="file-upload-da"
                accept=".xlsx,.xls"
                onChange={(e) => e.target.files?.[0] && setSelectedFileDA(e.target.files[0])}
                style={{ display: 'none' }}
              />
              <label
                htmlFor="file-upload-da"
                style={{
                  display: 'inline-block',
                  padding: '8px 16px',
                  borderRadius: '6px',
                  background: '#059669',
                  color: '#fff',
                  cursor: 'pointer',
                  fontSize: '13px',
                  fontWeight: 500,
                }}
              >
                Parcourir les fichiers
              </label>
            </div>

            {selectedFileDA && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '16px', padding: '12px', background: 'var(--ocp-surface-soft)', borderRadius: '8px' }}>
                <FileText size={20} color="#059669" />
                <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--ocp-text)' }}>{selectedFileDA.name}</span>
                <span style={{ fontSize: '11px', color: 'var(--ocp-text-muted)' }}>({(selectedFileDA.size / 1024).toFixed(1)} Ko)</span>
              </div>
            )}

            <button
              type="submit"
              disabled={uploadingDA || !selectedFileDA}
              style={{
                marginTop: '20px',
                width: '100%',
                padding: '12px',
                borderRadius: '8px',
                background: uploadingDA || !selectedFileDA ? '#94a3b8' : '#059669',
                color: '#fff',
                border: 'none',
                fontWeight: 600,
                cursor: uploadingDA || !selectedFileDA ? 'not-allowed' : 'pointer',
              }}
            >
              {uploadingDA ? 'Importation en cours...' : 'Importer le fichier DA'}
            </button>
          </form>

          <div className="card" style={{ padding: '20px' }}>
            <h4 style={{ margin: '0 0 12px', fontSize: '14px', fontWeight: 600, color: 'var(--ocp-text)' }}>
              Colonnes requises pour le reporting DA (18 colonnes) :
            </h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              {expectedColumnsDA.map((col) => (
                <span key={col} style={{ background: 'var(--ocp-surface-soft)', color: 'var(--ocp-text-muted)', padding: '4px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: 500 }}>
                  {col}
                </span>
              ))}
            </div>
          </div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {messagePdr && <Alert variant="success">{messagePdr}</Alert>}
          {errorPdr && <Alert variant="danger">{errorPdr}</Alert>}
          {pdrSummary && (
            <div className="card-ocp" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
              <div><span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>LIGNES IMPORTÉES</span><strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{pdrSummary.lignesImportees ?? 0}</strong></div>
              <div><span style={{ color: 'var(--ocp-text-muted)', fontSize: '12px' }}>ERREURS / REJETS</span><strong style={{ display: 'block', color: 'var(--ocp-text)', marginTop: '4px' }}>{pdrSummary.erreurs ?? 0}</strong></div>
              {pdrSummary.detailsErreurs && pdrSummary.detailsErreurs.length > 0 && (
                <div style={{ gridColumn: '1 / -1', color: 'var(--ocp-danger)', fontSize: '12px' }}>{pdrSummary.detailsErreurs.slice(0, 3).join(' | ')}</div>
              )}
            </div>
          )}

          <form onSubmit={handleUploadPDR} className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
              {/* Stock */}
              <div style={{ padding: '16px', border: '1px solid var(--ocp-border)', borderRadius: '8px', background: 'var(--ocp-surface-soft)' }}>
                <span style={{ fontWeight: 600, fontSize: '13px', color: 'var(--ocp-text)' }}>Fichier Stock Magasin</span>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={(e) => setStockFile(e.target.files?.[0] || null)}
                  style={{ display: 'block', marginTop: '8px', fontSize: '12px' }}
                />
              </div>

              {/* Consommations */}
              <div style={{ padding: '16px', border: '1px solid var(--ocp-border)', borderRadius: '8px', background: 'var(--ocp-surface-soft)' }}>
                <span style={{ fontWeight: 600, fontSize: '13px', color: 'var(--ocp-text)' }}>Fichier Consommations Mensuelles</span>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={(e) => setConsoFile(e.target.files?.[0] || null)}
                  style={{ display: 'block', marginTop: '8px', fontSize: '12px' }}
                />
              </div>

              {/* BOM */}
              <div style={{ padding: '16px', border: '1px solid var(--ocp-border)', borderRadius: '8px', background: 'var(--ocp-surface-soft)' }}>
                <span style={{ fontWeight: 600, fontSize: '13px', color: 'var(--ocp-text)' }}>Fiche BOM (Nomenclatures Équipements)</span>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={(e) => setBomFile(e.target.files?.[0] || null)}
                  style={{ display: 'block', marginTop: '8px', fontSize: '12px' }}
                />
              </div>

              {/* Backlog OT */}
              <div style={{ padding: '16px', border: '1px solid var(--ocp-border)', borderRadius: '8px', background: 'var(--ocp-surface-soft)' }}>
                <span style={{ fontWeight: 600, fontSize: '13px', color: 'var(--ocp-text)' }}>Fichier Backlog OT (Besoins planifiés)</span>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={(e) => setBacklogFile(e.target.files?.[0] || null)}
                  style={{ display: 'block', marginTop: '8px', fontSize: '12px' }}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={uploadingPdr || (!stockFile && !consoFile && !bomFile && !backlogFile)}
              style={{
                marginTop: '12px',
                padding: '12px',
                borderRadius: '8px',
                background: uploadingPdr || (!stockFile && !consoFile && !bomFile && !backlogFile) ? '#94a3b8' : '#2563eb',
                color: '#fff',
                border: 'none',
                fontWeight: 600,
                cursor: uploadingPdr || (!stockFile && !consoFile && !bomFile && !backlogFile) ? 'not-allowed' : 'pointer',
              }}
            >
              {uploadingPdr ? 'Synchronisation en cours...' : 'Importer et synchroniser les référentiels PDR'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default ImportPage;
