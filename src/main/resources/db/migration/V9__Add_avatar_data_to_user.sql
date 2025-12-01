-- V9__Add_avatar_data_to_user.sql
ALTER TABLE app_user
    ADD COLUMN avatar_data jsonb;