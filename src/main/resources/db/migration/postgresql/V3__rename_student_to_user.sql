ALTER TABLE student RENAME TO users;
ALTER TABLE users RENAME CONSTRAINT uk_student_email TO uk_users_email;
ALTER INDEX student_pkey RENAME TO users_pkey;

ALTER TABLE student_roles RENAME TO user_roles;
ALTER TABLE user_roles RENAME COLUMN student_id TO user_id;
ALTER TABLE user_roles RENAME CONSTRAINT fk_student_roles_student TO fk_user_roles_user;
ALTER TABLE user_roles RENAME CONSTRAINT fk_student_roles_role TO fk_user_roles_role;
ALTER INDEX student_roles_pkey RENAME TO user_roles_pkey;

ALTER TABLE physical_assessment RENAME COLUMN student_id TO user_id;
ALTER TABLE physical_assessment RENAME CONSTRAINT fk_physical_assessment_student TO fk_physical_assessment_user;

ALTER TABLE refresh_token RENAME COLUMN student_id TO user_id;
ALTER TABLE refresh_token RENAME CONSTRAINT fk_refresh_token_student TO fk_refresh_token_user;

ALTER TABLE workout RENAME COLUMN student_id TO user_id;
ALTER TABLE workout RENAME CONSTRAINT fk_workout_student TO fk_workout_user;

ALTER TABLE notification RENAME COLUMN student_id TO user_id;