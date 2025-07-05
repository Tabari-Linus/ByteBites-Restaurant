CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id UUID NOT NULL,
                             menu_item_id UUID NOT NULL,
                             quantity INTEGER NOT NULL,
                             price DECIMAL(10, 2) NOT NULL,
                             CONSTRAINT fk_order
                                 FOREIGN KEY(order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE
);