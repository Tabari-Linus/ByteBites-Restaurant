CREATE TABLE restaurants (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             owner_id UUID NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             address VARCHAR(255) NOT NULL,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_restaurants_owner_id ON restaurants(owner_id);