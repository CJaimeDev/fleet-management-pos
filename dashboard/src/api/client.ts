import axios from 'axios';
import type { Terminal, Alert, FleetStats } from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

export const api = {
  // Terminals
  getTerminals: async (status?: string) => {
    const response = await axios.get<{ success: boolean; data: Terminal[] }>(
      `${API_BASE_URL}/terminals${status ? `?status=${status}` : ''}`
    );
    return response.data.data || [];
  },

  getTerminal: async (id: string) => {
    const response = await axios.get<{ success: boolean; data: Terminal }>(
      `${API_BASE_URL}/terminals/${id}`
    );
    return response.data.data;
  },

  // Alerts
  getAlerts: async (resolved?: boolean) => {
    const response = await axios.get<{ success: boolean; data: Alert[] }>(
      `${API_BASE_URL}/alerts${resolved !== undefined ? `?resolved=${resolved}` : ''}`
    );
    return response.data.data || [];
  },

  getActiveAlerts: async () => {
    const response = await axios.get<{ success: boolean; data: Alert[] }>(
      `${API_BASE_URL}/alerts/active`
    );
    return response.data.data || [];
  },

  resolveAlert: async (id: number) => {
    const response = await axios.post<{ success: boolean; data: Alert }>(
      `${API_BASE_URL}/alerts/${id}/resolve`
    );
    return response.data.data;
  },

  // Stats
  getStats: async () => {
    const response = await axios.get<{ success: boolean; data: FleetStats }>(
      `${API_BASE_URL}/stats`
    );
    return response.data.data!;
  },

  // Health
  checkHealth: async () => {
    const response = await axios.get(`${API_BASE_URL}/health`);
    return response.data;
  },

  // Charts
  getUptimeData: async () => {
    const response = await axios.get<{ success: boolean; data: Array<{ time: string; uptime: number }> }>(
      `${API_BASE_URL}/charts/uptime`
    );
    return response.data.data || [];
  },

  getTransactionsData: async () => {
    const response = await axios.get<{ success: boolean; data: Array<{ hour: string; count: number }> }>(
      `${API_BASE_URL}/charts/transactions`
    );
    return response.data.data || [];
  },

  getVersionsData: async () => {
    const response = await axios.get<{ 
      success: boolean; 
      data: Array<{ version: string; count: number; percentage: number; isDeprecated: boolean }> 
    }>(
      `${API_BASE_URL}/charts/versions`
    );
    return response.data.data || [];
  },
};