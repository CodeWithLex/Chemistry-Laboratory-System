<?php
session_start();
if (!isset($_SESSION['group_id'])) { header("Location: login.php"); exit(); }
require_once 'db.php';
require_once 'send_mail.php';

$msg = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Handle apparatus request (Feature 3: request unlisted apparatus)
    if (isset($_POST['request_apparatus'])) {
        $apparatusName = trim($_POST['requested_apparatus_name']);
        $reason = trim($_POST['request_reason'] ?? '');
        if (!empty($apparatusName)) {
            $stmt = $conn->prepare("INSERT INTO apparatus_requests (group_id, apparatus_name, reason) VALUES (?, ?, ?)");
            $stmt->bind_param("iss", $_SESSION['group_id'], $apparatusName, $reason);
            if ($stmt->execute()) {
                $msg = 'apparatus_requested';
            }
        }
    } else {
        // Normal borrow request
        // Issue 4: Check for duplicate pending/approved request before inserting
        $dupCheck = $conn->prepare("SELECT COUNT(*) AS cnt FROM requests WHERE group_id = ? AND apparatus_id = ? AND status IN ('Pending','Approved')");
        $dupCheck->bind_param("ii", $_SESSION['group_id'], $_POST['apparatus_id']);
        $dupCheck->execute();
        $dupResult = $dupCheck->get_result();
        $dupRow = $dupResult->fetch_assoc();

        if ($dupRow['cnt'] > 0) {
            $msg = 'duplicate';
        } else {
            // Issue: Validate quantity against real_available
            $availStmt = $conn->prepare(
                "SELECT (current_quantity - COALESCE((SELECT SUM(qty) FROM requests WHERE apparatus_id = ? AND status IN ('Approved', 'Pending')), 0)) AS real_available " .
                "FROM apparatus WHERE apparatus_id = ?"
            );
            $availStmt->bind_param("ii", $_POST['apparatus_id'], $_POST['apparatus_id']);
            $availStmt->execute();
            $availResult = $availStmt->get_result();
            $availRow = $availResult->fetch_assoc();

            if ($_POST['qty'] > $availRow['real_available']) {
                $msg = 'exceeds_qty';
            } else {
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
        }
    }
}

// Feature 2 Fix: Show apparatus as long as current_quantity > 0 (not just real_available > 0)
// Apparatus only disappears when its total inventory quantity is depleted (zero)
// Also exclude apparatus that the group already has a pending/approved request for
$apparatus = $conn->prepare(
    "SELECT a.*, " .
    "(a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved','Pending') THEN r.qty ELSE 0 END), 0)) AS real_available " .
    "FROM apparatus a " .
    "LEFT JOIN requests r ON a.apparatus_id = r.apparatus_id AND r.status IN ('Approved','Pending') " .
    "WHERE a.current_quantity > 0 " .
    "AND a.apparatus_id NOT IN (" .
    "    SELECT apparatus_id FROM requests WHERE group_id = ? AND status IN ('Pending','Approved')" .
    ") " .
    "GROUP BY a.apparatus_id " .
    "ORDER BY a.item_name"
);
$apparatus->bind_param("i", $_SESSION['group_id']);
$apparatus->execute();
$apparatusList = $apparatus->get_result();

// Feature 1: Pending requests by OTHER groups (for visibility)
$pendingByOthersStmt = $conn->prepare(
    "SELECT r.apparatus_id, g.group_name, r.qty " .
    "FROM requests r " .
    "JOIN student_groups g ON r.group_id = g.group_id " .
    "WHERE r.status = 'Pending' AND r.group_id != ?"
);
$pendingByOthersStmt->bind_param("i", $_SESSION['group_id']);
$pendingByOthersStmt->execute();
$pendingByOthersRs = $pendingByOthersStmt->get_result();

$pendingByOthers = []; // apparatus_id => array of rows
while ($row = $pendingByOthersRs->fetch_assoc()) {
    $aid = (int)$row['apparatus_id'];
    if (!isset($pendingByOthers[$aid])) {
        $pendingByOthers[$aid] = [];
    }
    $pendingByOthers[$aid][] = $row;
}

