-- V3__create_progress_and_gamification.sql
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
      mastery_level INTEGER DEFAULT 0,
      last_practiced TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (user_id, lesson_id)
);

-- -----------------------------------
-- 3. TABLA DE PROGRESO DE PREGUNTA (Para el Algoritmo de Repaso Espaciado)
-- -----------------------------------
CREATE TABLE user_question_data (
    user_id UUID NOT NULL REFERENCES app_user(id),
    question_id UUID NOT NULL REFERENCES question(id),
    strength_score NUMERIC DEFAULT 0.5,
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