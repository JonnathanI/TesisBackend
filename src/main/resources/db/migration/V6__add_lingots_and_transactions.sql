-- 1. Añadir columna lingots_count a la tabla de usuarios
ALTER TABLE app_user
    ADD COLUMN lingots_count INTEGER NOT NULL DEFAULT 100;

-- 2. Crear la tabla de transacciones
CREATE TABLE transaction_log (
                                 id UUID PRIMARY KEY,
                                 user_id UUID NOT NULL REFERENCES app_user(id),
                                 type VARCHAR(50) NOT NULL,
                                 amount INTEGER NOT NULL,
                                 timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);