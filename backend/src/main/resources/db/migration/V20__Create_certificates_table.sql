-- Таблица для хранения информации о медицинских справках
CREATE TABLE certificates (
    id_certificate SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id_appointment) ON DELETE CASCADE,
    patient_id INTEGER NOT NULL REFERENCES patients(id_patient) ON DELETE CASCADE,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id_doctor) ON DELETE CASCADE,
    certificate_type VARCHAR(50) NOT NULL CHECK (certificate_type IN ('visit', 'work_study')),
    file_path VARCHAR(255) NOT NULL,
    valid_from DATE,
    valid_to DATE,
    disability_period_from DATE,
    disability_period_to DATE,
    work_restrictions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_certificates_patient ON certificates(patient_id);
CREATE INDEX idx_certificates_appointment ON certificates(appointment_id);
CREATE INDEX idx_certificates_doctor ON certificates(doctor_id);
CREATE INDEX idx_certificates_created_at ON certificates(created_at);
