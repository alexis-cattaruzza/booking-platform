-- V2: Création de toutes les tables principales
-- Date: 2024-11-25
-- Description: Tables businesses, services, schedules, appointments, customers, notifications

-- ============================================================================
-- TABLE: BUSINESSES
-- ============================================================================
CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    address VARCHAR(500),
    city VARCHAR(100),
    postal_code VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(255),
    category VARCHAR(50) CHECK (category IN (
        'hairdresser', 'beauty', 'health', 'sport', 'garage', 'other'
    )),
    logo_url VARCHAR(500),
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_businesses_user_id ON businesses(user_id);
CREATE INDEX idx_businesses_slug ON businesses(slug);
CREATE INDEX idx_businesses_is_active ON businesses(is_active);

-- ============================================================================
-- TABLE: SERVICES
-- ============================================================================
CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    color VARCHAR(7) DEFAULT '#3b82f6',
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

CREATE INDEX idx_services_business_id ON services(business_id);
CREATE INDEX idx_services_is_active ON services(is_active);
CREATE INDEX idx_services_display_order ON services(business_id, display_order);

-- ============================================================================
-- TABLE: SCHEDULES (Horaires hebdomadaires)
-- ============================================================================
CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL CHECK (end_time > start_time),
    slot_duration_minutes INTEGER DEFAULT 30 CHECK (slot_duration_minutes > 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT uk_business_day UNIQUE(business_id, day_of_week)
);

CREATE INDEX idx_schedules_business_id ON schedules(business_id);
CREATE INDEX idx_schedules_day_of_week ON schedules(day_of_week);

-- ============================================================================
-- TABLE: SCHEDULE_EXCEPTIONS (Fermetures exceptionnelles)
-- ============================================================================
CREATE TABLE schedule_exceptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    exception_date DATE NOT NULL,
    reason VARCHAR(255),
    is_closed BOOLEAN DEFAULT TRUE,
    start_time TIME,
    end_time TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exception_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT uk_business_exception_date UNIQUE(business_id, exception_date)
);

CREATE INDEX idx_exceptions_business_date ON schedule_exceptions(business_id, exception_date);

-- ============================================================================
-- TABLE: CUSTOMERS
-- ============================================================================
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20) NOT NULL,
    notes TEXT,
    total_appointments INTEGER DEFAULT 0,
    last_appointment_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT uk_business_phone UNIQUE(business_id, phone)
);

CREATE INDEX idx_customers_business_id ON customers(business_id);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_name ON customers(business_id, last_name, first_name);

-- ============================================================================
-- TABLE: APPOINTMENTS
-- ============================================================================
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    appointment_datetime TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN (
        'pending', 'confirmed', 'cancelled', 'completed', 'no_show'
    )),
    notes TEXT,
    cancellation_reason TEXT,
    cancellation_token VARCHAR(64) UNIQUE,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

CREATE INDEX idx_appointments_business_id ON appointments(business_id);
CREATE INDEX idx_appointments_service_id ON appointments(service_id);
CREATE INDEX idx_appointments_customer_id ON appointments(customer_id);
CREATE INDEX idx_appointments_datetime ON appointments(appointment_datetime);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_business_datetime ON appointments(business_id, appointment_datetime);
CREATE INDEX idx_appointments_cancellation_token ON appointments(cancellation_token);

-- ============================================================================
-- TABLE: NOTIFICATIONS
-- ============================================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'confirmation', 'reminder', 'cancellation', 'modification'
    )),
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('email', 'sms')),
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN (
        'pending', 'sent', 'failed', 'delivered'
    )),
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_appointment_id ON notifications(appointment_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);

-- ============================================================================
-- TABLE: SUBSCRIPTIONS (pour le modèle freemium - Phase 2)
-- ============================================================================
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    plan VARCHAR(20) DEFAULT 'free' CHECK (plan IN ('free', 'starter', 'pro')),
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN (
        'active', 'cancelled', 'expired', 'suspended'
    )),
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT uk_business_subscription UNIQUE(business_id)
);

CREATE INDEX idx_subscriptions_business_id ON subscriptions(business_id);
CREATE INDEX idx_subscriptions_plan ON subscriptions(plan);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);

-- ============================================================================
-- FUNCTION: Mise à jour automatique du champ updated_at
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour chaque table
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_businesses_updated_at BEFORE UPDATE ON businesses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_appointments_updated_at BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS (Documentation)
-- ============================================================================
COMMENT ON TABLE businesses IS 'Entreprises/professionnels utilisant la plateforme';
COMMENT ON TABLE services IS 'Services proposés par chaque business';
COMMENT ON TABLE schedules IS 'Horaires hebdomadaires de disponibilité';
COMMENT ON TABLE schedule_exceptions IS 'Fermetures exceptionnelles ou horaires spéciaux';
COMMENT ON TABLE customers IS 'Clients ayant pris rendez-vous';
COMMENT ON TABLE appointments IS 'Rendez-vous confirmés ou en attente';
COMMENT ON TABLE notifications IS 'Historique des notifications envoyées';
COMMENT ON TABLE subscriptions IS 'Abonnements et plans tarifaires';
