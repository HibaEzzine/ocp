import React, { useEffect, useState } from 'react';
import historiqueService from '../../services/historiqueService';
import Badge from '../../components/common/Badge';
import Loader from '../../components/common/Loader';
import { History, Calendar, FileText } from 'lucide-react';

interface HistoriqueDto {
  id: number;
  action: string;
  ancienneValeur: string;
  nouvelleValeur: string;
  dateAction: string;
  reportingId: number;
}

const HistoriquePage: React.FC = () => {
  const [historiques, setHistoriques] = useState<HistoriqueDto[]>([]);
  const [loading, setLoading] = useState(false);

  const loadHistoriques = async () => {
    setLoading(true);
    try {
      const response = await historiqueService.getAll();
      setHistoriques(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistoriques();
  }, []);

  return (
    <div className="app-content" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--ocp-text)' }}>Journal d'Audit & Historique</h2>
        </div>
        <Badge label={`${historiques.length} événement(s)`} variant="info" />
      </div>

      <div className="card-ocp" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <History size={18} color="var(--ocp-primary)" />
          <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-text)' }}>Activités Récents</h3>
        </div>

        {loading ? (
          <Loader type="skeleton-table" rows={6} />
        ) : (
          <div className="table-responsive-ocp">
            <table className="table-ocp">
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Ancienne Valeur</th>
                  <th>Nouvelle Valeur</th>
                  <th>N° Reporting</th>
                  <th>Date & Heure</th>
                </tr>
              </thead>
              <tbody>
                {historiques.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', padding: '30px', color: 'var(--ocp-text-muted)' }}>
                      Aucun historique d'audit enregistré.
                    </td>
                  </tr>
                ) : (
                  historiques.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <span className="badge-ocp badge-ocp--encours">
                          {item.action}
                        </span>
                      </td>
                      <td style={{ color: 'var(--ocp-danger)', fontWeight: 500 }}>
                        {item.ancienneValeur || '—'}
                      </td>
                      <td style={{ color: 'var(--ocp-success)', fontWeight: 500 }}>
                        {item.nouvelleValeur || '—'}
                      </td>
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontWeight: 600 }}>
                          <FileText size={14} color="var(--ocp-primary)" />
                          #{item.reportingId}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: '12px', color: 'var(--ocp-text-muted)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <Calendar size={13} />
                          {item.dateAction ? new Date(item.dateAction).toLocaleString() : '—'}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default HistoriquePage;
