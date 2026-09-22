export interface ReportingDto {
  id?: number;
  numeroDA: string;
  numeroDossier: string;
  numero: string;
  codeOracle: string;
  codeSAP: string;
  description?: string;
  uniteDeMesure: string;
  quantite: number | null;
  secteur: string;
  commande: string;
  fournisseur: string;
  pourcentageLivraison: number | null;
  delaiLivraison: number | null;
  dateNotification: string;
  datePrevisionnelle: string;
  statut: string;
  responsable: string;
  utilisateurId?: number | null;
  commentaire?: string;
  dateCreation?: string;
  dateModification?: string;
}
