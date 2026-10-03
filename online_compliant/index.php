<?php
session_start();
if (isset($_SESSION['user_id'])) header("Location: dashboard.php");
if (isset($_SESSION['admin_id'])) header("Location: admin/dashboard.php");
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Complaint Management System</title><link rel="stylesheet" href="css/style.css">
</head>
<body>
<nav class="navbar"><div class="brand">CMS</div><div><a href="login.php">User Login</a><a class="btn small" href="register.php">Register</a><a href="admin/login.php">Admin</a></div></nav>
<main class="hero"><section><span class="badge">Online Complaint Management</span><h1>Report. Track. Resolve.</h1><p>Submit complaints online and track their progress from one simple dashboard.</p><div class="actions"><a class="btn" href="register.php">Get Started</a><a class="btn outline" href="login.php">Track Complaint</a></div></section></main>
</body></html>