// Show ALL requests (removed LIMIT 5)
$requests = $conn->prepare("SELECT r.*, a.item_name FROM requests r JOIN apparatus a ON r.apparatus_id = a.apparatus_id WHERE r.group_id = ? ORDER BY created_at DESC");
$requests->bind_param("i", $_SESSION['group_id']);
$requests->execute();
$myRequests = $requests->get_result();

// Currently Borrowing: approved items
$borrowing = $conn->prepare("SELECT r.*, a.item_name FROM requests r JOIN apparatus a ON r.apparatus_id = a.apparatus_id WHERE r.group_id = ? AND r.status = 'Approved' ORDER BY r.updated_at DESC");
$borrowing->bind_param("i", $_SESSION['group_id']);
$borrowing->execute();
$myBorrowing = $borrowing->get_result();
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Borrow Apparatus</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #1a5d1a, #2e7d32); min-height: 100vh; padding: 20px; }
        .container { max-width: 500px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; color: white; margin-bottom: 20px; }
        .header-actions { display: flex; gap: 8px; align-items: center; }
        .card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        h2 { color: #2e7d32; margin-bottom: 15px; }
        h3 { margin-bottom: 15px; color: #333; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 6px; font-weight: bold; }
        select, input { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; }
        select:focus, input:focus { border-color: #2e7d32; outline: none; }
        textarea { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 14px; resize: vertical; min-height: 60px; }
        textarea:focus { border-color: #2e7d32; outline: none; }
        button { padding: 12px 24px; background: #2e7d32; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; }
        button:hover { background: #1a5d1a; }
        .btn-nav { background: rgba(255,255,255,0.2); text-decoration: none; color: white; padding: 8px 16px; border-radius: 6px; font-size: 14px; }
        .btn-nav:hover { background: rgba(255,255,255,0.35); }
        .btn-logout { background: rgba(255,255,255,0.2); text-decoration: none; color: white; padding: 8px 16px; border-radius: 6px; }
        .btn-logout:hover { background: rgba(255,255,255,0.35); }
        .btn-request { background: #ff9800; }
        .btn-request:hover { background: #e68a00; }
        .success { background: #d4edda; color: #155724; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
        .error-msg { background: #f8d7da; color: #721c24; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
        .info-msg { background: #d1ecf1; color: #0c5460; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
        .request-item { padding: 12px; background: #f8f9fa; border-radius: 8px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; border-left: 4px solid #ccc; }
        .request-item.Pending { border-left-color: #ff9800; }
        .request-item.Approved { border-left-color: #2e7d32; }
        .request-item.Rejected { border-left-color: #dc3545; }
        .request-item.Returned { border-left-color: #2196f3; }
        .status-badge { font-size: 12px; padding: 4px 10px; border-radius: 12px; color: white; font-weight: bold; }
        .status-badge.Pending { background: #ff9800; }
        .status-badge.Approved { background: #2e7d32; }
        .status-badge.Rejected { background: #dc3545; }
        .status-badge.Returned { background: #2196f3; }
        .borrow-item { padding: 12px; background: #e8f5e9; border-radius: 8px; margin-bottom: 10px; border-left: 4px solid #2e7d32; }
        .hint { font-size: 12px; color: #666; margin-top: 10px; line-height: 1.35; }
        .toggle-link { color: #2e7d32; cursor: pointer; font-size: 14px; text-decoration: underline; display: inline-block; margin-top: 10px; }
        .hidden { display: none; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔬 ChemLab</h1>
            <div class="header-actions">
                <a href="manage_group.php" class="btn-nav">👥 Members</a>
                <a href="logout.php" class="btn-logout">Logout</a>
            </div>
        </div>
        
        <div class="card">
            <h2>Welcome, <?= htmlspecialchars($_SESSION['group_name']) ?></h2>
        </div>
        
        <?php if ($msg === 'success'): ?>
            <div class="success">Request submitted! Wait for approval.</div>
        <?php endif; ?>
        <?php if ($msg === 'duplicate'): ?>
            <div class="error-msg">You already have a pending or approved request for this apparatus.</div>
        <?php endif; ?>
        <?php if ($msg === 'exceeds_qty'): ?>
            <div class="error-msg">Cannot request that many items. Quantity exceeds real available amount.</div>
        <?php endif; ?>
        <?php if ($msg === 'apparatus_requested'): ?>
            <div class="info-msg">Your apparatus request has been submitted for admin review. Thank you!</div>
        <?php endif; ?>

        <!-- Currently Borrowing Section -->
        <?php if ($myBorrowing->num_rows > 0): ?>
        <div class="card">
            <h3>📦 Currently Borrowing</h3>
            <?php while ($b = $myBorrowing->fetch_assoc()): ?>
                <div class="borrow-item">
                    <strong><?= htmlspecialchars($b['item_name']) ?></strong> x<?= $b['qty'] ?>
                </div>
            <?php endwhile; ?>
        </div>
        <?php endif; ?>
        
        <div class="card">
            <h3>Request Apparatus</h3>
            <?php if ($apparatusList->num_rows > 0): ?>
            <form method="POST">
                <div class="hint">
                    Availability is calculated in real-time: <b>Real Available</b> = Inventory − (Approved + Pending).<br/>
                    Hover an apparatus option to see <b>Pending by others</b> (desktop browsers).
                </div>
                <div class="form-group">
                    <label>Apparatus</label>
                    <select name="apparatus_id" required>
                        <option value="">Select...</option>
                        <?php while ($a = $apparatusList->fetch_assoc()): ?>
                            <?php
                                $aid = (int)$a['apparatus_id'];
                                $realAvail = max(0, (int)$a['real_available']);
                                $tooltip = "";
                                if (isset($pendingByOthers[$aid])) {
                                    $parts = [];
                                    foreach ($pendingByOthers[$aid] as $p) {
                                        $parts[] = $p['group_name'] . " (" . $p['qty'] . ")";
                                    }
                                    $tooltip = "Pending by others: " . implode(", ", $parts);
                                }
                            ?>
                            <option value="<?= $a['apparatus_id'] ?>" title="<?= htmlspecialchars($tooltip) ?>" <?= $realAvail <= 0 ? 'disabled' : '' ?>><?= htmlspecialchars($a['item_name']) ?> (<?= $realAvail ?> available)</option>
                        <?php endwhile; ?>
                    </select>
                </div>
                <div class="form-group">
                    <label>Quantity</label>
                    <input type="number" name="qty" min="1" required>
                </div>
                <button type="submit">Submit Request</button>
            </form>
            <?php else: ?>
                <p style="color: #666;">All available apparatus already have pending or approved requests from your group.</p>
            <?php endif; ?>
        </div>

        <!-- Feature 3: Request Apparatus Not in List -->
        <div class="card">
            <h3>🔍 Can't Find What You Need?</h3>
            <p style="font-size: 14px; color: #666; margin-bottom: 12px;">Request apparatus that is not currently in the selection list. The admin will review your request.</p>
            <span class="toggle-link" onclick="document.getElementById('requestForm').classList.toggle('hidden')">▶ Request Unlisted Apparatus</span>
            <div id="requestForm" class="hidden" style="margin-top: 12px;">
                <form method="POST">
                    <input type="hidden" name="request_apparatus" value="1">
                    <div class="form-group">
                        <label>Apparatus Name</label>
                        <input type="text" name="requested_apparatus_name" placeholder="e.g., Volumetric Flask 500ml" required>
                    </div>
                    <div class="form-group">
                        <label>Reason / Details (optional)</label>
                        <textarea name="request_reason" placeholder="Why do you need this apparatus?"></textarea>
                    </div>
                    <button type="submit" class="btn-request">📩 Submit Request</button>
                </form>
            </div>
        </div>
        
        <div class="card">
            <h3>All Requests</h3>
            <?php if ($myRequests->num_rows > 0): ?>
                <?php while ($r = $myRequests->fetch_assoc()): ?>
                    <div class="request-item <?= $r['status'] ?>">
                        <div><strong><?= htmlspecialchars($r['item_name']) ?></strong> x<?= $r['qty'] ?></div>
                        <span class="status-badge <?= $r['status'] ?>"><?= $r['status'] ?></span>
                    </div>
                <?php endwhile; ?>
            <?php else: ?>
                <p>No requests yet</p>
            <?php endif; ?>
        </div>
    </div>
</body>
</html>
