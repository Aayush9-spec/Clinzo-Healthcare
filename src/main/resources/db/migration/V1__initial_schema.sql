CREATE TABLE doctor (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    slot_duration INTEGER NOT NULL,
    buffer_time INTEGER NOT NULL
);

CREATE TABLE availability (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurring_weekday VARCHAR(20),
    status VARCHAR(20) NOT NULL
);

CREATE TABLE slot (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    availability_id UUID NOT NULL REFERENCES availability(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT
);

CREATE TABLE booking (
    id UUID PRIMARY KEY,
    slot_id UUID NOT NULL REFERENCES slot(id) ON DELETE CASCADE,
    patient_name VARCHAR(255) NOT NULL,
    patient_email VARCHAR(320) NOT NULL,
    booking_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reservation_hold (
    id UUID PRIMARY KEY,
    slot_id UUID NOT NULL REFERENCES slot(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    metadata TEXT
);

CREATE INDEX idx_availability_doctor_date ON availability(doctor_id, date);
CREATE INDEX idx_slot_doctor_time_status ON slot(doctor_id, start_time, status);
CREATE INDEX idx_booking_slot ON booking(slot_id);
CREATE INDEX idx_reservation_hold_status_expires ON reservation_hold(status, expires_at);
