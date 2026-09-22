import type { ReportingDto } from '../types/reporting';

export type ReportingColumnKey = keyof ReportingDto;

export interface ReportingColumn {
  key: ReportingColumnKey;
  label: string;
}

export const REPORTING_COLUMNS: ReportingColumn[] = [
  { key: 'id', label: 'ID' },
  { key: 'numeroDA', label: 'N° DA' },
  { key: 'numeroDossier', label: 'N° dossier' },
  { key: 'numero', label: 'N°' },
  { key: 'codeOracle', label: 'Code Oracle' },
  { key: 'codeSAP', label: 'Code SAP' },
  { key: 'description', label: 'Description' },
  { key: 'uniteDeMesure', label: 'UDM' },
  { key: 'quantite', label: 'Quantité' },
  { key: 'secteur', label: 'Secteur' },
  { key: 'commande', label: 'Commande' },
  { key: 'fournisseur', label: 'Fournisseur' },
  { key: 'pourcentageLivraison', label: '% Livraison' },
  { key: 'delaiLivraison', label: 'Délai livraison' },
  { key: 'dateNotification', label: 'Date notification' },
  { key: 'datePrevisionnelle', label: 'Date prévisionnelle' },
  { key: 'statut', label: 'Statut' },
  { key: 'responsable', label: 'Responsable' },
  { key: 'commentaire', label: 'Commentaire' },
];

const HIDDEN_COLUMNS_BY_ROLE: Record<string, ReportingColumnKey[]> = {
  CONSULTANT: ['dateNotification', 'datePrevisionnelle', 'numeroDA', 'numeroDossier'],
};

export const getReportingColumnsByRole = (role?: string): ReportingColumn[] => {
  const hiddenColumns = new Set<ReportingColumnKey>(HIDDEN_COLUMNS_BY_ROLE[role ?? ''] ?? []);
  return REPORTING_COLUMNS.filter((column) => !hiddenColumns.has(column.key));
};

export const getReportingCellValue = (columnKey: ReportingColumnKey, item: ReportingDto): string | number | null => {
  switch (columnKey) {
    case 'pourcentageLivraison':
      return item.pourcentageLivraison != null ? `${item.pourcentageLivraison}%` : '';
    case 'quantite':
      return item.quantite != null ? item.quantite : '';
    case 'delaiLivraison':
      return item.delaiLivraison != null ? item.delaiLivraison : '';
    case 'dateNotification':
    case 'datePrevisionnelle':
      return item[columnKey] ? item[columnKey] : '';
    default:
      return item[columnKey] != null ? item[columnKey] : '';
  }
};
