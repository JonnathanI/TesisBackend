-- V8__Add_options_to_question.sql

ALTER TABLE question
    ADD COLUMN options jsonb;