import React, { useState, useEffect } from 'react';
import type { Employee, Address } from '../services/employeeService';
import { X } from 'lucide-react';
import './Modal.css';

interface EmployeeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (employee: Partial<Employee>) => Promise<void>;
  employee?: Employee | null;
}

const EmployeeModal: React.FC<EmployeeModalProps> = ({ isOpen, onClose, onSave, employee }) => {
  const [formData, setFormData] = useState<Partial<Employee>>({
    username: '',
    email: '',
    salary: 0,
    levelNo: 1,
    address: { city: '', state: '', country: '', pincode: '' }
  });
  const [loading, setLoading] = useState(false);
  const [password, setPassword] = useState('');

  useEffect(() => {
    if (employee) {
      setFormData(employee);
    } else {
      setFormData({
        username: '',
        email: '',
        salary: 0,
        levelNo: 1,
        address: { city: '', state: '', country: '', pincode: '' }
      });
      setPassword('');
    }
  }, [employee, isOpen]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name.startsWith('address.')) {
      const addressField = name.split('.')[1];
      setFormData((prev) => ({
        ...prev,
        address: {
          ...prev.address!,
          [addressField]: value
        }
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: name === 'salary' || name === 'levelNo' ? Number(value) : value
      }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const dataToSave = { ...formData };
      if (!employee && password) {
        (dataToSave as any).password = password;
      }
      await onSave(dataToSave);
      onClose();
    } catch (err) {
      console.error(err);
      alert('Failed to save employee');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel animate-fade-in">
        <div className="modal-header">
          <h2>{employee ? 'Edit Employee' : 'Add Employee'}</h2>
          <button onClick={onClose} className="icon-btn" title="Close">
            <X size={20} />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" value={formData.username || ''} onChange={handleChange} required className="input-field" />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" name="email" value={formData.email || ''} onChange={handleChange} required className="input-field" />
          </div>
          {!employee && (
            <div className="form-group">
              <label>Password</label>
              <input type="password" name="password" value={password} onChange={(e) => setPassword(e.target.value)} required className="input-field" />
            </div>
          )}
          <div className="form-row">
            <div className="form-group">
              <label>Salary</label>
              <input type="number" name="salary" value={formData.salary || 0} onChange={handleChange} className="input-field" />
            </div>
            <div className="form-group">
              <label>Level</label>
              <input type="number" name="levelNo" value={formData.levelNo || 1} onChange={handleChange} className="input-field" />
            </div>
          </div>
          
          <h4>Address</h4>
          <div className="form-row">
            <div className="form-group">
              <label>City</label>
              <input type="text" name="address.city" value={formData.address?.city || ''} onChange={handleChange} className="input-field" />
            </div>
            <div className="form-group">
              <label>State</label>
              <input type="text" name="address.state" value={formData.address?.state || ''} onChange={handleChange} className="input-field" />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Country</label>
              <input type="text" name="address.country" value={formData.address?.country || ''} onChange={handleChange} className="input-field" />
            </div>
            <div className="form-group">
              <label>Pincode</label>
              <input type="text" name="address.pincode" value={formData.address?.pincode || ''} onChange={handleChange} className="input-field" />
            </div>
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn btn-secondary">Cancel</button>
            <button type="submit" disabled={loading} className="btn btn-primary">{loading ? 'Saving...' : 'Save'}</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EmployeeModal;
