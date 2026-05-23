<?php
/**
 * ChemLab System - Database Heartbeat Endpoint
 * Purpose: Lightweight DB ping for external services (UptimeRobot, etc.)
 * This satisfies Supabase "Client Activity" requirements.
 */

// 1. Database Configuration
require_once 'api/config.php'; // Adjust path if necessary

header('Content-Type: application/json');

try {
    // 2. Establish Connection
    $pdo = new PDO($dsn, $user, $pass, $options);
    
    // 3. Perform lightweight DB activity
    // We call the stored procedure established in supabase_keep_alive.sql
    $stmt = $pdo->prepare("SELECT record_heartbeat()");
    $stmt->execute();
    
    // 4. Success Response
    echo json_encode([
        'status' => 'success',
        'message' => 'ChemLab System Heartbeat Captured',
        'timestamp' => date('Y-m-d H:i:s'),
        'service' => 'Supabase Persistence'
    ]);

} catch (\PDOException $e) {
    // 5. Error Response
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Heartbeat Failed: ' . $e->getMessage()
    ]);
}
?>
