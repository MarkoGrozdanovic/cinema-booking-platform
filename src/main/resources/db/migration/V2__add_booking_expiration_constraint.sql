ALTER TABLE bookings
    ADD CONSTRAINT chk_booking_expiration
        CHECK (expires_at > created_at);