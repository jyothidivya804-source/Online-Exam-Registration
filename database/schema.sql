CREATE DATABASE IF NOT EXISTS online_exam
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE online_exam;

CREATE TABLE IF NOT EXISTS students (
    student_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    father_name VARCHAR(150) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(30) NOT NULL,
    mobile VARCHAR(25) NOT NULL,
    email VARCHAR(254) NOT NULL,
    roll_number VARCHAR(100) NOT NULL DEFAULT '',
    qualification VARCHAR(100) NOT NULL,
    institution VARCHAR(200) NOT NULL DEFAULT '',
    board_university VARCHAR(200) NOT NULL DEFAULT '',
    course VARCHAR(150) NOT NULL DEFAULT '',
    academic_score VARCHAR(50) NOT NULL DEFAULT '',
    passing_year SMALLINT UNSIGNED NULL,
    password_salt VARCHAR(64) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id),
    UNIQUE KEY uq_students_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS competitive_exams (
    exam_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    exam_name VARCHAR(150) NOT NULL,
    default_level VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (exam_id),
    UNIQUE KEY uq_competitive_exams_name (exam_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS applications (
    application_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    application_number VARCHAR(32) NOT NULL,
    student_id INT UNSIGNED NOT NULL,
    exam_id INT UNSIGNED NOT NULL,
    exam_level VARCHAR(80) NOT NULL,
    exam_center VARCHAR(120) NOT NULL,
    application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(40) NOT NULL DEFAULT 'Submitted',
    PRIMARY KEY (application_id),
    UNIQUE KEY uq_applications_number (application_number),
    KEY ix_applications_student (student_id),
    CONSTRAINT fk_applications_student
        FOREIGN KEY (student_id) REFERENCES students (student_id),
    CONSTRAINT fk_applications_exam
        FOREIGN KEY (exam_id) REFERENCES competitive_exams (exam_id)
) ENGINE=InnoDB;

INSERT INTO competitive_exams (exam_name, default_level) VALUES
    ('10th Board Exam', '10th'),
    ('Intermediate Board Exam', 'Intermediate'),
    ('ESET', 'Other'),
    ('AIMSET', 'Other'),
    ('JEA', 'Other'),
    ('Diploma Entrance Exam', 'Diploma'),
    ('B.Tech Entrance Exam', 'B.Tech'),
    ('Degree Entrance Exam', 'Degree'),
    ('Other Entrance Exam', 'Other')
ON DUPLICATE KEY UPDATE exam_name = VALUES(exam_name);
