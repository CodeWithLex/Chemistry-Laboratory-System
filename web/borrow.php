<?php
session_start();
if (!isset($_SESSION['group_id'])) { header("Location: login.php"); exit(); }
require_once 'db.php';
require_once 'send_mail.php';

$msg = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $stmt = $conn->prepare("INSERT INTO requests (group_id, apparatus_id, qty) VALUES (?, ?, ?)");
    $stmt->bind_param("iii", $_SESSION['group_id'], $_POST['apparatus_id'], $_POST['qty']);
    if ($stmt->execute()) {
        $msg = 'success';
        
        // Get apparatus name for email
        $appStmt = $conn->prepare("SELECT item_name FROM apparatus WHERE apparatus_id = ?");
        $appStmt->bind_param("i", $_POST['apparatus_id']);
        $appStmt->execute();
        $appResult = $appStmt->get_result();
        $appRow = $appResult->fetch_assoc();
        
        // Send email notification to admin
        sendAdminNotification($_SESSION['group_name'], $appRow['item_name'], $_POST['qty']);
    }
}

$apparatus = $conn->query("SELECT * FROM apparatus WHERE current_quantity > 0");
$requests = $conn->prepare("SELECT r.*, a.item_name FROM requests r JOIN apparatus a ON r.apparatus_id = a.apparatus_id WHERE r.group_id = ? ORDER BY created_at DESC LIMIT 5");
$requests->bind_param("i", $_SESSION['group_id']);
$requests->execute();
$myRequests = $requests->get_result();
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Borrow Apparatus</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: #667eea; min-height: 100vh; padding: 20px; }
        .container { max-width: 500px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; color: white; margin-bottom: 20px; }
        .card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        h2 { color: #667eea; margin-bottom: 15px; }
        h3 { margin-bottom: 15px; color: #333; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 6px; font-weight: bold; }
        select, input { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; }
        button { padding: 12px 24px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; }
        .btn-logout { background: rgba(255,255,255,0.2); text-decoration: none; color: white; padding: 8px 16px; border-radius: 6px; }
        .success { background: #d4edda; color: #155724; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
        .request-item { padding: 12px; background: #f8f9fa; border-radius: 8px; margin-bottom: 10px; border-left: 4px solid #ccc; }
        .request-item.Pending { border-left-color: #ffc107; }
        .request-item.Approved { border-left-color: #28a745; }
        .request-item.Rejected { border-left-color: #dc3545; }
        .status { font-size: 12px; padding: 4px 8px; border-radius: 4px; background: #eee; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔬 ChemLab</h1>
            <a href="logout.php" class="btn-logout">Logout</a>
        </div>
        
        <div class="card">
            <h2>Welcome, <?= htmlspecialchars($_SESSION['group_name']) ?></h2>
        </div>
        
        <?php if ($msg === 'success'): ?>
            <div class="success">Request submitted! Wait for approval.</div>
        <?php endif; ?>
        
        <div class="card">
            <h3>Request Apparatus</h3>
            <form method="POST">
                <div class="form-group">
                    <label>Apparatus</label>
                    <select name="apparatus_id" required>
                        <option value="">Select...</option>
                        <?php while ($a = $apparatus->fetch_assoc()): ?>
                            <option value="<?= $a['apparatus_id'] ?>"><?= htmlspecialchars($a['item_name']) ?> (<?= $a['current_quantity'] ?> available)</option>
                        <?php endwhile; ?>
                    </select>
                </div>
                <div class="form-group">
                    <label>Quantity</label>
                    <input type="number" name="qty" min="1" required>
                </div>
                <button type="submit">Submit Request</button>
            </form>
        </div>
        
        <div class="card">
            <h3>Recent Requests</h3>
            <?php if ($myRequests->num_rows > 0): ?>
                <?php while ($r = $myRequests->fetch_assoc()): ?>
                    <div class="request-item <?= $r['status'] ?>">
                        <strong><?= htmlspecialchars($r['item_name']) ?></strong> x<?= $r['qty'] ?>
                        <span class="status"><?= $r['status'] ?></span>
                    </div>
                <?php endwhile; ?>
            <?php else: ?>
                <p>No requests yet</p>
            <?php endif; ?>
        </div>
    </div>
</body>
</html>
