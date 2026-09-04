import api from './api';

export interface Address {
  city: string;
  state: string;
  country: string;
  pincode: string;
}

export interface Employee {
  id: number;
  username: string;
  email: string;
  salary: number;
  levelNo: number;
  address: Address;
  roles: { id: number, name: string }[];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const employeeService = {
  getAllEmployees: async (page = 0, size = 10, sortBy = 'id', sortDir = 'asc') => {
    const response = await api.get<PageResponse<Employee>>(`/employees?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`);
    return response.data;
  },

  deleteEmployee: async (id: number) => {
    await api.delete(`/employees/${id}`);
  },

  createEmployee: async (employeeData: Partial<Employee>) => {
    const response = await api.post<Employee>('/employees', employeeData);
    return response.data;
  },

  updateEmployee: async (id: number, employeeData: Partial<Employee>) => {
    const response = await api.put<Employee>(`/employees/${id}`, employeeData);
    return response.data;
  },

  applyHike: async (id: number, hikePercentage: number) => {
    const response = await api.put<Employee>(`/employees/${id}/salary?hikePercentage=${hikePercentage}`);
    return response.data;
  },
  
  searchByCity: async (city: string) => {
    const response = await api.get<Employee[]>(`/employees/search/city/${city}`);
    return response.data;
  }
};
