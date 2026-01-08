import type { Terminal } from '../types';
import { Battery, BatteryCharging, Wifi, MapPin } from 'lucide-react';

interface TerminalCardProps {
  terminal: Terminal;
}

export default function TerminalCard({ terminal }: TerminalCardProps) {
  const statusColor = terminal.status === 'online' ? 'bg-green-500' : 'bg-red-500';
  
  const getBatteryIcon = () => {
    if (terminal.batteryCharging) {
      return <BatteryCharging className="w-5 h-5" />;
    }
    return <Battery className="w-5 h-5" />;
  };

  const getBatteryColor = () => {
    if (!terminal.batteryLevel) return 'text-gray-400';
    if (terminal.batteryLevel < 20) return 'text-red-500';
    if (terminal.batteryLevel < 50) return 'text-yellow-500';
    return 'text-green-500';
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-4 hover:shadow-lg transition-shadow">
      <div className="flex items-start justify-between mb-3">
        <div>
          <h3 className="font-semibold text-lg text-gray-900 dark:text-white">{terminal.id}</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">{terminal.deviceId}</p>
        </div>
        <span className={`${statusColor} w-3 h-3 rounded-full`}></span>
      </div>

      {terminal.location && (
        <div className="flex items-center text-sm text-gray-600 dark:text-gray-400 mb-2">
          <MapPin className="w-4 h-4 mr-1" />
          {terminal.location}
        </div>
      )}

      <div className="grid grid-cols-2 gap-3 mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
        <div className="flex items-center">
          <div className={getBatteryColor()}>
            {getBatteryIcon()}
          </div>
          <span className="ml-2 text-sm text-gray-900 dark:text-gray-100">{terminal.batteryLevel || 0}%</span>
        </div>

        <div className="flex items-center">
          <Wifi className="w-5 h-5 text-blue-500" />
          <span className="ml-2 text-sm text-gray-900 dark:text-gray-100">{terminal.networkType || 'N/A'}</span>
        </div>
      </div>

      <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
        <div className="text-xs text-gray-500 dark:text-gray-400">
          <p>Transacciones: {terminal.totalTransactions}</p>
          <p>Modelo: {terminal.model || 'N/A'}</p>
        </div>
      </div>
    </div>
  );
}