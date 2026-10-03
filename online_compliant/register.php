<?php
session_start(); require "config/database.php";
$msg="";
if ($_SERVER["REQUEST_METHOD"]==="POST") {
    $name=trim($_POST["name"]); $email=trim($_POST["email"]); $password=$_POST["password"];
    if (!$name || !filter_var($email,FILTER_VALIDATE_EMAIL) || strlen($password)<6) $msg="Enter valid details. Password must be at least 6 characters.";
    else {
        $stmt=$conn->prepare("SELECT id FROM users WHERE email=?"); $stmt->bind_param("s",$email); $stmt->execute();
        if ($stmt->get_result()->num_rows) $msg="Email already registered.";
        else { $hash=password_hash($password,PASSWORD_DEFAULT); $stmt=$conn->prepare("INSERT INTO users(name,email,password) VALUES(?,?,?)"); $stmt->bind_param("sss",$name,$email,$hash); $stmt->execute(); header("Location: login.php?registered=1"); exit; }
    }
}
?>
<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Register</title><link rel="stylesheet" href="css/style.css"></head><body>
<div class="auth"><form class="card" method="post"><h2>Create Account</h2><?php if($msg): ?><div class="alert"><?=$msg?></div><?php endif; ?>
<label>Full Name</label><input name="name" required><label>Email</label><input type="email" name="email" required><label>Password</label><input type="password" name="password" minlength="6" required><button class="btn full">Register</button><p>Already have an account? <a href="login.php">Login</a></p></form></div></body></html>
