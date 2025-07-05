CREATE TABLE menu_items (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            restaurant_id UUID NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            price DECIMAL(10, 2) NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            CONSTRAINT fk_restaurant
                                FOREIGN KEY(restaurant_id)
                                    REFERENCES restaurants(id)
                                    ON DELETE CASCADE
);