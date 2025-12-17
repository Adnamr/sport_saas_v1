-- ═══════════════════════════════════════════════════════════════════
-- Sport SaaS - PostgreSQL Initialization Script
-- ═══════════════════════════════════════════════════════════════════

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schemas if needed (optional, for multi-tenant isolation)
-- CREATE SCHEMA IF NOT EXISTS tenant_template;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE sportsaas TO sportsaas;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'Sport SaaS database initialized successfully';
END $$;
