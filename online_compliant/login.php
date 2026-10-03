<?php
session_start(); require "config/database.php"; $msg="";
if ($_SERVER["REQUEST_METHOD"]==="POST") {
    $email=trim($_POST["email"]); $password=$_POST["password"];
    $stmt=$conn->prepare("SELECT id,name,password FROM users WHERE email=?"); $stmt->bind_param("s",$email); $stmt->execute(); $r=$stmt->get_result()->fetch_assoc();
    if($r && password_verify($password,$r["password"])) { $_SESSION["user_id"]=$r["id"]; $_SESSION["user_name"]=$r["name"]; header("Location: dashboard.php"); exit; }
    $msg="Invalid email or password.";
}
?>
<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>User Login</title><link rel="stylesheet" href="css/style.css"></head><body>
<div class="auth"><form class="card" method="post"><h2>User Login</h2><?php if(isset($_GET["registered"])):?><div class="success">Registration successful. Please login.</div><?php endif;?><?php if($msg):?><div class="alert"><?=$msg?></div><?php endif;?>
<label>Email</label><input type="email" name="email" required><label>Password</label><input type="password" name="password" required><button class="btn full">Login</button><p>New user? <a href="register.php">Create account</a></p><p><a href="index.php">← Home</a></p></form></div></body></html>
