import React from 'react';
import { X, AlertTriangle } from 'lucide-react';
import './Modal.css';

interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
}

const ConfirmModal: React.FC<ConfirmModalProps> = ({ isOpen, onClose, onConfirm, title, message }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel animate-fade-in" style={{ maxWidth: '400px', textAlign: 'center' }}>
        <div className="modal-header" style={{ justifyContent: 'center' }}>
          <AlertTriangle size={32} color="#f87171" style={{ marginBottom: '1rem' }} />
        </div>
        <h2 style={{ marginBottom: '1rem' }}>{title}</h2>
        <p style={{ marginBottom: '2rem', color: 'var(--text-muted)' }}>{message}</p>
        <div className="modal-actions" style={{ justifyContent: 'center' }}>
          <button onClick={onClose} className="btn btn-secondary">Cancel</button>
          <button onClick={() => { onConfirm(); onClose(); }} className="btn btn-danger">Confirm</button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
