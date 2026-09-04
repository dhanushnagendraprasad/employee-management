import api from './api';

export interface LoginRequest {
  username: string;
  password?: string;
}

export interface JwtResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: { id: number, name: string }[];
}

export interface SignupRequest {
  username: string;
  password?: string;
  email: string;
  role?: string;
  salary?: number;
  address?: {
    city: string;
    state: string;
    country: string;
    pincode: string;
  };
}

export const authService = {
  login: async (data: LoginRequest): Promise<JwtResponse> => {
    const response = await api.post<JwtResponse>('/auth/login', data);
    return response.data;
  },
  
  register: async (data: SignupRequest): Promise<string> => {
    const response = await api.post<string>('/auth/register', data);
    return response.data;
  }
};
