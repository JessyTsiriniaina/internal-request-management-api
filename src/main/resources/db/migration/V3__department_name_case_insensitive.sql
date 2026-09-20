-- V3: Make department name uniqueness case-insensitive + whitespace-aware
-- App normalizes: trim + collapse internal \s+ -> single space, but DB must still enforce case-insensitivity.

-- Deduplicate existing data by collapsing whitespace and lowercasing (keep first occurrence)
-- Normalize existing rows: trim + collapse spaces
UPDATE departments
SET name = regexp_replace(trim(name), '\s+', ' ', 'g')
WHERE name <> regexp_replace(trim(name), '\s+', ' ', 'g');

-- If case-insensitive duplicates exist, keep lowest id, remove others (or append suffix)
-- Detect duplicates by lower(name)
-- This block removes duplicate rows that would violate the new lower(name) unique index
DELETE FROM departments
WHERE id NOT IN (
    SELECT MIN(id) FROM departments GROUP BY lower(name)
);

-- Replace case-sensitive UNIQUE with case-insensitive unique index on lower(name)
ALTER TABLE departments DROP CONSTRAINT IF EXISTS departments_name_key;
CREATE UNIQUE INDEX uq_departments_name_lower ON departments (lower(name));
