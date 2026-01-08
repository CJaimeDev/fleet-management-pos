export interface Terminal {
  id: string;
  deviceId: string;
  location: string | null;
  status: string;
  batteryLevel: number | null;
  batteryCharging: boolean;
  networkType: string | null;
  signalStrength: number | null;
  model: string | null;
  androidVersion: string | null;
  appVersion: string | null;
  lastSeen: string | null;
  totalTransactions: number;
  uptimePercentage24h: number | null;
}

export interface Alert {
  id: number;
  deviceId: string;
  alertType: string;
  severity: string;
  message: string;
  location: string | null;
  resolved: boolean;
  createdAt: string;
  resolvedAt: string | null;
}

export interface FleetStats {
  totalTerminals: number;
  online: number;
  offline: number;
  activeAlerts: number;
  avgUptimePercentage: number;
  totalTransactionsToday: number;
  uptimeLastHour: number;
}