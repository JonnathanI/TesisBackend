-- Agrega la columna last_heart_refill_time a la tabla app_user
ALTER TABLE app_user
    ADD COLUMN last_heart_refill_time TIMESTAMP WITHOUT TIME ZONE;

-- Inicializa el campo para los usuarios existentes a la hora actual
UPDATE app_user
SET last_heart_refill_time = NOW();