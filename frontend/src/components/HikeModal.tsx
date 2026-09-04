import React, { useState } from 'react';
import { X } from 'lucide-react';
import './Modal.css';

interface HikeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApply: (percentage: number) => Promise<void>;
  employeeName: string;
}

const HikeModal: React.FC<HikeModalProps> = ({ isOpen, onClose, onApply, employeeName }) => {
  const [percentage, setPercentage] = useState<number | ''>('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (percentage === '' || isNaN(Number(percentage))) return;
    
    setLoading(true);
    try {
      await onApply(Number(percentage));
      setPercentage('');
      onClose();
    } catch (err) {
      alert('Failed to apply hike');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel animate-fade-in" style={{ maxWidth: '400px' }}>
        <div className="modal-header">
          <h2>Apply Hike to {employeeName}</h2>
          <button onClick={onClose} className="icon-btn">
            <X size={20} />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Hike Percentage (%)</label>
            <input 
              type="number" 
              value={percentage} 
              onChange={(e) => setPercentage(e.target.value === '' ? '' : Number(e.target.value))} 
              required 
              min="0"
              step="0.1"
              className="input-field" 
            />
          </div>
          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn btn-secondary">Cancel</button>
            <button type="submit" disabled={loading} className="btn btn-primary">{loading ? 'Applying...' : 'Apply Hike'}</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default HikeModal;
