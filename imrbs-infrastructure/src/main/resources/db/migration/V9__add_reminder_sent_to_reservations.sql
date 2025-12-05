-- V9: Add reminder_sent column to reservations table
-- Purpose: Track whether a meeting reminder has been sent for each reservation
-- to prevent duplicate reminder emails

ALTER TABLE reservations 
ADD COLUMN IF NOT EXISTS reminder_sent BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for efficient querying of upcoming reservations that need reminders
CREATE INDEX IF NOT EXISTS idx_reservation_reminder 
ON reservations (reminder_sent, start_time, status) 
WHERE status = 'CONFIRMED';

COMMENT ON COLUMN reservations.reminder_sent IS 'Flag indicating whether a meeting reminder has been sent (prevents duplicate reminders)';
