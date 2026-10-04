# Online Exam Registration System

A full-stack web application designed for students to register for academic and competitive examinations online, manage their examination profiles, and track application status in real-time.

---

## 📌 Project Overview

The **Online Exam Registration System** streamlines the registration workflow for entrance and board examinations. It provides a secure, intuitive student portal with end-to-end data persistence using a Java backend connected to MySQL.

### Key Capabilities
- **Candidate Registration:** Capture detailed applicant information (personal details, academic history, exam preferences, and examination centers).
- **Secure Authentication:** Password hashing using PBKDF2 with salt (`210,000` iterations) and session token management.
- **Application Tracking:** Instant status lookup using Application Number or registered Mobile Number.
- **Student Dashboard:** View student profile details, enrolled exams, and real-time application processing states.
- **Competitive Exams Catalog:** Browse available exams (10th Board, Intermediate, ESET, AIMSET, JEA, Diploma, B.Tech, Degree Entrance).

---

## 🛠️ Tech Stack & Architecture

- **Frontend:** HTML5, CSS3, JavaScript (Fetch API, responsive design)
- **Backend:** Java (Java HTTP Server, JDBC, PBKDF2 encryption)
- **Database:** MySQL 8+ with relational schema, foreign key constraints, and unique indices
- **Driver:** MySQL Connector/J 9.x (`mysql-connector-j-9.7.0.jar`)

---

## 📁 Repository Structure

```text
Online_ Exam _Registration/
├── backend/
│   ├── ExamRegistrationApi.java       # Core HTTP API server handling registration, auth & status
│   ├── RegistrationServer.java       # Entry point runner
│   ├── DatabaseConnection.java       # Database connection utility
│   ├── Student.java                  # Model class for student entity
│   ├── StudentDAO.java               # Data access object for database interactions
│   └── mysql-connector-j-9.7.0.jar   # MySQL JDBC Driver
├── database/
│   ├── schema.sql                    # Database DDL with tables, indexes, and initial exam seeds
│   └── README.md                     # Database configuration details
├── frontend/
│   ├── index.html                    # Landing page
│   ├── exams.html                    # Exam catalog & information
│   ├── register.html                 # Comprehensive candidate registration form
│   ├── login.html                    # Student login page
│   ├── dashboard.html                # Student dashboard & profile
│   ├── status.html                   # Application tracking interface
│   ├── style.css                     # Global stylesheet
│   ├── script.js                     # API client & frontend logic
│   └── Screenshots/                  # Project preview images
└── README.md                         # Project documentation
```

---

## 🗄️ Database Design

The system runs on the `online_exam` database with three interconnected tables:

1. **`students`**
   - Stores candidate demographics, qualification, board/university, and PBKDF2-secured credentials.
2. **`competitive_exams`**
   - Stores list of active entrance exams and default education levels.
3. **`applications`**
   - Links student records to specific exams with unique application numbers, chosen exam center, and application status (`Submitted`, `Under Review`, `Approved`, etc.).

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK 21+)**
- **MySQL Server 8.0+**
- **Python 3** (or any static HTTP server for frontend)
- **Git**

---

### Step 1: Set Up MySQL Database

1. Open PowerShell / Command Prompt and import the schema:
   ```powershell
   Get-Content database\schema.sql | mysql -u root -p
   ```
2. Verify the tables (`students`, `competitive_exams`, `applications`) have been created under `online_exam`.

---

### Step 2: Configure Environment Variables

Set your database credentials in PowerShell before running the backend:
```powershell
$env:ONLINE_EXAM_DB_USER = "root"
$env:ONLINE_EXAM_DB_PASSWORD = "your_mysql_password"
$env:ONLINE_EXAM_DB_URL = "jdbc:mysql://localhost:3306/online_exam"
```

---

### Step 3: Compile & Start the Java Backend API

From the root directory:
```powershell
$classes = Join-Path $env:TEMP "online-exam-classes"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
javac -cp "backend\mysql-connector-j-9.7.0.jar" -d $classes backend\ExamRegistrationApi.java backend\RegistrationServer.java
java -cp "$classes;backend\mysql-connector-j-9.7.0.jar" RegistrationServer
```

The API will start listening at:
```
http://localhost:8080
```

---

### Step 4: Run the Frontend

Open a new terminal window in the repository root and serve the `frontend/` folder:
```powershell
python -m http.server 5500 --directory frontend
```

Open your browser and navigate to:
```
http://localhost:5500
```

---

## 🔌 API Endpoints Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/register` | Register new student & submit examination application |
| `POST` | `/login` | Authenticate student and issue session token |
| `POST` | `/dashboard` | Fetch student details and applications |
| `POST` | `/status` | Track application by Application Number or Mobile |
| `POST` | `/logout` | End active session |

---

## 🔒 Security Highlights

- **PBKDF2 Password Hashing:** Passwords are never stored in plaintext. They are salted using `SecureRandom` and hashed with PBKDF2 (`210,000` iterations).
- **Environment Isolation:** Database credentials are read from environment variables and not hard-coded in the source tree.
- **SQL Injection Prevention:** All database operations utilize parameterized `PreparedStatement` queries.

---

## 👤 Author

Developed by **[jyothidivya804-source](https://github.com/jyothidivya804-source)**
