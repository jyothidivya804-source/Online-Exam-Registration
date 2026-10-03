# Online Complaint Management System

A simple full-stack **Online Complaint Management System** built with PHP, MySQL, HTML, CSS and JavaScript-ready structure.

## Features

- User registration and secure password hashing
- User login/logout
- Submit complaints
- Automatic complaint number
- Complaint categories
- User complaint history
- Admin dashboard
- Complaint status updates
- Admin remarks
- Responsive interface
- MySQL database

## Requirements

- XAMPP / WAMP / LAMP
- PHP 7.4+
- MySQL / MariaDB
- Web browser
- Git

## Installation

1. Copy the project folder into XAMPP's `htdocs` directory.
2. Start **Apache** and **MySQL**.
3. Open phpMyAdmin.
4. Import `database/complaint_system.sql`.
5. Open:

`http://localhost/Online-Complaint-Management-System/`

## Admin Login

- Email: `admin@example.com`
- Password: `admin123`

**Change the demo admin password before using this system in production.**

## GitHub

```bash
git init
git add .
git commit -m "Initial commit - Online Complaint Management System"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/Online-Complaint-Management-System.git
git push -u origin main
```

## Security notes

For a production deployment, use environment variables for database credentials, HTTPS, CSRF protection, stronger authorization controls, rate limiting, server-side validation, and secure session/cookie settings.
