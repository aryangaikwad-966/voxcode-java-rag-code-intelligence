-- VXC-190: Add repair_attempt_count to track bounded repair loop attempts.
-- This column records how many LLM-driven repair attempts have been made
-- against a given failed VerificationResult. Bounded by MAX_RETRY_ATTEMPTS = 2.
ALTER TABLE verification_results
    ADD COLUMN repair_attempt_count INT NOT NULL DEFAULT 0;
