import type { FleetStats } from '../types';
import { Server, Wifi, WifiOff, AlertTriangle, Activity } from 'lucide-react';

interface StatsCardsProps {
  stats: FleetStats;
}

export default function StatsCards({ stats }: StatsCardsProps) {
  const getUptimeColor = (uptime: number) => {
    if (uptime >= 95) return 'text-green-600 dark:text-green-400';
    if (uptime >= 80) return 'text-yellow-600 dark:text-yellow-400';
    return 'text-red-600 dark:text-red-400';
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
      {/* Total Terminals */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400">Total Terminales</p>
            <p className="text-3xl font-bold text-gray-900 dark:text-white">{stats.totalTerminals}</p>
          </div>
          <Server className="w-12 h-12 text-blue-500" />
        </div>
      </div>

      {/* Online */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400">Online</p>
            <p className="text-3xl font-bold text-green-600 dark:text-green-400">{stats.online}</p>
          </div>
          <Wifi className="w-12 h-12 text-green-500" />
        </div>
      </div>

      {/* Offline */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400">Offline</p>
            <p className="text-3xl font-bold text-red-600 dark:text-red-400">{stats.offline}</p>
          </div>
          <WifiOff className="w-12 h-12 text-red-500" />
        </div>
      </div>

      {/* Active Alerts */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400">Alertas Activas</p>
            <p className="text-3xl font-bold text-yellow-600 dark:text-yellow-400">{stats.activeAlerts}</p>
          </div>
          <AlertTriangle className="w-12 h-12 text-yellow-500" />
        </div>
      </div>

      {/* Uptime Last Hour - NUEVO */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400">Uptime Última Hora</p>
            <p className={`text-3xl font-bold ${getUptimeColor(stats.uptimeLastHour)}`}>
              {stats.uptimeLastHour.toFixed(1)}%
            </p>
          </div>
          <Activity className="w-12 h-12 text-purple-500" />
        </div>
      </div>
    </div>
  );
}