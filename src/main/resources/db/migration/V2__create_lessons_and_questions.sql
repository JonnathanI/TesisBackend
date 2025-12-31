-- V2__create_lessons_and_questions.sql

CREATE TABLE lesson (
                        id UUID PRIMARY KEY,
                        unit_id UUID NOT NULL REFERENCES unit(id),
                        title VARCHAR(255) NOT NULL,
                        lesson_order INTEGER NOT NULL,
                        required_xp INTEGER DEFAULT 10,

                        UNIQUE (unit_id, lesson_order)
);

-- -----------------------------------
-- 2. TABLA PARA TIPOS DE PREGUNTAS
-- -----------------------------------
CREATE TABLE question_type (
   id VARCHAR(50) PRIMARY KEY,
   description VARCHAR(255)
);

-- -----------------------------------
-- 3. TABLA DE PREGUNTAS (El contenido real)
-- -----------------------------------
CREATE TABLE question (
                          id UUID PRIMARY KEY,
                          lesson_id UUID NOT NULL REFERENCES lesson(id),
                          question_type_id VARCHAR(50) NOT NULL,
                          text_source TEXT NOT NULL,
                          text_target TEXT,
                          audio_url VARCHAR(255),
                          hint_json TEXT,
                          difficulty_score NUMERIC DEFAULT 0.5
);