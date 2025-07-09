CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               recipient_id UUID NOT NULL,
                               recipient_email VARCHAR(255) NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               subject VARCHAR(255) NOT NULL,
                               content TEXT NOT NULL,
                               status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                               event_id VARCHAR(255) UNIQUE NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               sent_at TIMESTAMP,
                               error_message TEXT
);

CREATE TABLE notification_metadata (
                                       notification_id UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
                                       metadata_key VARCHAR(255) NOT NULL,
                                       metadata_value TEXT,
                                       PRIMARY KEY (notification_id, metadata_key)
);

CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_event_id ON notifications(event_id);