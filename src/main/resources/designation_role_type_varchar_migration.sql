-- Convert designations.role_type from MySQL ENUM to VARCHAR
-- Needed because MECHANIC (and potentially other new roles) were added to RoleName
-- after the table was created, and the MySQL ENUM column wasn't updated.
ALTER TABLE designations
    MODIFY COLUMN role_type VARCHAR(30) NOT NULL;
