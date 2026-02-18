<?php
// Generate a proper password hash
$password = 'password123';
$hash = password_hash($password, PASSWORD_DEFAULT);

echo "Use this SQL in MySQL Workbench:<br><br>";
echo "<code>UPDATE student_groups SET password = '" . $hash . "' WHERE username = 'groupa';</code>";
?>
