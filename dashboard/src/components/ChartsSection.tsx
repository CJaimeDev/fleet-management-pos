import { useEffect, useState } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { api } from '../api/client';
import { useTheme } from '../contexts/ThemeContext';

interface UptimeData {
  time: string;
  uptime: number;
}

interface TransactionData {
  hour: string;
  count: number;
}

interface VersionData {
  version: string;
  count: number;
  percentage: number;
  isDeprecated: boolean;
}

export default function ChartsSection() {
  const [uptimeData, setUptimeData] = useState<UptimeData[]>([]);
  const [transactionsData, setTransactionsData] = useState<TransactionData[]>([]);
  const [versionsData, setVersionsData] = useState<VersionData[]>([]);
  const { theme } = useTheme();

  // Colores según el tema
  const textColor = theme === 'dark' ? '#9ca3af' : '#374151';
  const gridColor = theme === 'dark' ? '#374151' : '#e5e7eb';

  useEffect(() => {
    const fetchChartData = async () => {
      try {
        const [uptime, transactions, versions] = await Promise.all([
          api.getUptimeData(),
          api.getTransactionsData(),
          api.getVersionsData(),
        ]);
        
        setUptimeData(uptime);
        setTransactionsData(transactions);
        setVersionsData(versions);
      } catch (error) {
        console.error('Error fetching chart data:', error);
      }
    };

    fetchChartData();
    const interval = setInterval(fetchChartData, 10000);
    return () => clearInterval(interval);
  }, []);

  // Estilo personalizado para tooltip
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div 
          className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2 shadow-lg"
        >
          <p className="text-gray-900 dark:text-gray-100 text-sm font-medium">
            {label}
          </p>
          <p className="text-gray-700 dark:text-gray-300 text-sm">
            {payload[0].name === 'uptime' ? 'Uptime: ' : 'Transacciones: '}
            <span className="font-bold">{payload[0].value}{payload[0].name === 'uptime' ? '%' : ''}</span>
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="space-y-8">
      {/* Uptime Chart */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">📊 Uptime últimas 24h</h2>
        </div>
        <div className="p-6">
          {uptimeData.length > 0 ? (
            <ResponsiveContainer width="100%" height={250}>
              <LineChart data={uptimeData}>
                <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
                <XAxis 
                  dataKey="time" 
                  stroke={gridColor}
                  tick={{ fill: textColor }}
                />
                <YAxis 
                  domain={[0, 100]} 
                  stroke={gridColor}
                  tick={{ fill: textColor }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Line 
                  type="monotone" 
                  dataKey="uptime" 
                  stroke="#22c55e" 
                  strokeWidth={2}
                  dot={{ fill: '#22c55e' }}
                />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <p>No hay datos de uptime disponibles</p>
            </div>
          )}
        </div>
      </div>

      {/* Transactions Chart */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">💳 Transacciones por Hora</h2>
        </div>
        <div className="p-6">
          {transactionsData.length > 0 ? (
            <ResponsiveContainer width="100%" height={250}>
              <BarChart data={transactionsData}>
                <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
                <XAxis 
                  dataKey="hour" 
                  stroke={gridColor}
                  tick={{ fill: textColor }}
                />
                <YAxis 
                  stroke={gridColor}
                  tick={{ fill: textColor }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="count" fill="#3b82f6" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <p>No hay datos de transacciones disponibles</p>
            </div>
          )}
        </div>
      </div>

      {/* Software Versions */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">🔄 Versiones de Software</h2>
        </div>
        <div className="p-6">
          {versionsData.length > 0 ? (
            <>
              <div className="space-y-4">
                {versionsData.map((version) => (
                  <div key={version.version}>
                    <div className="flex justify-between text-sm mb-2">
                      <span className="text-gray-700 dark:text-gray-300 font-medium">
                        {version.version} {version.isDeprecated && '(Deprecated)'}
                      </span>
                      <span className="text-gray-500 dark:text-gray-400">
                        {version.count} terminales ({version.percentage.toFixed(1)}%)
                      </span>
                    </div>
                    <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5">
                      <div 
                        className={`h-2.5 rounded-full ${
                          version.isDeprecated ? 'bg-red-500' : 
                          version.percentage > 50 ? 'bg-green-500' : 'bg-yellow-500'
                        }`}
                        style={{ width: `${version.percentage}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>

              <button className="mt-6 w-full bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
                Actualizar Terminales Pendientes
              </button>
            </>
          ) : (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <p>No hay terminales registradas</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}