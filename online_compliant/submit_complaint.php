<?php
session_start(); if(!isset($_SESSION["user_id"])) {header("Location: login.php");exit;} require "config/database.php"; $msg="";
if($_SERVER["REQUEST_METHOD"]==="POST"){
 $title=trim($_POST["title"]);$category=$_POST["category"];$description=trim($_POST["description"]);
 if(!$title||!$description)$msg="Please fill all required fields.";
 else{$no="CMP".date("Ymd").strtoupper(bin2hex(random_bytes(3)));$stmt=$conn->prepare("INSERT INTO complaints(complaint_no,user_id,title,category,description) VALUES(?,?,?,?,?)");$stmt->bind_param("sisss",$no,$_SESSION["user_id"],$title,$category,$description);$stmt->execute();header("Location: dashboard.php");exit;}
}
?>
<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Submit Complaint</title><link rel="stylesheet" href="css/style.css"></head><body><nav class="navbar"><div class="brand">CMS</div><a href="dashboard.php">← Dashboard</a></nav>
<div class="container narrow"><form class="card" method="post"><h2>Submit a Complaint</h2><?php if($msg):?><div class="alert"><?=$msg?></div><?php endif;?>
<label>Complaint Title</label><input name="title" required><label>Category</label><select name="category" required><option value="">Select category</option><option>Infrastructure</option><option>Academic</option><option>Technical</option><option>Service</option><option>Other</option></select><label>Description</label><textarea name="description" rows="7" required></textarea><button class="btn full">Submit Complaint</button></form></div></body></html>
