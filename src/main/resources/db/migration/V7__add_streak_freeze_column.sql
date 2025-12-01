-- Agrega la columna has_streak_freeze a la tabla app_user
ALTER TABLE app_user
    ADD COLUMN has_streak_freeze BOOLEAN NOT NULL DEFAULT FALSE;