CREATE TABLE roles
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE student
(
    id         UUID PRIMARY KEY,
    birth_date DATE,
    created_at TIMESTAMP(6) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(255),
    CONSTRAINT uk_student_email UNIQUE (email)
);

CREATE TABLE student_roles
(
    student_id UUID NOT NULL,
    role_id    UUID NOT NULL,
    PRIMARY KEY (student_id, role_id),
    CONSTRAINT fk_student_roles_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_student_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE exercise
(
    id               UUID PRIMARY KEY,
    created_at       TIMESTAMP(6) NOT NULL,
    description      VARCHAR(255),
    difficulty_level VARCHAR(255) NOT NULL
        CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    equipment        VARCHAR(255),
    muscle_group     VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL
);

CREATE TABLE workout
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP(6) NOT NULL,
    description VARCHAR(255),
    name        VARCHAR(255) NOT NULL,
    objective   VARCHAR(255),
    updated_at  TIMESTAMP(6) NOT NULL,
    student_id  UUID,
    CONSTRAINT fk_workout_student FOREIGN KEY (student_id) REFERENCES student (id)
);

CREATE TABLE workout_exercise
(
    workout_id  UUID NOT NULL,
    exercise_id UUID NOT NULL,
    PRIMARY KEY (workout_id, exercise_id),
    CONSTRAINT fk_workout_exercise_workout FOREIGN KEY (workout_id) REFERENCES workout (id),
    CONSTRAINT fk_workout_exercise_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id)
);

CREATE TABLE physical_assessment
(
    id                  UUID PRIMARY KEY,
    bmi                 NUMERIC(38, 2) NOT NULL,
    bmi_classification  VARCHAR(255)
        CHECK (bmi_classification IN ('UNDERWEIGHT', 'NORMAL', 'OVERWEIGHT', 'OBESE')),
    body_fat_percentage NUMERIC(38, 2) NOT NULL,
    created_at          TIMESTAMP(6)   NOT NULL,
    height              NUMERIC(38, 2) NOT NULL,
    updated_at          TIMESTAMP(6)   NOT NULL,
    weight              NUMERIC(38, 2) NOT NULL,
    student_id          UUID           NOT NULL,
    CONSTRAINT fk_physical_assessment_student FOREIGN KEY (student_id) REFERENCES student (id)
);

CREATE TABLE refresh_token
(
    id         UUID PRIMARY KEY,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked    BOOLEAN                     NOT NULL,
    token_hash VARCHAR(255)                NOT NULL,
    student_id UUID                        NOT NULL,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_student FOREIGN KEY (student_id) REFERENCES student (id)
);

CREATE TABLE notification
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    message    VARCHAR(255) NOT NULL,
    is_read    BOOLEAN      NOT NULL,
    student_id UUID         NOT NULL
);