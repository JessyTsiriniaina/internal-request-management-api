-- V6: split users.name into first_name / last_name per directive
ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN last_name VARCHAR(100);

-- Migrate existing data: split on first space
UPDATE users
SET first_name = CASE
        WHEN position(' ' in trim(name)) > 0 THEN split_part(trim(name), ' ', 1)
        ELSE trim(name)
    END,
    last_name = CASE
        WHEN position(' ' in trim(name)) > 0 THEN trim(substring(trim(name) from position(' ' in trim(name)) + 1))
        ELSE ''
    END
WHERE first_name IS NULL;

-- Enforce not null after migration (last_name may be empty but not null)
ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN first_name SET DEFAULT '';
ALTER TABLE users ALTER COLUMN last_name SET DEFAULT '';

-- Drop old column
ALTER TABLE users DROP COLUMN name;
