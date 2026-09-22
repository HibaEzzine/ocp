import React, { useEffect, useMemo, useState } from 'react';
import { AxiosError } from 'axios';
import { useAuth } from '../../context/AuthContext';
import reportingService from '../../services/reportingService';
import type { ReportingDto } from '../../types/reporting';
import Alert from '../../components/common/Alert';
import ReportingTable from '../../components/reporting/ReportingTable';
import { getReportingColumnsByRole } from '../../utils/reportingColumns';
import {
  FileText,
  PlusCircle,
  RotateCcw,
  Search,
  Package,
  Truck,
  MessageSquare,
  X,
  Save,
} from 'lucide-react';

const statutOptions = ['', 'EN_COURS', 'ATTENTE_LIVRAISON', 'LIVRE', 'ECARTE', 'ADJUGE', 'LITIGE', 'ANNULE'];
const secteurOptions = ['', 'AMMONIAC', 'SOUFRE', 'EXPORT', 'COMMUN'];
const responsableOptions = ['', 'ATTOUCHI', 'BELYAZID', 'REGUIG', 'EL_HARKI'];
const udmOptions = ['', 'PCE', 'ML', 'MC', 'U', 'KG'];

const statusLabels: Record<string, string> = {
  EN_COURS: 'En cours',
  ATTENTE_LIVRAISON: 'Attente',
  LIVRE: 'Livré',
  ECARTE: 'Écarté',
  ADJUGE: 'Adjugé',
  LITIGE: 'Litige',
  CONTENTIEUX: 'Litige',
  ANNULE: 'Annulé',
};

