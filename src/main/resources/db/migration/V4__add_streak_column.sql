-- Agrega la columna last_practice_date a la tabla app_user para el cálculo de racha.
ALTER TABLE app_user
    ADD COLUMN last_practice_date TIMESTAMP WITHOUT TIME ZONE;

-- Opcional: Si quieres que el campo current_streak sea NOT NULL (si no lo era ya)
-- ALTER TABLE app_user
--     ALTER COLUMN current_streak SET NOT NULL;