import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService, type SignupRequest } from '../services/authService';
import { User, Lock, Mail, AlertCircle, CheckCircle } from 'lucide-react';
import './Login.css'; // Reusing login styles

const Signup: React.FC = () => {
  const [formData, setFormData] = useState<SignupRequest>({
    username: '',
    email: '',
    password: '',
    role: 'ROLE_ADMIN' // Default to admin for testing, or could be employee
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { id, value } = e.target;
    setFormData(prev => ({ ...prev, [id]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await authService.register(formData);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data || 'Failed to register account');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card glass-panel animate-fade-in" style={{ maxWidth: '500px' }}>
        <div className="login-header">
          <div className="logo-icon">
            <User size={32} color="var(--primary)" />
          </div>
          <h2>Create an Account</h2>
          <p>Join the employee portal</p>
        </div>

        {error && (
          <div className="alert alert-danger animate-fade-in">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="alert animate-fade-in" style={{ background: 'rgba(34, 197, 94, 0.1)', color: '#4ade80', border: '1px solid rgba(34, 197, 94, 0.2)' }}>
            <CheckCircle size={18} />
            <span>Account created successfully! Redirecting...</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="input-group">
            <label className="input-label" htmlFor="username">Username</label>
            <div className="input-with-icon">
              <User size={18} className="input-icon" />
              <input
                id="username"
                type="text"
                className="input-field has-icon"
                placeholder="Enter a username"
                value={formData.username}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label" htmlFor="email">Email Address</label>
            <div className="input-with-icon">
              <Mail size={18} className="input-icon" />
              <input
                id="email"
                type="email"
                className="input-field has-icon"
                placeholder="Enter your email"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label" htmlFor="password">Password</label>
            <div className="input-with-icon">
              <Lock size={18} className="input-icon" />
              <input
                id="password"
                type="password"
                className="input-field has-icon"
                placeholder="Create a password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label" htmlFor="role">Role</label>
            <select id="role" className="input-field" value={formData.role} onChange={handleChange}>
              <option value="ROLE_EMPLOYEE">Employee</option>
              <option value="ROLE_MANAGER">Manager</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary login-btn" 
            disabled={isLoading || success}
          >
            {isLoading ? 'Creating account...' : 'Sign Up'}
          </button>

          <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            Already have an account? <Link to="/login" style={{ color: 'var(--primary)', textDecoration: 'none' }}>Sign In</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Signup;
