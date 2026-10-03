<?php
session_start(); if(!isset($_SESSION["user_id"])) {header("Location: login.php");exit;} require "config/database.php";
$stmt=$conn->prepare("SELECT * FROM complaints WHERE user_id=? ORDER BY created_at DESC");$stmt->bind_param("i",$_SESSION["user_id"]);$stmt->execute();$complaints=$stmt->get_result();
?>
<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Dashboard</title><link rel="stylesheet" href="css/style.css"></head><body>
<nav class="navbar"><div class="brand">CMS</div><div>Welcome, <?=htmlspecialchars($_SESSION["user_name"])?> <a href="submit_complaint.php">New Complaint</a><a href="logout.php">Logout</a></div></nav>
<main class="container"><div class="page-head"><div><h1>My Complaints</h1><p>Track all complaints submitted by you.</p></div><a class="btn" href="submit_complaint.php">+ New Complaint</a></div>
<div class="table-wrap"><table><tr><th>Complaint ID</th><th>Title</th><th>Category</th><th>Status</th><th>Date</th></tr>
<?php while($c=$complaints->fetch_assoc()): ?><tr><td>#<?=htmlspecialchars($c["complaint_no"])?></td><td><?=htmlspecialchars($c["title"])?></td><td><?=htmlspecialchars($c["category"])?></td><td><span class="status <?=strtolower(str_replace(' ','-',$c["status"]))?>"><?=htmlspecialchars($c["status"])?></span></td><td><?=date("d M Y",strtotime($c["created_at"]))?></td></tr><?php endwhile; ?>
<?php if($complaints->num_rows===0):?><tr><td colspan="5" class="empty">No complaints yet.</td></tr><?php endif;?></table></div></main></body></html>
