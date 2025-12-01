-- V1__create_users_and_roles.sql

-- -----------------------------------
-- 1. TABLA DE USUARIOS Y ROLES (Estudiante, Profesor, Admin)
-- -----------------------------------
CREATE TABLE app_user (
                          id UUID PRIMARY KEY,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          full_name VARCHAR(255) NOT NULL,

    -- Los roles posibles serán: 'STUDENT', 'TEACHER', 'ADMIN'
                          role VARCHAR(50) NOT NULL,

    -- Campos de Gamificación y Progreso (Solo usados por STUDENT)
                          xp_total INTEGER DEFAULT 0,
                          current_streak INTEGER DEFAULT 0,
                          hearts_count INTEGER DEFAULT 5, -- Vidas

    -- Metadata
                          created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          is_active BOOLEAN DEFAULT TRUE
);

-- -----------------------------------
-- 2. TABLA PARA CURSOS
-- -----------------------------------
CREATE TABLE course (
                        id UUID PRIMARY KEY,
                        title VARCHAR(255) UNIQUE NOT NULL,
                        target_language VARCHAR(50) NOT NULL, -- Ejemplo: 'English', 'French'
                        base_language VARCHAR(50) NOT NULL, -- Idioma de instrucción
                        is_active BOOLEAN DEFAULT TRUE
);

-- -----------------------------------
-- 3. TABLA PARA UNIDADES (Secciones grandes del curso)
-- -----------------------------------
CREATE TABLE unit (
                      id UUID PRIMARY KEY,
                      course_id UUID NOT NULL REFERENCES course(id),
                      title VARCHAR(255) NOT NULL,
                      unit_order INTEGER NOT NULL, -- Para ordenar en la ruta de aprendizaje

                      UNIQUE (course_id, unit_order)
);