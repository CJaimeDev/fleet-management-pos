import type { Alert } from '../types';
import { AlertCircle, CheckCircle, Info } from 'lucide-react';

interface AlertPanelProps {
  alerts: Alert[];
  onResolve: (id: number) => void;
}

export default function AlertPanel({ alerts, onResolve }: AlertPanelProps) {
  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return <AlertCircle className="w-5 h-5 text-red-500" />;
      case 'WARNING':
        return <AlertCircle className="w-5 h-5 text-yellow-500" />;
      default:
        return <Info className="w-5 h-5 text-blue-500" />;
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return 'border-l-red-500 bg-red-50 dark:bg-red-900/20';
      case 'WARNING':
        return 'border-l-yellow-500 bg-yellow-50 dark:bg-yellow-900/20';
      default:
        return 'border-l-blue-500 bg-blue-50 dark:bg-blue-900/20';
    }
  };

  return (
    <>
      {alerts.length === 0 ? (
        <div className="text-center py-8 text-gray-500 dark:text-gray-400">
          <CheckCircle className="w-12 h-12 mx-auto mb-2 text-green-500" />
          <p>No hay alertas activas</p>
        </div>
      ) : (
        <div className="space-y-3">
          {alerts.map((alert) => (
            <div
              key={alert.id}
              className={`border-l-4 p-4 rounded ${getSeverityColor(alert.severity)}`}
            >
              <div className="flex items-start justify-between">
                <div className="flex items-start space-x-3 flex-1">
                  {getSeverityIcon(alert.severity)}
                  <div className="flex-1">
                    <p className="font-medium text-gray-900 dark:text-white">{alert.message}</p>
                    <div className="mt-1 text-sm text-gray-600 dark:text-gray-400">
                      <p>Device: {alert.deviceId}</p>
                      {alert.location && <p>Ubicación: {alert.location}</p>}
                      <p className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                        {new Date(alert.createdAt).toLocaleString()}
                      </p>
                    </div>
                  </div>
                </div>
                
                {!alert.resolved && (
                  <button
                    onClick={() => onResolve(alert.id)}
                    className="ml-4 px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600 transition-colors"
                  >
                    Resolver
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}