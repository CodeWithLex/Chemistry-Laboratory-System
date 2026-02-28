<?php
session_start();
if (!isset($_SESSION['group_id'])) { header("Location: login.php"); exit(); }
require_once 'db.php';

$msg = '';

// Handle adding a member
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
    if ($_POST['action'] === 'add_member') {
        $memberName = trim($_POST['member_name']);
        if (empty($memberName)) {
            $msg = 'empty';
        } else {
            $stmt = $conn->prepare("INSERT INTO group_members (group_id, member_name) VALUES (?, ?)");
            $stmt->bind_param("is", $_SESSION['group_id'], $memberName);
            if ($stmt->execute()) {
                $msg = 'added';
            } else {
                $msg = 'error';
            }
        }
    } elseif ($_POST['action'] === 'remove_member' && isset($_POST['member_id'])) {
        $stmt = $conn->prepare("DELETE FROM group_members WHERE member_id = ? AND group_id = ?");
        $stmt->bind_param("ii", $_POST['member_id'], $_SESSION['group_id']);
        if ($stmt->execute()) {
            $msg = 'removed';
        }
    }
}

// Fetch current members
$members = $conn->prepare("SELECT member_id, member_name FROM group_members WHERE group_id = ? ORDER BY member_name");
$members->bind_param("i", $_SESSION['group_id']);
$members->execute();
$memberList = $members->get_result();
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Group Members</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #1a5d1a, #2e7d32); min-height: 100vh; padding: 20px; }
        .container { max-width: 500px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; color: white; margin-bottom: 20px; }
        .card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        h2 { color: #2e7d32; margin-bottom: 15px; }
        h3 { margin-bottom: 15px; color: #333; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 6px; font-weight: bold; }
        input[type="text"] { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; }
        input[type="text"]:focus { border-color: #2e7d32; outline: none; }
        button, .btn { padding: 12px 24px; background: #2e7d32; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; text-decoration: none; display: inline-block; }
        button:hover, .btn:hover { background: #1a5d1a; }
        .btn-back { background: rgba(255,255,255,0.2); text-decoration: none; color: white; padding: 8px 16px; border-radius: 6px; font-size: 14px; }
        .btn-back:hover { background: rgba(255,255,255,0.35); }
        .btn-remove { background: #dc3545; padding: 6px 12px; font-size: 13px; border-radius: 6px; }
        .btn-remove:hover { background: #b02a37; }
        .success { background: #d4edda; color: #155724; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
        .error-msg { background: #f8d7da; color: #721c24; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
        .member-item { padding: 12px; background: #f8f9fa; border-radius: 8px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; border-left: 4px solid #2e7d32; }
        .member-name { font-weight: bold; color: #333; }
        .empty-text { color: #999; font-style: italic; padding: 20px; text-align: center; }
        .add-form { display: flex; gap: 10px; }
        .add-form input { flex: 1; }
        .add-form button { white-space: nowrap; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>👥 Group Members</h1>
            <a href="borrow.php" class="btn-back">← Back to Borrow</a>
        </div>

        <div class="card">
            <h2>Welcome, <?= htmlspecialchars($_SESSION['group_name']) ?></h2>
        </div>

        <?php if ($msg === 'added'): ?>
            <div class="success">Member added successfully!</div>
        <?php elseif ($msg === 'removed'): ?>
            <div class="success">Member removed.</div>
        <?php elseif ($msg === 'empty'): ?>
            <div class="error-msg">Please enter a member name.</div>
        <?php elseif ($msg === 'error'): ?>
            <div class="error-msg">Failed to add member. Please try again.</div>
        <?php endif; ?>

        <div class="card">
            <h3>Add New Member</h3>
            <form method="POST" class="add-form">
                <input type="hidden" name="action" value="add_member">
                <input type="text" name="member_name" placeholder="Enter member's full name" required>
                <button type="submit">+ Add</button>
            </form>
        </div>

        <div class="card">
            <h3>Current Members (<?= $memberList->num_rows ?>)</h3>
            <?php if ($memberList->num_rows > 0): ?>
                <?php while ($m = $memberList->fetch_assoc()): ?>
                    <div class="member-item">
                        <span class="member-name"><?= htmlspecialchars($m['member_name']) ?></span>
                        <form method="POST" style="margin:0;">
                            <input type="hidden" name="action" value="remove_member">
                            <input type="hidden" name="member_id" value="<?= $m['member_id'] ?>">
                            <button type="submit" class="btn-remove" onclick="return confirm('Remove this member?')">✕ Remove</button>
                        </form>
                    </div>
                <?php endwhile; ?>
            <?php else: ?>
                <p class="empty-text">No members added yet. Add your group members above.</p>
            <?php endif; ?>
        </div>
    </div>
</body>
</html>
