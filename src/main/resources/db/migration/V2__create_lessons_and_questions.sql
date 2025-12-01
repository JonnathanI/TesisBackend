-- V2__create_lessons_and_questions.sql

-- -----------------------------------
-- 1. TABLA PARA LECCIONES (Nodos en la ruta de Duolingo)
-- -----------------------------------
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
                               id VARCHAR(50) PRIMARY KEY, -- Ejemplos: 'TRANSLATION', 'LISTEN_AND_TYPE', 'MATCHING'
                               description VARCHAR(255)
);

-- Datos iniciales para Question Type (Flyway no soporta INSERT, pero lo incluimos por claridad)
/*
INSERT INTO question_type (id, description) VALUES
('TRANSLATION_TO_TARGET', 'Translate a sentence to the target language'),
('LISTEN_AND_TYPE', 'Listen to an audio and type the phrase'),
('PRONUNCIATION_PRACTICE', 'Record and verify pronunciation'),
('MATCHING', 'Match words or phrases'),
('SELECT_WORD', 'Select the correct word to complete the sentence');
*/

-- -----------------------------------
-- 3. TABLA DE PREGUNTAS (El contenido real)
-- -----------------------------------
CREATE TABLE question (
                          id UUID PRIMARY KEY,
                          lesson_id UUID NOT NULL REFERENCES lesson(id),
                          question_type_id VARCHAR(50) NOT NULL, -- No podemos hacer REFERENCES hasta V3

    -- El contenido de la pregunta
                          text_source TEXT NOT NULL, -- Ej: "The cat drinks milk"
                          text_target TEXT,          -- Ej: "El gato bebe leche" (Respuesta correcta)
                          audio_url VARCHAR(255),    -- URL al archivo de audio para escuchar
                          hint_json TEXT,            -- Opciones/palabras para preguntas tipo drag-and-drop

                          difficulty_score NUMERIC DEFAULT 0.5 -- Para el algoritmo de aprendizaje adaptativo
);