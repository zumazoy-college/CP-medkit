-- МЕДКИТ - База данных для системы автоматизации поликлиники
-- Версия: 1.0.0
-- Дата: 2026-04-15
-- Обновлено в соответствии с ERD схемой

-- Таблица пользователей (общая для врачей и пациентов)
CREATE TABLE users (
    id_user SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    phone_number VARCHAR(11),
    role VARCHAR(20) NOT NULL CHECK (role IN ('doctor', 'patient')),
    avatar_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица врачей
CREATE TABLE doctors (
    id_doctor SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id_user) ON DELETE CASCADE,
    specialization VARCHAR(100) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 0.00 CHECK (rating >= 0 AND rating <= 5),
    reviews_count INTEGER DEFAULT 0,
    office VARCHAR(10),
    work_experience DATE
);

-- Таблица пациентов
CREATE TABLE patients (
    id_patient SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id_user) ON DELETE CASCADE,
    birthdate DATE NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('male', 'female')),
    snils VARCHAR(11) NOT NULL UNIQUE,
    allergies TEXT,
    chronic_diseases TEXT
);

-- Таблица расписаний врачей
CREATE TABLE schedules (
    id_schedule SERIAL PRIMARY KEY,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6),
    work_start TIME NOT NULL,
    work_end TIME NOT NULL,
    lunch_start TIME,
    lunch_end TIME,
    appointment_duration INTEGER NOT NULL DEFAULT 20 CHECK (appointment_duration >= 5 AND appointment_duration <= 120),
    is_active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(doctor_id, day_of_week)
);

-- Таблица исключений в расписании (отпуска, больничные)
CREATE TABLE exceptions (
    id_exception SERIAL PRIMARY KEY,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Справочник статусов слотов
CREATE TABLE slot_statuses (
    id_status SERIAL PRIMARY KEY,
    title VARCHAR(20) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Таблица временных слотов для записи
CREATE TABLE appointment_slots (
    id_slot SERIAL PRIMARY KEY,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status_id INTEGER NOT NULL REFERENCES slot_statuses(id_status),
    patient_id INTEGER REFERENCES patients(id_patient) ON DELETE SET NULL,
    cancellation_reason TEXT,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(doctor_id, slot_date, start_time)
);

-- Таблица приемов
CREATE TABLE appointments (
    id_appointment SERIAL PRIMARY KEY,
    slot_id INTEGER NOT NULL UNIQUE REFERENCES appointment_slots(id_slot) ON DELETE CASCADE,
    patient_id INTEGER NOT NULL REFERENCES patients(id_patient) ON DELETE CASCADE,
    complaints TEXT,
    anamnesis TEXT,
    objective_data TEXT,
    recommendations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица диагнозов (справочник МКБ-11)
CREATE TABLE diagnoses (
    id_diagnosis SERIAL PRIMARY KEY,
    icd_code VARCHAR(10) NOT NULL UNIQUE,
    icd_name VARCHAR(500) NOT NULL
);

-- Связь приемов и диагнозов (многие ко многим)
CREATE TABLE appointment_diagnoses (
    id_appointment_diagnosis SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    diagnosis_id INTEGER NOT NULL REFERENCES diagnoses(id_diagnosis) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    UNIQUE(appointment_id, diagnosis_id)
);

-- Справочник лекарств
CREATE TABLE medications (
    id_medication SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    active_substance VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255),
    form VARCHAR(100)
);

-- Справочник процедур
CREATE TABLE procedures (
    id_procedure SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INTEGER
);

-- Справочник анализов
CREATE TABLE analyses (
    id_analysis SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Справочник статусов назначений
CREATE TABLE prescription_statuses (
    id_status SERIAL PRIMARY KEY,
    title VARCHAR(20) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Таблица назначений лекарств
CREATE TABLE medication_prescriptions (
    id_medication_prescription SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    medication_id INTEGER NOT NULL REFERENCES medications(id_medication),
    duration VARCHAR(100),
    instructions TEXT,
    status_id INTEGER NOT NULL REFERENCES prescription_statuses(id_status),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица назначений процедур
CREATE TABLE procedure_prescriptions (
    id_procedure_prescription SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    procedure_id INTEGER NOT NULL REFERENCES procedures(id_procedure),
    quantity INTEGER,
    instructions TEXT,
    status_id INTEGER NOT NULL REFERENCES prescription_statuses(id_status),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица назначений анализов
CREATE TABLE analysis_prescriptions (
    id_analysis_prescription SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    analysis_id INTEGER NOT NULL REFERENCES analyses(id_analysis),
    instructions TEXT,
    status_id INTEGER NOT NULL REFERENCES prescription_statuses(id_status),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица шаблонов врачей
CREATE TABLE templates (
    id_template SERIAL PRIMARY KEY,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    complaints TEXT,
    anamnesis TEXT,
    objective_data TEXT,
    recommendations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Связь шаблонов и диагнозов
CREATE TABLE template_diagnoses (
    id_template_diagnosis SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL REFERENCES templates(id_template) ON DELETE CASCADE,
    diagnosis_id INTEGER NOT NULL REFERENCES diagnoses(id_diagnosis) ON DELETE CASCADE,
    UNIQUE(template_id, diagnosis_id)
);

-- Таблица файлов (прикрепленные к приемам)
CREATE TABLE files (
    id_file SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    file_url VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица отзывов
CREATE TABLE reviews (
    id_review SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL UNIQUE REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    patient_id INTEGER NOT NULL REFERENCES patients(id_patient) ON DELETE CASCADE,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_complained BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица избранных врачей
CREATE TABLE favorites (
    id_favorite SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL REFERENCES patients(id_patient) ON DELETE CASCADE,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(patient_id, doctor_id)
);

-- Таблица уведомлений
CREATE TABLE notifications (
    id_notification SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id_user) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(50) NOT NULL,
    body VARCHAR(100) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    link VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_doctors_specialization ON doctors(specialization);
CREATE INDEX idx_patients_user_id ON patients(user_id);
CREATE INDEX idx_patients_snils ON patients(snils);
CREATE INDEX idx_schedules_doctor_id ON schedules(doctor_id);
CREATE INDEX idx_appointment_slots_doctor_date ON appointment_slots(doctor_id, slot_date);
CREATE INDEX idx_appointment_slots_status ON appointment_slots(status_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_reviews_doctor_id ON reviews(doctor_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

-- Триггер для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_templates_updated_at BEFORE UPDATE ON templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_favorites_updated_at BEFORE UPDATE ON favorites
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Начальные данные для справочников

-- Статусы слотов
INSERT INTO slot_statuses (title, is_active) VALUES
('free', TRUE),
('booked', TRUE),
('completed', TRUE),
('cancelled', TRUE);

-- Статусы назначений
INSERT INTO prescription_statuses (title, is_active) VALUES
('active', TRUE),
('completed', TRUE),
('cancelled', TRUE);
