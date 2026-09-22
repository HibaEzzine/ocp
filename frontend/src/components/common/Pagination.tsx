import React from 'react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, index) => index + 1);

  return (
    <nav aria-label="Pagination">
      <ul className="ui-pagination">
        <li className={`ui-pagination__item ${currentPage === 1 ? 'ui-pagination__item--disabled' : ''}`}>
          <button
            type="button"
            className="ui-pagination__link"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 1}
          >
            Précédent
          </button>
        </li>
        {pages.map((page) => (
          <li key={page} className={`ui-pagination__item ${page === currentPage ? 'ui-pagination__item--active' : ''}`}>
            <button type="button" className="ui-pagination__link" onClick={() => onPageChange(page)}>
              {page}
            </button>
          </li>
        ))}
        <li className={`ui-pagination__item ${currentPage === totalPages ? 'ui-pagination__item--disabled' : ''}`}>
          <button
            type="button"
            className="ui-pagination__link"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
          >
            Suivant
          </button>
        </li>
      </ul>
    </nav>
  );
};

export default React.memo(Pagination);
