-- ═══════════════════════════════════════════════════════════════════════════
-- V2__extend_tenants_table.sql
-- Extend tenants table with subscription and contact fields
-- ═══════════════════════════════════════════════════════════════════════════

-- Add new columns to tenants table
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(50) NOT NULL DEFAULT 'FREE';
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS contact_email VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(50);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS trial_ends_at TIMESTAMP;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS subscription_ends_at TIMESTAMP;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_users INTEGER DEFAULT 5;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_products INTEGER DEFAULT 100;

-- Update system tenant with contact email
UPDATE tenants SET contact_email = 'admin@sportsaas.com' WHERE slug = 'system';

-- Create indexes for new columns
CREATE INDEX IF NOT EXISTS idx_tenants_subscription_plan ON tenants(subscription_plan);

-- Add comment
COMMENT ON TABLE tenants IS 'Multi-tenant organizations with subscription management';
