import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { employeeService, type Employee, type PageResponse } from '../services/employeeService';
import { LogOut, Users, Search, TrendingUp, Trash2, MapPin, Plus, Edit2, ChevronLeft, ChevronRight, XCircle, Download, CheckSquare } from 'lucide-react';
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
  
  // Filtering
  const [filterType, setFilterType] = useState('city');
  const [isSearching, setIsSearching] = useState(false);

  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [size] = useState(6);

  // Modals state
  const [isEmpModalOpen, setIsEmpModalOpen] = useState(false);
  const [empToEdit, setEmpToEdit] = useState<Employee | null>(null);
  
  const [isHikeModalOpen, setIsHikeModalOpen] = useState(false);
  const [empForHike, setEmpForHike] = useState<{id: number, name: string} | null>(null);

  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [empToDelete, setEmpToDelete] = useState<number | null>(null);

  // Bulk Actions
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [isBulkHikeOpen, setIsBulkHikeOpen] = useState(false);
  const [isBulkDeleteOpen, setIsBulkDeleteOpen] = useState(false);

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
      clearSearch();
      return;
    }
    try {
      setLoading(true);
      setIsSearching(true);
      setSelectedIds(new Set());
      let data: Employee[] = [];
      if (filterType === 'city') {
        data = await employeeService.searchByCity(searchTerm);
      } else if (filterType === 'level') {
        data = await employeeService.searchByLevel(Number(searchTerm));
      }
      setEmployees(data);
      setTotalPages(1);
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
    setSelectedIds(new Set());
    loadEmployees();
  };

  const handleSaveEmployee = async (empData: Partial<Employee>) => {
    if (empToEdit) {
      await employeeService.updateEmployee(empToEdit.id, empData);
    } else {
      await employeeService.createEmployee(empData);
    }
    if (!isSearching) loadEmployees();
    else handleSearch({ preventDefault: () => {} } as any);
  };

  const handleApplyHike = async (percentage: number) => {
    if (empForHike) {
      await employeeService.applyHike(empForHike.id, percentage);
    } else if (isBulkHikeOpen) {
      await employeeService.applyBulkHike(Array.from(selectedIds), percentage);
      setSelectedIds(new Set());
      setIsBulkHikeOpen(false);
    }
    if (!isSearching) loadEmployees();
    else handleSearch({ preventDefault: () => {} } as any);
  };

  const confirmDelete = async () => {
    if (empToDelete) {
      await employeeService.deleteEmployees([empToDelete]);
    } else if (isBulkDeleteOpen) {
      await employeeService.deleteEmployees(Array.from(selectedIds));
      setSelectedIds(new Set());
      setIsBulkDeleteOpen(false);
    }
    if (!isSearching) loadEmployees();
    else handleSearch({ preventDefault: () => {} } as any);
  };

  const toggleSelect = (id: number) => {
    const newSet = new Set(selectedIds);
    if (newSet.has(id)) newSet.delete(id);
    else newSet.add(id);
    setSelectedIds(newSet);
  };

  const selectAll = () => {
    if (selectedIds.size === employees.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(employees.map(e => e.id)));
    }
  };

  const exportCSV = () => {
    if (employees.length === 0) return;
    const headers = ['ID', 'Username', 'Email', 'Role', 'Salary', 'Level', 'Department', 'Project', 'Skill', 'City'];
    const rows = employees.map(e => [
      e.id,
      e.username,
      e.email,
      e.roles?.map(r => r.name.replace('ROLE_', '')).join('|') || '',
      e.salary,
      e.levelNo || '',
      e.department || '',
      e.projectName || '',
      e.primarySkill || '',
      e.address?.city || ''
    ]);
    
    const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'employees.csv';
    link.click();
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
                <Plus size={16} /> Add
              </button>
            )}
            {isAdminOrManager && (
              <button onClick={exportCSV} className="btn btn-secondary" style={{ marginLeft: '0.5rem' }} title="Export to CSV">
                <Download size={16} /> Export
              </button>
            )}
          </div>
          
          {isAdminOrManager && (
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
              <select className="input-field" style={{ width: '120px' }} value={filterType} onChange={e => setFilterType(e.target.value)}>
                <option value="city">City</option>
                <option value="level">Level</option>
              </select>
              <form onSubmit={handleSearch} className="search-bar">
                <Search size={18} className="search-icon" />
                <input 
                  type="text" 
                  placeholder={`Search by ${filterType}...`}
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
            </div>
          )}
        </header>

        {selectedIds.size > 0 && (
          <div className="bulk-action-bar glass-panel animate-fade-in">
            <span>{selectedIds.size} employees selected</span>
            <div className="bulk-actions">
              <button onClick={() => setIsBulkHikeOpen(true)} className="btn btn-primary">Bulk Hike</button>
              {isAdmin && <button onClick={() => setIsBulkDeleteOpen(true)} className="btn btn-danger">Bulk Delete</button>}
              <button onClick={() => setSelectedIds(new Set())} className="btn btn-secondary">Cancel</button>
            </div>
          </div>
        )}

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
              {employees.length > 0 && (
                 <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <button onClick={selectAll} className="btn btn-secondary btn-sm" style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}>
                      <CheckSquare size={14} /> {selectedIds.size === employees.length ? 'Deselect All' : 'Select All on Page'}
                    </button>
                 </div>
              )}
              <div className="employee-grid">
                {employees.map(emp => (
                  <div key={emp.id} className={`glass-card employee-card animate-scale-up ${selectedIds.has(emp.id) ? 'selected' : ''}`}>
                    <div className="emp-card-header">
                      <input 
                        type="checkbox" 
                        className="emp-checkbox"
                        checked={selectedIds.has(emp.id)}
                        onChange={() => toggleSelect(emp.id)}
                      />
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
                        <span className="label">Level:</span>
                        <span className="value">L{emp.levelNo || 1}</span>
                      </div>
                      <div className="detail-row">
                        <span className="label">Salary:</span>
                        <span className="value" style={{color: 'var(--primary-light)', fontWeight: 600}}>${emp.salary ? emp.salary.toLocaleString() : '0'}</span>
                      </div>
                      {emp.department && (
                        <div className="detail-row">
                          <span className="label">Dept:</span>
                          <span className="value">{emp.department}</span>
                        </div>
                      )}
                      {emp.primarySkill && (
                        <div className="detail-row">
                          <span className="label">Skill:</span>
                          <span className="value">{emp.primarySkill}</span>
                        </div>
                      )}
                      {emp.address && emp.address.city && (
                        <div className="detail-row" style={{ marginTop: '0.25rem' }}>
                          <MapPin size={16} className="text-muted" />
                          <span className="value text-muted" style={{ fontSize: '0.8rem' }}>{emp.address.city}, {emp.address.country}</span>
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
      
      <HikeModal 
        isOpen={isBulkHikeOpen} 
        onClose={() => setIsBulkHikeOpen(false)} 
        onApply={handleApplyHike} 
        employeeName={`${selectedIds.size} selected employees`} 
      />

      <ConfirmModal 
        isOpen={isConfirmOpen} 
        onClose={() => setIsConfirmOpen(false)} 
        onConfirm={confirmDelete} 
        title="Delete Employee" 
        message="Are you sure you want to delete this employee? This action cannot be undone." 
      />
      
      <ConfirmModal 
        isOpen={isBulkDeleteOpen} 
        onClose={() => setIsBulkDeleteOpen(false)} 
        onConfirm={confirmDelete} 
        title="Bulk Delete" 
        message={`Are you sure you want to delete ${selectedIds.size} employees? This action cannot be undone.`} 
      />
    </div>
  );
};

export default Dashboard;
