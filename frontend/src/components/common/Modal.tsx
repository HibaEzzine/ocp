import React from 'react';

interface ModalProps {
  title: string;
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  footer?: React.ReactNode;
  className?: string;
}

const Modal: React.FC<ModalProps> = ({ title, isOpen, onClose, children, footer, className = '' }) => {
  if (!isOpen) return null;

  return (
    <div className={`ui-modal-backdrop ${className}`} role="dialog" aria-modal="true" onClick={onClose}>
      <div className="ui-modal ui-card ui-card--transparent" onClick={(event) => event.stopPropagation()}>
        <div className="ui-modal__header">
          <h5 className="ui-modal__title">{title}</h5>
          <button type="button" className="ui-modal__close" aria-label="Fermer" onClick={onClose}>
            
          </button>
        </div>
        <div className="ui-modal__body">{children}</div>
        {footer && <div className="ui-modal__footer">{footer}</div>}
      </div>
    </div>
  );
};

export default React.memo(Modal);
