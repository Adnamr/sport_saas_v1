-- =====================================================
-- V3: Auth tables - Add missing columns and create new tables
-- =====================================================

-- Add missing columns to users table (created in V1)
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

-- Rename password_hash to password if exists (for compatibility)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'password_hash') THEN
        ALTER TABLE users RENAME COLUMN password_hash TO password;
    END IF;
END $$;

-- Permissions table (system-level)
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Roles table (tenant-aware)
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_roles_tenant_name UNIQUE (tenant_id, name)
);

-- Role-Permission mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- User-Role mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Add missing columns to refresh_tokens (created in V1)
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES tenants(id);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_info VARCHAR(500);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS ip_address VARCHAR(50);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- Indexes for better query performance (IF NOT EXISTS not supported, use DO block)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_users_email_verification_token') THEN
        CREATE INDEX idx_users_email_verification_token ON users(email_verification_token);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_users_password_reset_token') THEN
        CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_roles_tenant_id') THEN
        CREATE INDEX idx_roles_tenant_id ON roles(tenant_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_roles_is_system_role') THEN
        CREATE INDEX idx_roles_is_system_role ON roles(is_system_role);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_refresh_tokens_expires_at') THEN
        CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
    END IF;
END $$;

-- Insert default permissions (only if not exists)
INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'users:read', 'View users', 'users', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'users:read');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'users:write', 'Create/Update users', 'users', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'users:write');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'users:delete', 'Delete users', 'users', 'delete'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'users:delete');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'roles:read', 'View roles', 'roles', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'roles:read');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'roles:write', 'Create/Update roles', 'roles', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'roles:write');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'roles:delete', 'Delete roles', 'roles', 'delete'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'roles:delete');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'tenants:read', 'View tenant info', 'tenants', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'tenants:read');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'tenants:write', 'Update tenant settings', 'tenants', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'tenants:write');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'products:read', 'View products', 'products', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'products:read');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'products:write', 'Create/Update products', 'products', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'products:write');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'products:delete', 'Delete products', 'products', 'delete'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'products:delete');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'orders:read', 'View orders', 'orders', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'orders:read');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'orders:write', 'Create/Update orders', 'orders', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'orders:write');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'orders:delete', 'Delete orders', 'orders', 'delete'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'orders:delete');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'inventory:read', 'View inventory', 'inventory', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'inventory:read');

INSERT INTO permissions (id, name, description, resource, action)
SELECT gen_random_uuid(), 'inventory:write', 'Manage inventory', 'inventory', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'inventory:write');
