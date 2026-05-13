-- Create user_settings table for storing patient notification preferences and app settings
CREATE TABLE user_settings (
    id_settings SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    notify_appointment_reminder BOOLEAN DEFAULT TRUE,
    notify_rating_reminder BOOLEAN DEFAULT TRUE,
    notify_appointment_cancelled BOOLEAN DEFAULT TRUE,
    default_screen VARCHAR(50) DEFAULT 'search',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Create index on user_id for faster lookups
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);
