CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        customer_id UUID NOT NULL,
                        restaurant_id UUID NOT NULL,
                        restaurant_name VARCHAR(255) NOT NULL,
                        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                        total_amount DECIMAL(10,2) NOT NULL,
                        delivery_address TEXT NOT NULL,
                        customer_notes TEXT,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        confirmed_at TIMESTAMP,
                        delivered_at TIMESTAMP
);

CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             menu_item_id UUID NOT NULL,
                             menu_item_name VARCHAR(255) NOT NULL,
                             unit_price DECIMAL(10,2) NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             subtotal DECIMAL(10,2) NOT NULL,
                             special_instructions TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);

ALTER TABLE order_items ADD CONSTRAINT check_positive_quantity CHECK (quantity > 0);
ALTER TABLE order_items ADD CONSTRAINT check_positive_unit_price CHECK (unit_price >= 0);
ALTER TABLE order_items ADD CONSTRAINT check_positive_subtotal CHECK (subtotal >= 0);
ALTER TABLE orders ADD CONSTRAINT check_positive_total_amount CHECK (total_amount >= 0);