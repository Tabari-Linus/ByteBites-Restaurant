CREATE TABLE restaurants (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             owner_id UUID NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             description TEXT,
                             address TEXT NOT NULL,
                             phone VARCHAR(20) NOT NULL,
                             email VARCHAR(255),
                             status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE menu_items (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            price DECIMAL(10,2) NOT NULL,
                            category VARCHAR(50) NOT NULL,
                            available BOOLEAN NOT NULL DEFAULT true,
                            image_url VARCHAR(500),
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurants_owner_id ON restaurants(owner_id);
CREATE INDEX idx_restaurants_status ON restaurants(status);
CREATE INDEX idx_restaurants_name ON restaurants(name);
CREATE INDEX idx_menu_items_restaurant_id ON menu_items(restaurant_id);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_available ON menu_items(available);

CREATE UNIQUE INDEX idx_restaurants_owner_name ON restaurants(owner_id, name) WHERE status != 'INACTIVE';