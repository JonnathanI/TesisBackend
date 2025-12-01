-- V3__create_progress_and_gamification.sql

-- -----------------------------------
-- 1. CLAVE FORÁNEA: Conectamos QUESTION a QUESTION_TYPE (solo se puede hacer después de crear V2)
-- -----------------------------------
ALTER TABLE question
    ADD CONSTRAINT fk_question_type
        FOREIGN KEY (question_type_id)
            REFERENCES question_type(id);

-- -----------------------------------
-- 2. TABLA DE PROGRESO DE LECCIÓN (Ruta de aprendizaje)
-- -----------------------------------
CREATE TABLE user_lesson_progress (
                                      user_id UUID NOT NULL REFERENCES app_user(id),
                                      lesson_id UUID NOT NULL REFERENCES lesson(id),

    -- Nivel de "Maestría" o cuántas veces se ha completado bien (Duolingo usa 5 niveles)
                                      mastery_level INTEGER DEFAULT 0,

    -- La última vez que el usuario practicó esta lección
                                      last_practiced TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                      PRIMARY KEY (user_id, lesson_id)
);

-- -----------------------------------
-- 3. TABLA DE PROGRESO DE PREGUNTA (Para el Algoritmo de Repaso Espaciado)
-- -----------------------------------
CREATE TABLE user_question_data (
                                    user_id UUID NOT NULL REFERENCES app_user(id),
                                    question_id UUID NOT NULL REFERENCES question(id),

    -- Cuán fuerte es el recuerdo (Basado en el algoritmo de repetición espaciada)
                                    strength_score NUMERIC DEFAULT 0.5,

    -- Fecha para el próximo repaso programado
                                    next_due_date TIMESTAMP WITHOUT TIME ZONE,

                                    PRIMARY KEY (user_id, question_id)
);

-- -----------------------------------
-- 4. TABLA DE TRANSACCIONES (Monedas/Gemas/Lingots)
-- -----------------------------------
CREATE TABLE transaction_log (
                                 id UUID PRIMARY KEY,
                                 user_id UUID NOT NULL REFERENCES app_user(id),
                                 type VARCHAR(50) NOT NULL,   -- Ejemplos: 'LESSON_COMPLETED', 'HEART_BOUGHT', 'STREAK_FREEZE'
                                 amount INTEGER NOT NULL,     -- Cantidad de monedas/gemas añadidas o restadas
                                 timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);