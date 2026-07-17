CREATE TABLE business_profiles (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT,
    client_profile_id BIGINT NOT NULL REFERENCES client_profiles(id),
    business_name VARCHAR(180) NOT NULL,
    business_type VARCHAR(30) NOT NULL,
    pan_number VARCHAR(20), gstin VARCHAR(20), tan_number VARCHAR(20), cin VARCHAR(30), msme_number VARCHAR(30),
    incorporation_date DATE, business_status VARCHAR(20) NOT NULL, assigned_ca_id BIGINT REFERENCES users(id), address VARCHAR(1000)
);
CREATE INDEX idx_business_profiles_client ON business_profiles(client_profile_id);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS business_profile_id BIGINT REFERENCES business_profiles(id);
