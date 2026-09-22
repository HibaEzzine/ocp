import React, { useEffect, useMemo, useState } from 'react';
import reportingService from '../../services/reportingService';
import type { ReportingDto } from '../../types/reporting';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../../components/common/StatCard';
import Badge from '../../components/common/Badge';
import Alert from '../../components/common/Alert';
import Loader from '../../components/common/Loader';
import ReportingTable from '../../components/reporting/ReportingTable';
import { getReportingColumnsByRole } from '../../utils/reportingColumns';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import {
  FileSpreadsheet,
  RotateCcw,
  Search,
  Filter,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Layers,
  PackageCheck,
  Building2,
} from 'lucide-react';

const statutOptions = ['', 'EN_COURS', 'ATTENTE_LIVRAISON', 'LIVRE', 'ECARTE', 'ADJUGE', 'LITIGE', 'ANNULE'];
const secteurOptions = ['', 'AMMONIAC', 'SOUFRE', 'EXPORT', 'COMMUN'];
const responsableOptions = ['', 'ATTOUCHI', 'BELYAZID', 'REGUIG', 'EL_HARKI'];

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

const COLORS = ['#00843D', '#2563EB', '#F59E0B', '#DC2626', '#0D9488', '#8B5CF6', '#64748B'];

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [reportings, setReportings] = useState<ReportingDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [filters, setFilters] = useState({
    search: '',
    statut: '',
    secteur: '',
    responsable: '',
    fournisseur: '',
    commande: '',
    sort: '',
  });

  const loadReportings = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const params: Record<string, string> = {};
      if (filters.search) params.search = filters.search;
      if (filters.statut) params.statut = filters.statut;
      if (filters.secteur) params.secteur = filters.secteur;
      if (filters.responsable) params.responsable = filters.responsable;
      if (filters.fournisseur) params.fournisseur = filters.fournisseur;
      if (filters.commande) params.commande = filters.commande;
      if (filters.sort) params.sort = filters.sort;
      const response = await reportingService.getAll(params);
      setReportings(response.data);
    } catch (error) {
      setErrorMessage('Impossible de charger les reportings.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReportings();
  }, [filters]);

  const handleReset = () => {
    setFilters({ search: '', statut: '', secteur: '', responsable: '', fournisseur: '', commande: '', sort: '' });
  };

  const handleExport = async () => {
    try {
      const params: Record<string, string> = {};
      if (filters.search) params.search = filters.search;
      if (filters.statut) params.statut = filters.statut;
      if (filters.secteur) params.secteur = filters.secteur;
      if (filters.responsable) params.responsable = filters.responsable;
      if (filters.fournisseur) params.fournisseur = filters.fournisseur;
      if (filters.commande) params.commande = filters.commande;
      if (filters.sort) params.sort = filters.sort;
      params.format = 'xlsx';

      const response = await reportingService.exportReportings(params);

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Reportings_OCP_${new Date().toISOString().slice(0, 10)}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.parentNode?.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error: any) {
      const status = error?.response?.status;
      if (status === 401) {
        setErrorMessage('Authentification requise pour l’export. Veuillez vous reconnecter.');
      } else if (status === 403) {
        setErrorMessage('Vous n’êtes pas autorisé(e) à exporter les reportings.');
      } else {
        setErrorMessage('Impossible de télécharger l’export Excel.');
      }
    }
  };

  const statusCounts = useMemo(() => {
    const counts: Record<string, number> = {
      EN_COURS: 0,
      ATTENTE_LIVRAISON: 0,
      LIVRE: 0,
      ECARTE: 0,
      ADJUGE: 0,
      LITIGE: 0,
      ANNULE: 0,
    };
    reportings.forEach((report) => {
      if (report.statut in counts) {
        counts[report.statut] += 1;
      }
    });
    return counts;
  }, [reportings]);

  const secteurData = useMemo(() => {
    const counts: Record<string, number> = {};
    reportings.forEach((r) => {
      const sec = r.secteur || 'Non défini';
      counts[sec] = (counts[sec] || 0) + 1;
    });
    return Object.keys(counts).map((key) => ({ name: key, total: counts[key] }));
  }, [reportings]);

  const pieChartData = useMemo(() => {
    return Object.keys(statusCounts)
      .map((key) => ({
        name: statusLabels[key] || key,
        value: statusCounts[key],
      }))
      .filter((item) => item.value > 0);
  }, [statusCounts]);

  const totalCount = reportings.length;
  const columns = React.useMemo(() => getReportingColumnsByRole(user?.role), [user?.role]);

  return (
    <div className="app-content" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--ocp-text)' }}>Tableau de bord PDR</h2>
        </div>
        {user?.role !== 'CONSULTANT' && (
          <div style={{ display: 'flex', gap: '10px' }}>
            <button className="btn-ocp btn-ocp-secondary" onClick={handleReset}>
              <RotateCcw size={16} />
              <span>Réinitialiser</span>
            </button>
            <button className="btn-ocp btn-ocp-primary" onClick={handleExport}>
              <FileSpreadsheet size={16} />
              <span>Exporter (.xlsx)</span>
            </button>
          </div>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
        <StatCard
          title="Total Dossiers PDR"
          value={totalCount}
          icon={<Layers size={22} />}
          variant="primary"
        />
        <StatCard
          title="Livraisons Effectuées"
          value={statusCounts.LIVRE}
          icon={<CheckCircle2 size={22} />}
          variant="success"
        />
        <StatCard
          title="Commandes en Attente"
          value={statusCounts.ATTENTE_LIVRAISON}
          icon={<Clock size={22} />}
          variant="warning"
        />
        <StatCard
          title="En cours de Traitement"
          value={statusCounts.EN_COURS}
          icon={<PackageCheck size={22} />}
          variant="info"
        />
        <StatCard
          title="Litiges & Anomalies"
          value={statusCounts.LITIGE + statusCounts.ECARTE}
          icon={<AlertTriangle size={22} />}
          variant="danger"
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '20px' }}>
        <div className="card-ocp">
          <div style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Layers size={18} color="var(--ocp-primary)" />
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-text)' }}>
              Répartition par Statut de Livraison
            </h3>
          </div>
          <div style={{ width: '100%', height: 260 }}>
            {pieChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieChartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {pieChartData.map((_entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend verticalAlign="bottom" height={36} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ textAlign: 'center', padding: '60px', color: 'var(--ocp-text-muted)' }}>
                Aucune donnée à afficher
              </div>
            )}
          </div>
        </div>

        <div className="card-ocp">
          <div style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Building2 size={18} color="var(--ocp-primary)" />
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-text)' }}>
              Volume de Dossiers par Secteur OCP
            </h3>
          </div>
          <div style={{ width: '100%', height: 260 }}>
            {secteurData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={secteurData} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--ocp-border-light)" />
                  <XAxis dataKey="name" stroke="var(--ocp-text-muted)" fontSize={12} />
                  <YAxis stroke="var(--ocp-text-muted)" fontSize={12} />
                  <Tooltip />
                  <Bar dataKey="total" fill="var(--ocp-primary)" radius={[6, 6, 0, 0]} name="Dossiers" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ textAlign: 'center', padding: '60px', color: 'var(--ocp-text-muted)' }}>
                Aucune donnée à afficher
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="card-ocp" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Filter size={18} color="var(--ocp-primary)" />
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-text)' }}>
              Filtres & Recherche
            </h3>
          </div>
          <Badge label={`${totalCount} dossier(s) trouvé(s)`} variant="info" />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
          <div className="app-navbar__search" style={{ width: '100%' }}>
            <Search size={16} color="var(--ocp-text-muted)" />
            <input
              type="text"
              placeholder="Recherche (N° DA, N° CMD, Code SAP, Fournisseur...)"
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
                {opt ? `Statut: ${statusLabels[opt] || opt}` : 'Tous les statuts'}
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
                {opt ? `Secteur: ${opt}` : 'Tous les secteurs'}
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
                {opt ? `Responsable: ${opt}` : 'Tous les responsables'}
              </option>
            ))}
          </select>

          <select
            className="form-control-ocp"
            value={filters.sort}
            onChange={(e) => setFilters({ ...filters, sort: e.target.value })}
          >
            <option value="">Trier par défaut</option>
            <option value="id,asc">Identifiant (Croissant)</option>
            <option value="id,desc">Identifiant (Décroissant)</option>
          </select>
        </div>

        {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

        {loading ? (
          <Loader type="skeleton-table" rows={5} />
        ) : (
          <ReportingTable reportings={reportings} columns={columns} loading={loading} />
        )}
      </div>
    </div>
  );
};

export default Dashboard;
