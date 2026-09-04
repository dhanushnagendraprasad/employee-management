import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authService } from '../services/authService';
import { LogIn, User, Lock, AlertCircle } from 'lucide-react';
import './Login.css';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();
  const setAuth = useAuthStore(state => state.setAuth);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const data = await authService.login({ username, password });
      
      // Extract roles from JWT payload since backend doesn't return them in JwtResponse
      let roles: {id: number, name: string}[] = [];
      try {
        const payload = JSON.parse(atob(data.token.split('.')[1]));
        if (payload.roles) {
          roles = payload.roles.map((r: string, i: number) => ({ id: i, name: r }));
        }
      } catch (e) {
        console.error('Failed to parse JWT');
      }

      // Map response to store format
      setAuth(data.token, {
        id: data.id || 0,
        username: data.username,
        email: data.email || '',
        roles: roles
      });
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data || 'Invalid username or password');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card glass-panel animate-fade-in">
        <div className="login-header">
          <div className="logo-icon">
            <LogIn size={32} color="var(--primary)" />
          </div>
          <h2>Welcome Back</h2>
          <p>Sign in to access the employee portal</p>
        </div>

        {error && (
          <div className="alert alert-danger animate-fade-in">
            <AlertCircle size={18} />
            <span>{error}</span>
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
                placeholder="Enter your username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
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
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary login-btn" 
            disabled={isLoading}
          >
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
          
          <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            Don't have an account? <Link to="/signup" style={{ color: 'var(--primary)', textDecoration: 'none' }}>Sign Up</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Login;
