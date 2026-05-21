<?php
session_start();
if (isset($_SESSION['group_id'])) { header("Location: borrow.php"); exit(); }

$error = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_once 'db.php';
    $username = trim($_POST['username']);

    // PDO prepared statement - works with PostgreSQL
    $stmt = $conn->prepare("SELECT group_id, group_name, password FROM student_groups WHERE username = ?");
    $stmt->execute([$username]);
    $row = $stmt->fetch();

    if ($row) {
        // Compare password (plain text match - same as before)
        // For better security, use password_verify() with bcrypt hashes in the database
        if ($_POST['password'] === $row['password']) {
            $_SESSION['group_id']   = $row['group_id'];
            $_SESSION['group_name'] = $row['group_name'];
            header("Location: borrow.php");
            exit();
        } else {
            $error = 'Wrong password';
        }
    } else {
        $error = 'Username not found: ' . htmlspecialchars($username);
    }
}
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Login</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #1a5d1a, #2e7d32); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
        .card { background: white; padding: 40px 30px; border-radius: 12px; width: 100%; max-width: 360px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }
        h1 { text-align: center; color: #2e7d32; margin-bottom: 30px; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #333; }
        input { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; }
        input:focus { border-color: #2e7d32; outline: none; }
        button { width: 100%; padding: 14px; background: #2e7d32; color: white; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; }
        button:hover { background: #1a5d1a; }
        .error { background: #fee; color: #c00; padding: 10px; border-radius: 8px; margin-bottom: 20px; text-align: center; }
    </style>
</head>
<body>
    <div class="card">
        <h1>🔬 Chemistry Laboratory System</h1>
        <?php if ($error): ?><div class="error"><?= htmlspecialchars($error) ?></div><?php endif; ?>
        <form method="POST">
            <div class="form-group">
                <label>Username</label>
                <input type="text" name="username" required>
            </div>
            <div class="form-group">
                <label>Password</label>
                <input type="password" name="password" required>
            </div>
            <button type="submit">Login</button>
        </form>
    </div>
</body>
</html>
