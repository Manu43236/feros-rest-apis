-- Add bank branch, Aadhar, and nominee fields to staff profiles
-- Run on staging then prod (Hibernate ddl-auto=update also creates these on deploy)

ALTER TABLE staff_profiles
    ADD COLUMN bank_branch_name      VARCHAR(255),
    ADD COLUMN aadhar_number         VARCHAR(255),
    ADD COLUMN aadhar_name           VARCHAR(255),
    ADD COLUMN nominee_name          VARCHAR(255),
    ADD COLUMN nominee_relation      VARCHAR(255),
    ADD COLUMN nominee_date_of_birth DATE,
    ADD COLUMN nominee_aadhar_number VARCHAR(255);
