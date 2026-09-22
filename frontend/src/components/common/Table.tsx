import React from 'react';

interface TableProps {
  columns: Array<{ key: string; label: string }>;
  children: React.ReactNode;
  className?: string;
}

const Table: React.FC<TableProps> = ({ columns, children, className = '' }) => (
  <div className={`app-table-responsive ${className}`}>
    <table className="app-table app-table--striped">
      <thead>
        <tr>
          {columns.map((column) => (
            <th key={column.key}>{column.label}</th>
          ))}
        </tr>
      </thead>
      <tbody>{children}</tbody>
    </table>
  </div>
);

export default React.memo(Table);
