-- =====================================================
-- V4: Catalog tables for categories and products
-- =====================================================

-- Categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    image_url VARCHAR(500),
    parent_id UUID REFERENCES categories(id),
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    product_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT uk_categories_tenant_slug UNIQUE (tenant_id, slug)
);

-- Products table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    short_description VARCHAR(500),
    category_id UUID NOT NULL REFERENCES categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    product_condition VARCHAR(20) DEFAULT 'NEW',
    purchase_price DECIMAL(10, 2),
    rental_price_daily DECIMAL(10, 2),
    rental_price_weekly DECIMAL(10, 2),
    rental_price_monthly DECIMAL(10, 2),
    sale_price DECIMAL(10, 2),
    deposit_amount DECIMAL(10, 2),
    brand VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),
    size VARCHAR(50),
    color VARCHAR(50),
    weight DECIMAL(10, 2),
    dimensions VARCHAR(500),
    is_rentable BOOLEAN NOT NULL DEFAULT TRUE,
    is_sellable BOOLEAN NOT NULL DEFAULT FALSE,
    requires_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    min_rental_days INTEGER NOT NULL DEFAULT 1,
    max_rental_days INTEGER,
    view_count INTEGER NOT NULL DEFAULT 0,
    rental_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT uk_products_tenant_slug UNIQUE (tenant_id, slug),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT uk_products_tenant_sku UNIQUE (tenant_id, sku)
);

-- Product images table
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    alt_text VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    file_size BIGINT,
    content_type VARCHAR(100),
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Product attributes table
CREATE TABLE product_attributes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(1000) NOT NULL,
    attribute_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_filterable BOOLEAN NOT NULL DEFAULT FALSE,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_categories_tenant_id ON categories(tenant_id);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_active ON categories(active);

CREATE INDEX idx_products_tenant_id ON products(tenant_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_is_rentable ON products(is_rentable);
CREATE INDEX idx_products_is_sellable ON products(is_sellable);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_is_primary ON product_images(is_primary);

CREATE INDEX idx_product_attributes_product_id ON product_attributes(product_id);
CREATE INDEX idx_product_attributes_is_filterable ON product_attributes(is_filterable);
