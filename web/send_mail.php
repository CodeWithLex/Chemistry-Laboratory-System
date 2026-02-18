<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

require_once 'phpmailer/PHPMailer.php';
require_once 'phpmailer/SMTP.php';
require_once 'phpmailer/Exception.php';
require_once 'mail_config.php';

function sendAdminNotification($groupName, $apparatusName, $quantity) {
    $mail = new PHPMailer(true);
    
    try {
        // Server settings
        $mail->isSMTP();
        $mail->Host       = MAIL_HOST;
        $mail->SMTPAuth   = true;
        $mail->Username   = MAIL_USERNAME;
        $mail->Password   = MAIL_PASSWORD;
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
        $mail->Port       = MAIL_PORT;

        // Recipients
        $mail->setFrom(MAIL_FROM, MAIL_FROM_NAME);
        $mail->addAddress(ADMIN_EMAIL);

        // Content
        $mail->isHTML(true);
        $mail->Subject = '🔬 New Apparatus Borrow Request';
        $mail->Body    = "
            <div style='font-family: Arial, sans-serif; padding: 20px;'>
                <h2 style='color: #667eea;'>New Borrow Request</h2>
                <p>A new apparatus borrow request has been submitted:</p>
                <table style='border-collapse: collapse; margin: 20px 0;'>
                    <tr>
                        <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Group:</td>
                        <td style='padding: 10px; border: 1px solid #ddd;'>{$groupName}</td>
                    </tr>
                    <tr>
                        <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Apparatus:</td>
                        <td style='padding: 10px; border: 1px solid #ddd;'>{$apparatusName}</td>
                    </tr>
                    <tr>
                        <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Quantity:</td>
                        <td style='padding: 10px; border: 1px solid #ddd;'>{$quantity}</td>
                    </tr>
                </table>
                <p>Please review and approve/reject this request in the ChemLab desktop application.</p>
            </div>
        ";
        $mail->AltBody = "New Borrow Request\nGroup: {$groupName}\nApparatus: {$apparatusName}\nQuantity: {$quantity}";

        $mail->send();
        return true;
    } catch (Exception $e) {
        error_log("Email error: " . $mail->ErrorInfo);
        return false;
    }
}
?>
