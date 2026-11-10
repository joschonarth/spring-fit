ALTER TABLE exercise
    ADD CONSTRAINT ck_exercise_muscle_group
        CHECK (muscle_group IN ('CHEST',
                                'BACK',
                                'SHOULDERS',
                                'BICEPS',
                                'TRICEPS',
                                'FOREARMS',
                                'ABS',
                                'OBLIQUES',
                                'GLUTES',
                                'QUADRICEPS',
                                'HAMSTRINGS',
                                'CALVES',
                                'CORE'));