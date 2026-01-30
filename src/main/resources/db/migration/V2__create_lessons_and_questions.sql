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
CREATE TABLE question_option (
                                 id UUID PRIMARY KEY,
                                 question_id UUID NOT NULL REFERENCES question(id) ON DELETE CASCADE,

                                 value TEXT NOT NULL,          -- texto lógico (apple, banana, A, B, etc)
                                 icon_key VARCHAR(50),         -- clave de icono (apple, banana)
                                 image_url VARCHAR(255),       -- si algún día usas imágenes
                                 audio_url VARCHAR(255),       -- audio por opción
                                 is_correct BOOLEAN DEFAULT false,
                                 option_order INTEGER
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