const ReportingPage: React.FC = () => {
  const { user } = useAuth();
  const [reportings, setReportings] = useState<ReportingDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [editingReporting, setEditingReporting] = useState<ReportingDto | null>(null);
  const [filters, setFilters] = useState({
    search: '',
    statut: '',
    secteur: '',
    responsable: '',
    fournisseur: '',
    commande: '',
  });

  const loadReportings = async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = {};
      if (filters.search) params.search = filters.search;
      if (filters.statut) params.statut = filters.statut;
      if (filters.secteur) params.secteur = filters.secteur;
      if (filters.responsable) params.responsable = filters.responsable;
      if (filters.fournisseur) params.fournisseur = filters.fournisseur;
      if (filters.commande) params.commande = filters.commande;
      const reportingsResponse = await reportingService.getAll(params);
      setReportings(reportingsResponse.data);
    } catch (error) {
      setErrorMessage('Impossible de charger les reportings.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReportings();
  }, []);

  const handleFilterSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    await loadReportings();
  };

  const handleReset = () => {
    setFilters({ search: '', statut: '', secteur: '', responsable: '', fournisseur: '', commande: '' });
    setTimeout(loadReportings, 0);
  };

  const createNewReporting = (): ReportingDto => ({
    numeroDA: '',
    numeroDossier: '',
    numero: '',
    codeOracle: '',
    codeSAP: '',
    description: '',
    uniteDeMesure: '',
    quantite: NaN,
    secteur: '',
    commande: '',
    fournisseur: '',
    pourcentageLivraison: NaN,
    delaiLivraison: NaN,
    dateNotification: '',
    datePrevisionnelle: '',
    statut: '',
    responsable: '',
    utilisateurId: user?.id,
    commentaire: '',
  });

  const handleCreate = () => {
    setErrorMessage(null);
    setEditingReporting(createNewReporting());
  };

  const handleEdit = async (id: number | undefined) => {
    if (id == null) {
      setErrorMessage('Identifiant du reporting manquant.');
      return;
    }
    setLoading(true);
    try {
      const response = await reportingService.getById(id);
      setEditingReporting(response.data);
      setErrorMessage(null);
    } catch (error) {
      setErrorMessage('Impossible de charger le reporting.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number | undefined) => {
    if (id == null) {
      setErrorMessage('Identifiant du reporting manquant.');
      return;
    }
    if (!window.confirm('Voulez-vous vraiment supprimer ce reporting ?')) {
      return;
    }
    setLoading(true);
    try {
      await reportingService.remove(id);
      await loadReportings();
    } catch (error) {
      setErrorMessage('Impossible de supprimer le reporting.');
    } finally {
      setLoading(false);
    }
  };

  const updateEditingField = (field: keyof ReportingDto, value: string | number | null | undefined) => {
    if (!editingReporting) return;
    setEditingReporting({ ...editingReporting, [field]: value } as ReportingDto);
  };

  const handleSave = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!editingReporting) return;

    const isEmptyField = (value: unknown) =>
      value === undefined || value === null || value === '' || (typeof value === 'number' && Number.isNaN(value));

    const requiredFields = [
      { value: editingReporting.numeroDA, label: 'DA' },
      { value: editingReporting.numeroDossier, label: 'Dossier' },
      { value: editingReporting.numero, label: 'N°' },
      { value: editingReporting.codeOracle, label: 'Code Oracle' },
      { value: editingReporting.codeSAP, label: 'Code SAP' },
      { value: editingReporting.secteur, label: 'Secteur' },
      { value: editingReporting.commande, label: 'CMD' },
      { value: editingReporting.fournisseur, label: 'Fournisseur' },
      { value: editingReporting.statut, label: 'Statut livraison' },
      { value: editingReporting.responsable, label: 'Responsable DO' },
      { value: editingReporting.quantite, label: 'Quantité' },
      { value: editingReporting.pourcentageLivraison, label: '% Livraison' },
    ];

    const missing = requiredFields
      .filter((field) => isEmptyField(field.value))
      .map((field) => field.label);

    if (missing.length > 0) {
      setErrorMessage(`Veuillez renseigner les champs obligatoires : ${missing.join(', ')}`);
      return;
    }

    setSaveLoading(true);
    setErrorMessage(null);

    try {
      const reportingId = editingReporting.id;
      const isExisting = reportingId != null;
      const rawPayload: any = {
        ...editingReporting,
        utilisateurId: editingReporting.utilisateurId ?? user?.id,
      };

      const sanitizePayload = (obj: any) => {
        const copy: any = { ...obj };
        Object.keys(copy).forEach((k) => {
          if (copy[k] === '') copy[k] = null;
          if (typeof copy[k] === 'number' && Number.isNaN(copy[k])) copy[k] = null;
        });
        return copy;
      };

      const payload = sanitizePayload(rawPayload);

      if (!payload.utilisateurId) {
        throw new Error('Utilisateur non authentifié');
      }

      if (isExisting) {
        await reportingService.update(reportingId, payload);
      } else {
        await reportingService.create(payload);
      }
      setEditingReporting(null);
      await loadReportings();
    } catch (error) {
      if (error instanceof AxiosError && error.response) {
        const responseData = error.response.data;
        let serverMessage: string | undefined;
        if (responseData?.message) serverMessage = responseData.message;
        else if (Array.isArray(responseData)) serverMessage = responseData.map((i) => i.message || JSON.stringify(i)).join(', ');
        else if (responseData?.errors) serverMessage = responseData.errors.map((i: any) => `${i.field || ''}: ${i.message || ''}`).join(', ');

        setErrorMessage(`Impossible d’enregistrer : ${error.response.status} ${serverMessage || error.response.statusText}`);
      } else {
        setErrorMessage(`Erreur : ${error instanceof Error ? error.message : 'Erreur inconnue'}`);
      }
    } finally {
      setSaveLoading(false);
    }
  };

  const columns = useMemo(() => getReportingColumnsByRole(user?.role), [user?.role]);

  return (
    <div className="app-content" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--ocp-text)', margin: 0 }}>Gestion des Reportings PDR</h2>
          <p style={{ fontSize: '13px', color: 'var(--ocp-text-muted)', marginTop: '2px' }}>
            Consultez, ajoutez et éditez les dossiers de demandes d'achat et approvisionnements.
          </p>
        </div>
        {user?.role !== 'CONSULTANT' && (
          <button className="btn-ocp btn-ocp-primary" onClick={handleCreate}>
            <PlusCircle size={18} />
            <span>Nouveau Reporting</span>
          </button>
        )}
      </div>

      {editingReporting && (
        <div className="card-ocp" style={{ border: '2px solid var(--ocp-primary)', boxShadow: 'var(--shadow-lg)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--ocp-primary)', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <FileText size={20} />
              {editingReporting.id ? `Modifier le Reporting #${editingReporting.id}` : 'Nouveau Reporting PDR'}
            </h3>
            <button className="btn-ocp-icon" onClick={() => setEditingReporting(null)} title="Fermer">
              <X size={18} />
            </button>
          </div>

          {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div className="form-section-ocp">
              <h4 className="form-section-ocp__title">
                <FileText size={16} />
                Section 1 : Informations Générales
              </h4>
              <div className="form-grid-2">
                <div className="form-group-ocp">
                  <label>Numéro Dossier *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Ex: DOS-2026-001"
                    value={editingReporting.numeroDossier}
                    onChange={(e) => updateEditingField('numeroDossier', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Numéro DA (Demande d'Achat) *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Ex: DA-450098"
                    value={editingReporting.numeroDA}
                    onChange={(e) => updateEditingField('numeroDA', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Numéro N° *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Ex: 01"
                    value={editingReporting.numero}
                    onChange={(e) => updateEditingField('numero', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Code Oracle *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Ex: OR-9908"
                    value={editingReporting.codeOracle}
                    onChange={(e) => updateEditingField('codeOracle', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Code SAP *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Ex: SAP-12345"
                    value={editingReporting.codeSAP}
                    onChange={(e) => updateEditingField('codeSAP', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp" style={{ gridColumn: '1 / -1' }}>
                  <label>Description du matériel / PDR</label>
                  <textarea
                    className="form-control-ocp"
                    rows={2}
                    placeholder="Saisissez la désignation détaillée de la pièce..."
                    value={editingReporting.description ?? ''}
                    onChange={(e) => updateEditingField('description', e.target.value)}
                  />
                </div>
              </div>
            </div>

            <div className="form-section-ocp">
              <h4 className="form-section-ocp__title">
                <Package size={16} />
                Section 2 : Informations Métier & Approvisionnement
              </h4>
              <div className="form-grid-2">
                <div className="form-group-ocp">
                  <label>Fournisseur *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Nom du fournisseur"
                    value={editingReporting.fournisseur}
                    onChange={(e) => updateEditingField('fournisseur', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Secteur *</label>
                  <select
                    className="form-control-ocp"
                    value={editingReporting.secteur}
                    onChange={(e) => updateEditingField('secteur', e.target.value)}
                  >
                    {secteurOptions.map((opt) => (
                      <option key={opt} value={opt}>
                        {opt || 'Sélectionner le secteur'}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group-ocp">
                  <label>Unité de Mesure (UDM)</label>
                  <select
                    className="form-control-ocp"
                    value={editingReporting.uniteDeMesure}
                    onChange={(e) => updateEditingField('uniteDeMesure', e.target.value)}
                  >
                    {udmOptions.map((opt) => (
                      <option key={opt} value={opt}>
                        {opt || 'Sélectionner UDM'}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group-ocp">
                  <label>Quantité Retenue *</label>
                  <input
                    type="number"
                    className="form-control-ocp"
                    placeholder="Ex: 50"
                    value={editingReporting.quantite ?? ''}
                    onChange={(e) => updateEditingField('quantite', e.target.value ? parseFloat(e.target.value) : null)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>N° Commande (CMD) *</label>
                  <input
                    type="text"
                    className="form-control-ocp"
                    placeholder="Ex: CMD-8890"
                    value={editingReporting.commande}
                    onChange={(e) => updateEditingField('commande', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>% Livraison *</label>
                  <input
                    type="number"
                    className="form-control-ocp"
                    placeholder="0 - 100"
                    value={editingReporting.pourcentageLivraison ?? ''}
                    onChange={(e) => updateEditingField('pourcentageLivraison', e.target.value ? parseInt(e.target.value, 10) : null)}
                  />
                </div>
              </div>
            </div>

            <div className="form-section-ocp">
              <h4 className="form-section-ocp__title">
                <Truck size={16} />
                Section 3 : Suivi & Statut Livraison
              </h4>
              <div className="form-grid-2">
                <div className="form-group-ocp">
                  <label>Date de Notification</label>
                  <input
                    type="date"
                    className="form-control-ocp"
                    value={editingReporting.dateNotification ?? ''}
                    onChange={(e) => updateEditingField('dateNotification', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Date Prévisionnelle de Livraison</label>
                  <input
                    type="date"
                    className="form-control-ocp"
                    value={editingReporting.datePrevisionnelle ?? ''}
                    onChange={(e) => updateEditingField('datePrevisionnelle', e.target.value)}
                  />
                </div>
                <div className="form-group-ocp">
                  <label>Statut de Livraison *</label>
                  <select
                    className="form-control-ocp"
                    value={editingReporting.statut}
                    onChange={(e) => updateEditingField('statut', e.target.value)}
                  >
                    {statutOptions.map((opt) => (
                      <option key={opt} value={opt}>
                        {opt ? statusLabels[opt] || opt : 'Sélectionner le statut'}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group-ocp">
                  <label>Responsable DO *</label>
                  <select
                    className="form-control-ocp"
                    value={editingReporting.responsable}
                    onChange={(e) => updateEditingField('responsable', e.target.value)}
                  >
                    {responsableOptions.map((opt) => (
                      <option key={opt} value={opt}>
                        {opt || 'Sélectionner le responsable'}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            <div className="form-section-ocp">
              <h4 className="form-section-ocp__title">
                <MessageSquare size={16} />
                Section 4 : Remarques & Commentaires
              </h4>
              <div className="form-group-ocp">
                <label>Commentaires internes</label>
                <textarea
                  className="form-control-ocp"
                  rows={3}
                  placeholder="Notes de suivi, spécifications ou observations sur le dossier..."
                  value={editingReporting.commentaire ?? ''}
                  onChange={(e) => updateEditingField('commentaire', e.target.value)}
                />
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button type="button" className="btn-ocp btn-ocp-secondary" onClick={() => setEditingReporting(null)}>
                Annuler
              </button>
              <button type="submit" className="btn-ocp btn-ocp-primary" disabled={saveLoading}>
                <Save size={16} />
                <span>{saveLoading ? 'Enregistrement en cours...' : 'Enregistrer le Reporting'}</span>
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="card-ocp" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <form onSubmit={handleFilterSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Search size={18} color="var(--ocp-primary)" />
              <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-text)', margin: 0 }}>Filtres de Recherche</h3>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button type="button" className="btn-ocp btn-ocp-secondary" onClick={handleReset}>
                <RotateCcw size={16} />
                <span>Réinitialiser</span>
              </button>
              <button type="submit" className="btn-ocp btn-ocp-primary">
                <Search size={16} />
                <span>Appliquer</span>
              </button>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
            <div className="app-navbar__search" style={{ width: '100%' }}>
              <Search size={16} color="var(--ocp-text-muted)" />
              <input
                type="text"
                placeholder="Recherche..."
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value })}
              />
            </div>
            <input
              type="text"
              className="form-control-ocp"
              placeholder="Filtre Fournisseur"
              value={filters.fournisseur}
              onChange={(e) => setFilters({ ...filters, fournisseur: e.target.value })}
            />
            <select
              className="form-control-ocp"
              value={filters.statut}
              onChange={(e) => setFilters({ ...filters, statut: e.target.value })}
            >
              {statutOptions.map((opt) => (
                <option key={opt} value={opt}>
                  {opt ? statusLabels[opt] || opt : 'Tous les statuts'}
                </option>
              ))}
            </select>
            <select
              className="form-control-ocp"
              value={filters.secteur}
              onChange={(e) => setFilters({ ...filters, secteur: e.target.value })}
            >
              {secteurOptions.map((opt) => (
                <option key={opt} value={opt}>
                  {opt || 'Tous les secteurs'}
                </option>
              ))}
            </select>
            <select
              className="form-control-ocp"
              value={filters.responsable}
              onChange={(e) => setFilters({ ...filters, responsable: e.target.value })}
            >
              {responsableOptions.map((opt) => (
                <option key={opt} value={opt}>
                  {opt || 'Tous les responsables'}
                </option>
              ))}
            </select>
          </div>
        </form>

        <ReportingTable
          reportings={reportings}
          columns={columns}
          loading={loading}
          onEdit={user?.role !== 'CONSULTANT' ? handleEdit : undefined}
          onDelete={user?.role !== 'CONSULTANT' ? handleDelete : undefined}
        />
      </div>
    </div>
  );
};

export default ReportingPage;
