import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { employeeService, type Employee, type PageResponse } from '../services/employeeService';
import { LogOut, Users, Search, TrendingUp, Trash2, MapPin, Plus, Edit2, ChevronLeft, ChevronRight, XCircle } from 'lucide-react';
import EmployeeModal from '../components/EmployeeModal';
import HikeModal from '../components/HikeModal';
import ConfirmModal from '../components/ConfirmModal';
import './Dashboard.css';

const Dashboard: React.FC = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [size] = useState(6); // cards per page
  const [isSearching, setIsSearching] = useState(false);

  // Modals state
  const [isEmpModalOpen, setIsEmpModalOpen] = useState(false);
  const [empToEdit, setEmpToEdit] = useState<Employee | null>(null);
  
  const [isHikeModalOpen, setIsHikeModalOpen] = useState(false);
  const [empForHike, setEmpForHike] = useState<{id: number, name: string} | null>(null);

  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [empToDelete, setEmpToDelete] = useState<number | null>(null);

  const isAdminOrManager = user?.roles?.some(r => r.name === 'ROLE_ADMIN' || r.name === 'ROLE_MANAGER') || false;
  const isAdmin = user?.roles?.some(r => r.name === 'ROLE_ADMIN') || false;

  useEffect(() => {
    if (isAdminOrManager && !isSearching) {
      loadEmployees();
    } else if (!isAdminOrManager) {
      setLoading(false);
    }
  }, [isAdminOrManager, page, isSearching]);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      const data: PageResponse<Employee> = await (employeeService as any).getAllEmployees(page, size, 'id', 'asc');
      setEmployees(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Failed to load employees', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchTerm.trim()) {
      setIsSearching(false);
      setPage(0);
      loadEmployees();
      return;
    }
    try {
      setLoading(true);
      setIsSearching(true);
      const data = await employeeService.searchByCity(searchTerm);
      setEmployees(data);
      setTotalPages(1); // Search returns a list, not paginated in current backend implementation
    } catch (error) {
      console.error('Search failed', error);
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };
  
  const clearSearch = () => {
    setSearchTerm('');
    setIsSearching(false);
    setPage(0);
  };

  const handleSaveEmployee = async (empData: Partial<Employee>) => {
    if (empToEdit) {
      await employeeService.updateEmployee(empToEdit.id, empData);
    } else {
      await employeeService.createEmployee(empData);
    }
    if (!isSearching) loadEmployees();
  };

  const handleApplyHike = async (percentage: number) => {
    if (empForHike) {
      await employeeService.applyHike(empForHike.id, percentage);
      if (!isSearching) loadEmployees();
      else handleSearch({ preventDefault: () => {} } as any);
    }
  };

  const confirmDelete = async () => {
    if (empToDelete) {
      await employeeService.deleteEmployee(empToDelete);
      if (!isSearching) loadEmployees();
      else handleSearch({ preventDefault: () => {} } as any);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-layout">
      {/* Sidebar */}
      <aside className="sidebar glass-panel">
        <div className="sidebar-header">
          <div className="avatar">{user?.username.charAt(0).toUpperCase()}</div>
          <div className="user-info">
            <h3>{user?.username}</h3>
            <span className="role-badge">{user?.roles?.[0]?.name.replace('ROLE_', '') || 'GUEST'}</span>
          </div>
        </div>
        
        <nav className="sidebar-nav">
          <a href="#" className="nav-item active">
            <Users size={20} />
            <span>Employees</span>
          </a>
        </nav>
        
        <div className="sidebar-footer">
          <button onClick={handleLogout} className="btn btn-secondary w-100">
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="topbar">
          <div className="topbar-title">
            <h1>Employee Directory</h1>
            {isAdmin && (
              <button 
                className="btn btn-primary" 
                onClick={() => { setEmpToEdit(null); setIsEmpModalOpen(true); }}
                style={{ marginLeft: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
              >
                <Plus size={16} /> Add Employee
              </button>
            )}
          </div>
          
          {isAdminOrManager && (
            <form onSubmit={handleSearch} className="search-bar">
              <Search size={18} className="search-icon" />
              <input 
                type="text" 
                placeholder="Search by city..." 
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-field"
              />
              {isSearching && (
                <button type="button" onClick={clearSearch} className="icon-btn" style={{ position: 'absolute', right: '10px' }}>
                  <XCircle size={16} />
                </button>
              )}
            </form>
          )}
        </header>

        <div className="content-area">
          {!isAdminOrManager ? (
            <div className="glass-card empty-state">
              <h2>Welcome to the portal</h2>
              <p>You are logged in as a standard employee. You do not have permission to view the directory.</p>
            </div>
          ) : loading ? (
            <div className="loading-spinner">
               <div className="spinner"></div>
               <p>Loading data...</p>
            </div>
          ) : (
            <div className="employee-wrapper">
              <div className="employee-grid">
                {employees.map(emp => (
                  <div key={emp.id} className="glass-card employee-card animate-scale-up">
                    <div className="emp-card-header">
                      <div className="emp-avatar">{emp.username.charAt(0).toUpperCase()}</div>
                      <div className="emp-title">
                        <h3>{emp.username}</h3>
                        <span>{emp.email}</span>
                      </div>
                      <div className="emp-badges">
                        {emp.roles?.map(r => (
                           <span key={r.id} className="small-badge">{r.name.replace('ROLE_', '')}</span>
                        ))}
                      </div>
                    </div>
                    
                    <div className="emp-details">
                      <div className="detail-row">
                        <span className="label">Salary:</span>
                        <span className="value" style={{color: 'var(--primary-light)', fontWeight: 600}}>${emp.salary ? emp.salary.toLocaleString() : '0'}</span>
                      </div>
                      {emp.address && emp.address.city && (
                        <div className="detail-row">
                          <MapPin size={16} className="text-muted" />
                          <span className="value">{emp.address.city}, {emp.address.country}</span>
                        </div>
                      )}
                    </div>

                    <div className="emp-actions">
                      <div className="actions-left">
                        {isAdminOrManager && (
                          <button onClick={() => { setEmpToEdit(emp); setIsEmpModalOpen(true); }} className="btn btn-secondary action-btn" title="Edit Employee">
                            <Edit2 size={16} />
                          </button>
                        )}
                        {isAdminOrManager && (
                          <button onClick={() => { setEmpForHike({id: emp.id, name: emp.username}); setIsHikeModalOpen(true); }} className="btn btn-secondary action-btn" title="Apply Hike">
                            <TrendingUp size={16} />
                          </button>
                        )}
                      </div>
                      <div className="actions-right">
                        {isAdmin && (
                          <button onClick={() => { setEmpToDelete(emp.id); setIsConfirmOpen(true); }} className="btn btn-danger action-btn" title="Delete">
                            <Trash2 size={16} />
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
                
                {employees.length === 0 && (
                  <div className="empty-state">No employees found.</div>
                )}
              </div>

              {!isSearching && totalPages > 1 && (
                <div className="pagination">
                  <button 
                    disabled={page === 0} 
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    className="btn btn-secondary icon-btn"
                  >
                    <ChevronLeft size={20} />
                  </button>
                  <span className="page-info">Page {page + 1} of {totalPages}</span>
                  <button 
                    disabled={page >= totalPages - 1} 
                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    className="btn btn-secondary icon-btn"
                  >
                    <ChevronRight size={20} />
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </main>

      <EmployeeModal 
        isOpen={isEmpModalOpen} 
        onClose={() => setIsEmpModalOpen(false)} 
        onSave={handleSaveEmployee} 
        employee={empToEdit} 
      />

      <HikeModal 
        isOpen={isHikeModalOpen} 
        onClose={() => setIsHikeModalOpen(false)} 
        onApply={handleApplyHike} 
        employeeName={empForHike?.name || ''} 
      />

      <ConfirmModal 
        isOpen={isConfirmOpen} 
        onClose={() => setIsConfirmOpen(false)} 
        onConfirm={confirmDelete} 
        title="Delete Employee" 
        message="Are you sure you want to delete this employee? This action cannot be undone." 
      />
    </div>
  );
};

export default Dashboard;
