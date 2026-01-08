import { useEffect, useState } from 'react';
import type { Terminal, Alert, FleetStats } from './types';
import { api } from './api/client';
import StatsCards from './components/StatsCards';
import TerminalCard from './components/TerminalCard';
import AlertPanel from './components/AlertPanel';
import ChartsSection from './components/ChartsSection';
import { useTheme } from './contexts/ThemeContext';
import { RefreshCw, Moon, Sun } from 'lucide-react';

type View = 'dashboard' | 'terminals' | 'alerts' | 'updates';

function App() {
  const [stats, setStats] = useState<FleetStats | null>(null);
  const [terminals, setTerminals] = useState<Terminal[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'online' | 'offline'>('all');
  const [currentView, setCurrentView] = useState<View>('dashboard');
  const { theme, toggleTheme } = useTheme();

  const fetchData = async () => {
    try {
      setLoading(true);
      const [statsData, terminalsData, alertsData] = await Promise.all([
        api.getStats(),
        api.getTerminals(),
        api.getActiveAlerts(),
      ]);
      
      setStats(statsData);
      setTerminals(terminalsData);
      setAlerts(alertsData);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleResolveAlert = async (id: number) => {
    try {
      await api.resolveAlert(id);
      setAlerts(alerts.filter(alert => alert.id !== id));
    } catch (error) {
      console.error('Error resolving alert:', error);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const filteredTerminals = filter === 'all' 
    ? terminals 
    : terminals.filter(t => t.status === filter);

  if (loading && !stats) {
    return (
      <div className="min-h-screen bg-gray-100 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="w-12 h-12 animate-spin mx-auto text-blue-500" />
          <p className="mt-4 text-gray-600 dark:text-gray-400">Cargando datos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 dark:bg-gray-900">
      {/* Header */}
      <nav className="bg-white shadow-lg dark:bg-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-blue-600 dark:text-blue-400">🏪 Fleet Management POS</h1>
              <div className="hidden md:ml-6 md:flex md:space-x-8">
                <button
                  onClick={() => setCurrentView('dashboard')}
                  className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium ${
                    currentView === 'dashboard'
                      ? 'border-blue-500 text-gray-900 dark:text-white'
                      : 'border-transparent text-gray-500 dark:text-gray-400 hover:border-gray-300 hover:text-gray-700 dark:hover:text-gray-300'
                  }`}
                >
                  Dashboard
                </button>
                <button
                  onClick={() => setCurrentView('terminals')}
                  className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium ${
                    currentView === 'terminals'
                      ? 'border-blue-500 text-gray-900 dark:text-white'
                      : 'border-transparent text-gray-500 dark:text-gray-400 hover:border-gray-300 hover:text-gray-700 dark:hover:text-gray-300'
                  }`}
                >
                  Terminales
                </button>
                <button
                  onClick={() => setCurrentView('alerts')}
                  className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium ${
                    currentView === 'alerts'
                      ? 'border-blue-500 text-gray-900 dark:text-white'
                      : 'border-transparent text-gray-500 dark:text-gray-400 hover:border-gray-300 hover:text-gray-700 dark:hover:text-gray-300'
                  }`}
                >
                  Alertas
                </button>
                <button
                  onClick={() => setCurrentView('updates')}
                  className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium ${
                    currentView === 'updates'
                      ? 'border-blue-500 text-gray-900 dark:text-white'
                      : 'border-transparent text-gray-500 dark:text-gray-400 hover:border-gray-300 hover:text-gray-700 dark:hover:text-gray-300'
                  }`}
                >
                  Actualizaciones
                </button>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-500 dark:text-gray-400">Usuario: Admin</span>
              
              <button
                onClick={toggleTheme}
                className="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 transition-colors"
                aria-label="Toggle theme"
              >
                {theme === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
              </button>
              
              <button
                onClick={fetchData}
                className="flex items-center space-x-2 px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors text-sm font-medium"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Actualizar</span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* Dashboard View */}
        {currentView === 'dashboard' && (
          <>
            {stats && <StatsCards stats={stats} />}
            
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-8">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">🚨 Alertas Recientes</h2>
              </div>
              <div className="p-6">
                <AlertPanel alerts={alerts.slice(0, 5)} onResolve={handleResolveAlert} />
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
                <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                  <h2 className="text-lg font-semibold text-gray-900 dark:text-white">📱 Terminales Recientes</h2>
                </div>
                <div className="p-6 max-h-[600px] overflow-y-auto">
                  {terminals.length === 0 ? (
                    <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                      <p>No hay terminales registradas</p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {terminals.slice(0, 5).map((terminal) => (
                        <TerminalCard key={terminal.id} terminal={terminal} />
                      ))}
                    </div>
                  )}
                </div>
              </div>
              
              <ChartsSection />
            </div>
          </>
        )}

        {/* Terminals View */}
        {currentView === 'terminals' && (
          <>
            {stats && <StatsCards stats={stats} />}
            
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">📱 Todas las Terminales</h2>
                <div className="flex space-x-2">
                  <button
                    onClick={() => setFilter('all')}
                    className={`px-3 py-1 text-sm rounded transition-colors ${
                      filter === 'all' 
                        ? 'bg-blue-500 text-white' 
                        : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
                    }`}
                  >
                    Todas
                  </button>
                  <button
                    onClick={() => setFilter('online')}
                    className={`px-3 py-1 text-sm rounded transition-colors ${
                      filter === 'online' 
                        ? 'bg-blue-500 text-white' 
                        : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
                    }`}
                  >
                    Online
                  </button>
                  <button
                    onClick={() => setFilter('offline')}
                    className={`px-3 py-1 text-sm rounded transition-colors ${
                      filter === 'offline' 
                        ? 'bg-blue-500 text-white' 
                        : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
                    }`}
                  >
                    Offline
                  </button>
                </div>
              </div>
              
              <div className="p-6">
                {filteredTerminals.length === 0 ? (
                  <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                    <p>No hay terminales {filter !== 'all' && filter}</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {filteredTerminals.map((terminal) => (
                      <TerminalCard key={terminal.id} terminal={terminal} />
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        )}

        {/* Alerts View */}
        {currentView === 'alerts' && (
          <>
            {stats && <StatsCards stats={stats} />}
            
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">🚨 Todas las Alertas</h2>
              </div>
              <div className="p-6">
                <AlertPanel alerts={alerts} onResolve={handleResolveAlert} />
              </div>
            </div>
          </>
        )}

        {/* Updates View */}
        {currentView === 'updates' && (
          <>
            {stats && <StatsCards stats={stats} />}
            
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow max-w-2xl">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">🔄 Versiones de Software</h2>
              </div>
              <div className="p-6">
                <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                  <p>No hay terminales registradas</p>
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}

export default App;