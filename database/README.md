# Database setup

The registration, login, dashboard, and application-status pages use MySQL.
Install/start MySQL, then initialize the project schema from the repository root:

```powershell
Get-Content database\schema.sql | mysql -u root -p
```

Set credentials for the account that can access the `online_exam` database before
starting the Java API. The password is deliberately not stored in source files:

```powershell
$env:ONLINE_EXAM_DB_USER = "root"
$env:ONLINE_EXAM_DB_PASSWORD = "your-mysql-password"
$env:ONLINE_EXAM_DB_URL = "jdbc:mysql://localhost:3306/online_exam"
```

The API requires Java 21 or later and uses the MySQL Connector/J JAR already in
`backend`. Start it from the repository root:

```powershell
$classes = Join-Path $env:TEMP "online-exam-classes"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
javac -cp "backend\mysql-connector-j-9.7.0.jar" -d $classes backend\ExamRegistrationApi.java backend\RegistrationServer.java
java -cp "$classes;backend\mysql-connector-j-9.7.0.jar" RegistrationServer
```

In a second PowerShell window, serve the existing frontend:

```powershell
python -m http.server 5500 --directory frontend
```

Then visit <http://localhost:5500>. Keep the API and frontend servers running
while using the site. Applications and hashed student passwords are persisted
in MySQL; login sessions are held in memory and expire when the API restarts.
