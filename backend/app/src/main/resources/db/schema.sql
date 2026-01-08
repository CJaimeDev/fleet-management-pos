-- Tabla: terminals
CREATE TABLE IF NOT EXISTS terminals (
    id VARCHAR(50) PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    location VARCHAR(255),
    status VARCHAR(20) DEFAULT 'offline',
    
    -- Battery
    battery_level INTEGER CHECK (battery_level >= 0 AND battery_level <= 100),
    battery_charging BOOLEAN DEFAULT false,
    
    -- Network
    network_type VARCHAR(20),
    signal_strength INTEGER,
    
    -- Device Info
    model VARCHAR(100),
    manufacturer VARCHAR(100),
    android_version VARCHAR(20),
    app_version VARCHAR(20),
    
    -- Storage
    storage_total BIGINT,
    storage_available BIGINT,
    
    -- Timestamps
    last_seen TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Metrics
    total_transactions INTEGER DEFAULT 0,
    uptime_percentage_24h DECIMAL(5,2) DEFAULT 0
);

-- Tabla: heartbeats
CREATE TABLE IF NOT EXISTS heartbeats (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    battery_level INTEGER NOT NULL,
    battery_charging BOOLEAN NOT NULL,
    network_type VARCHAR(20) NOT NULL,
    signal_strength INTEGER,
    storage_available BIGINT NOT NULL,
    app_version VARCHAR(20) NOT NULL,
    android_version VARCHAR(20) NOT NULL,
    model VARCHAR(100) NOT NULL,
    transactions_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT fk_terminal FOREIGN KEY (device_id) 
        REFERENCES terminals(device_id) ON DELETE CASCADE
);

-- Tabla: alerts
CREATE TABLE IF NOT EXISTS alerts (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    message TEXT NOT NULL,
    location VARCHAR(255),
    resolved BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    resolved_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT fk_alert_terminal FOREIGN KEY (device_id) 
        REFERENCES terminals(device_id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_terminals_status ON terminals(status);
CREATE INDEX IF NOT EXISTS idx_heartbeats_device ON heartbeats(device_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_resolved ON alerts(resolved) WHERE resolved = false;