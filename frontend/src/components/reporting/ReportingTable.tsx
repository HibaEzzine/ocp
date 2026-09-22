import React from 'react';
import type { ReportingDto } from '../../types/reporting';
import type { ReportingColumn } from '../../utils/reportingColumns';
import { getReportingCellValue } from '../../utils/reportingColumns';
import Badge from '../common/Badge';
import { Edit3, Trash2, Eye } from 'lucide-react';

type TableColumn = ReportingColumn | { key: 'actions'; label: string };

interface ReportingTableProps {
  reportings: ReportingDto[];
  columns: ReportingColumn[];
  loading?: boolean;
  onEdit?: (id?: number) => void;
  onDelete?: (id?: number) => void;
  onView?: (item: ReportingDto) => void;
  className?: string;
}

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

const statusBadgeVariantMap: Record<string, any> = {
  EN_COURS: 'encours',
  ATTENTE_LIVRAISON: 'attente',
  LIVRE: 'livre',
  ECARTE: 'ecarte',
  ADJUGE: 'adjuge',
  LITIGE: 'litige',
  CONTENTIEUX: 'litige',
  ANNULE: 'annule',
};

const ReportingTable: React.FC<ReportingTableProps> = ({
  reportings,
  columns,
  loading = false,
  onEdit,
  onDelete,
  onView,
  className = '',
}) => {
  const hasActions = Boolean(onEdit || onDelete || onView);
  const tableColumns: TableColumn[] = hasActions ? [...columns, { key: 'actions', label: 'Actions' }] : columns;

  const renderCell = (columnKey: string, item: ReportingDto) => {
    if (columnKey === 'actions') {
      return (
        <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
          {onView && (
            <button
              type="button"
              className="btn-ocp-icon"
              onClick={() => onView(item)}
              title="Consulter les détails"
            >
              <Eye size={15} />
            </button>
          )}
          {onEdit && (
            <button
              type="button"
              className="btn-ocp-icon"
              onClick={() => onEdit(item.id)}
              title="Modifier"
            >
              <Edit3 size={15} />
            </button>
          )}
          {onDelete && (
            <button
              type="button"
              className="btn-ocp-icon danger"
              onClick={() => onDelete(item.id)}
              title="Supprimer"
            >
              <Trash2 size={15} />
            </button>
          )}
        </div>
      );
    }

    if (columnKey === 'statut' && item.statut) {
      const variant = statusBadgeVariantMap[item.statut] || 'encours';
      const rawVal = getReportingCellValue(columnKey as any, item);
      const val = rawVal !== null && rawVal !== undefined ? String(rawVal) : '';
      const displayLabel = statusLabels[item.statut] || val;
      return <Badge label={displayLabel} variant={variant} />;
    }

    return getReportingCellValue(columnKey as any, item);
  };

  return (
    <div className={`table-responsive-ocp ${className}`}>
      <table className="table-ocp">
        <thead>
          <tr>
            {tableColumns.map((col) => (
              <th key={col.key}>{col.label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={tableColumns.length} style={{ textAlign: 'center', padding: '30px' }}>
                Chargement...
              </td>
            </tr>
          ) : reportings.length === 0 ? (
            <tr>
              <td colSpan={tableColumns.length} style={{ textAlign: 'center', padding: '30px', color: 'var(--ocp-text-muted)' }}>
                Aucune donnée disponible.
              </td>
            </tr>
          ) : (
            reportings.map((item) => (
              <tr key={item.id ?? `${item.numeroDA}-${item.numeroDossier}-${item.numero}`}>
                {tableColumns.map((column) => (
                  <td key={column.key}>{renderCell(column.key, item)}</td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default React.memo(ReportingTable);
