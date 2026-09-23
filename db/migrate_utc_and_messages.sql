-- Run once against the existing database while the old server is stopped.
-- Existing timestamps were written with the server's Europe/Riga local clock.
BEGIN;
CREATE TABLE IF NOT EXISTS app_migrations (id text PRIMARY KEY, applied_at timestamptz NOT NULL DEFAULT now());
ALTER TABLE measurements ADD COLUMN IF NOT EXISTS message_id varchar(255);
ALTER TABLE measurements ADD COLUMN IF NOT EXISTS unit varchar(255);
ALTER TABLE measurements ADD COLUMN IF NOT EXISTS measured_at timestamp without time zone;
ALTER TABLE measurements ALTER COLUMN value TYPE numeric(12,3);
ALTER TABLE stations ALTER COLUMN latitude TYPE numeric(9,6);
ALTER TABLE stations ALTER COLUMN longitude TYPE numeric(9,6);
ALTER TABLE station_heartbeat ADD COLUMN IF NOT EXISTS message_id varchar(255);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_migrations WHERE id = '20260923_utc') THEN
        UPDATE stations SET last_seen = last_seen AT TIME ZONE 'Europe/Riga' AT TIME ZONE 'UTC'
            WHERE last_seen IS NOT NULL;
        UPDATE measurements SET created_at = created_at AT TIME ZONE 'Europe/Riga' AT TIME ZONE 'UTC'
            WHERE created_at IS NOT NULL;
        UPDATE station_heartbeat SET created_at = created_at AT TIME ZONE 'Europe/Riga' AT TIME ZONE 'UTC'
            WHERE created_at IS NOT NULL;
        UPDATE station_events SET created_at = created_at AT TIME ZONE 'Europe/Riga' AT TIME ZONE 'UTC'
            WHERE created_at IS NOT NULL;
        UPDATE measurements SET measured_at = created_at WHERE measured_at IS NULL;
        INSERT INTO app_migrations (id) VALUES ('20260923_utc');
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_measurements_message_id ON measurements (message_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_heartbeat_message_id ON station_heartbeat (message_id);
COMMIT;
