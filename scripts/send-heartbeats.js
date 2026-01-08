import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

// Datos de terminales simuladas
const terminals = [
  { deviceId: 'A1B2C3D4E5F6', location: 'Supermercado Central - Santiago', battery: 87, version: '2.5.0', online: true },
  { deviceId: 'B2C3D4E5F6A1', location: 'Farmacia Providencia', battery: 65, version: '2.5.0', online: true },
  { deviceId: 'C3D4E5F6A1B2', location: 'Minimarket Las Condes', battery: 12, version: '2.4.8', online: true },  // ← Cambiar a true
  { deviceId: 'D4E5F6A1B2C3', location: 'Restaurant Bellavista', battery: 92, version: '2.5.0', online: true },
  { deviceId: 'E5F6A1B2C3D4', location: 'Panadería Vitacura', battery: 78, version: '2.4.8', online: true },
  { deviceId: 'F6A1B2C3D4E5', location: 'Ferretería Ñuñoa', battery: 8, version: '2.4.5', online: true },  // ← Cambiar a true y battery a 8
  { deviceId: 'A7B8C9D0E1F2', location: 'Librería Lastarria', battery: 45, version: '2.5.0', online: true },
  { deviceId: 'B8C9D0E1F2A7', location: 'Zapatería Maipú', battery: 88, version: '2.5.0', online: true },
];

// Enviar heartbeat
async function sendHeartbeat(terminal, isInitial = false) {
  // Para terminales offline, solo enviar heartbeat en la inicialización (hace 15 min)
  const timestamp = (terminal.online || isInitial) 
    ? Date.now() 
    : Date.now() - (15 * 60 * 1000); // 15 minutos atrás

  const heartbeat = {
    deviceId: terminal.deviceId,
    timestamp: timestamp,
    batteryLevel: Math.floor(terminal.battery),
    batteryCharging: terminal.battery < 20,
    networkType: terminal.battery < 15 ? 'WIFI' : 'LTE',
    signalStrength: Math.floor(Math.random() * -50) - 50,
    storageAvailable: Math.floor(Math.random() * 10000000000) + 5000000000,
    appVersion: terminal.version,
    androidVersion: '13',
    model: 'Samsung Galaxy Tab A',
    transactionsCount: Math.floor(Math.random() * 50) + 10,
    failedLoginAttempts: terminal.deviceId === 'D4E5F6A1B2C3' ? 5 : 0,  // ← Terminal D4 con 5 intentos
    location: terminal.location
  };

  try {
    await axios.post(`${API_URL}/heartbeat`, heartbeat);
    
    if (terminal.online) {
      console.log(`✅ Online: ${terminal.deviceId} - ${terminal.location}`);
    } else {
      console.log(`⚫ Offline: ${terminal.deviceId} - ${terminal.location} (último heartbeat hace 15 min)`);
    }
  } catch (error) {
    console.error(`❌ Error: ${terminal.deviceId}:`, error.response?.data?.message || error.message);
  }
}

// Enviar heartbeat inicial para crear todas las terminales
async function initializeTerminals() {
  console.log('🔄 Inicializando terminales...\n');
  
  for (const terminal of terminals) {
    await sendHeartbeat(terminal, true);
    await new Promise(resolve => setTimeout(resolve, 300));
  }
  
  console.log('\n✨ Inicialización completa!');
  console.log('⏳ Espera 2-3 minutos para que el job automático marque las offline...\n');
}

// Enviar heartbeats continuos solo para terminales online
async function sendContinuousHeartbeats() {
  console.log('📡 Enviando heartbeats continuos cada 5 minutos para terminales online...');
  console.log('Presiona Ctrl+C para detener\n');
  
  setInterval(async () => {
    for (const terminal of terminals.filter(t => t.online)) {
      // Variar batería ligeramente
      terminal.battery = Math.floor(Math.max(0, Math.min(100, terminal.battery + (Math.random() - 0.5) * 3)));
      await sendHeartbeat(terminal, false);
    }
  }, 5 * 60 * 1000);  // ← Cada 5 minutos
}

// Ejecutar
async function main() {
  await initializeTerminals();
  await sendContinuousHeartbeats();
}

main();