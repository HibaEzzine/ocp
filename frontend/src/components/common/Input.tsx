import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  className?: string;
}

const Input = React.memo(({ label, error, className = '', ...props }: InputProps) => (
  <div className={`ui-form-group ${className}`}>
    {label && (
      <label htmlFor={props.id} className="ui-form-group__label">
        {label}
      </label>
    )}
    <input {...props} className={`ui-form-control${error ? ' ui-form-control--invalid' : ''}`} />
    {error && <div className="ui-form-feedback">{error}</div>}
  </div>
));

export default Input;
