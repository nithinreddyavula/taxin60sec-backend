-- One-time data repairs for schemas that existed before recent entity changes.
-- Runs on every boot (spring.sql.init.mode=always). spring.sql.init.continue-on-error=true
-- is also set, so any statement here that hits a table/column that doesn't exist yet
-- (e.g. a brand-new database) is logged and skipped rather than crashing the boot.
--
-- IMPORTANT: no DO $$ ... $$ blocks in this file. Spring's script splitter does not
-- understand Postgres dollar-quoting and mis-splits on the semicolons inside those
-- blocks, silently turning them into broken fragments that never actually run. Every
-- statement below is plain SQL for exactly that reason.

-- 1. users.referral_credits was changed to NOT NULL; backfill any existing NULLs to 0
-- before Hibernate tries to add the NOT NULL constraint, or that ALTER fails on real data.
UPDATE users SET referral_credits = 0 WHERE referral_credits IS NULL;

-- 2. client_profiles.tier is NOT NULL in ClientProfile (defaults to ClientTier.STANDARD).
-- Rows created before this field existed may have NULL here.
UPDATE client_profiles SET tier = 'STANDARD' WHERE tier IS NULL;

-- 3. service_offerings_category_check was generated when ServiceCategory had fewer values.
-- New values (e.g. NRI) can be rejected by the stale CHECK constraint even though they're
-- valid Java enum values. ddl-auto=update doesn't reliably recreate CHECK constraints when
-- enum values change, so drop the stale one explicitly.
ALTER TABLE IF EXISTS service_offerings DROP CONSTRAINT IF EXISTS service_offerings_category_check;

-- 4. cases.referral_discount_applied was added to the Case entity but ddl-auto=update has
-- not reliably applied it in every environment. Add it explicitly and idempotently.
ALTER TABLE IF EXISTS cases ADD COLUMN IF NOT EXISTS referral_discount_applied BOOLEAN NOT NULL DEFAULT FALSE;

-- 5. ca_profiles.availability is a new NOT NULL column (CAProfile.availability, defaults to
-- CAAvailability.AVAILABLE) on an existing table - same class of bug as #1. Add the column
-- idempotently and backfill any NULLs before Hibernate enforces NOT NULL.
ALTER TABLE IF EXISTS ca_profiles ADD COLUMN IF NOT EXISTS availability VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';
UPDATE ca_profiles SET availability = 'AVAILABLE' WHERE availability IS NULL;