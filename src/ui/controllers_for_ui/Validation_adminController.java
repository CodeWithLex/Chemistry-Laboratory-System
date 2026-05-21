/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui.controllers_for_ui;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.util.PasswordUtil;
import chemlab_system.util.PasswordUtil.VerifyResult;

/**
 * FXML Controller class
 * Handles admin verification before allowing admin account creation.
 *
 * @author User
 */
public class Validation_adminController implements Initializable {

    @FXML
    private TextField adminUsernameField;
    @FXML
    private PasswordField adminPasswordField;
    @FXML
    private Button verifyBtn;
    @FXML
    private Button cancelBtn;

    private boolean verified = false;
    private Runnable onVerifiedCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (adminUsernameField != null) {
            javafx.application.Platform.runLater(() -> adminUsernameField.requestFocus());
        }
        if (adminUsernameField != null) {
            adminUsernameField.setOnAction(event -> {
                if (adminPasswordField != null)
                    adminPasswordField.requestFocus();
            });
        }
        if (adminPasswordField != null) {
            adminPasswordField.setOnAction(event -> verifyClicked(null));
        }
    }

    public void setOnVerifiedCallback(Runnable callback) {
        this.onVerifiedCallback = callback;
    }

    public boolean isVerified() {
        return verified;
    }

    @FXML
    private void verifyClicked(ActionEvent event) {
        String adminUsername = adminUsernameField.getText().trim();
        String adminPassword = adminPasswordField.getText();

        if (adminUsername.isEmpty() || adminPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter both admin username and password.");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Connector_ChemSystem.getConnection();
            if (conn == null) {
                showAlert(AlertType.ERROR, "Connection Error", "Could not connect to the database.");
                return;
            }

            // Fetch by username + role ONLY — verify password separately with BCrypt
            String sql = "SELECT user_id, password_hash FROM users WHERE username = ? AND role = 'Admin'";
            ps = conn.prepareStatement(sql);
            ps.setString(1, adminUsername);
            rs = ps.executeQuery();

            boolean isAdmin = false;
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String storedHash = rs.getString("password_hash");

                VerifyResult result = PasswordUtil.verify(adminPassword, storedHash);
                if (result.matches) {
                    isAdmin = true;

                    // Silently upgrade legacy plaintext to BCrypt
                    if (result.needsRehash) {
                        try {
                            String newHash = PasswordUtil.hash(adminPassword);
                            PreparedStatement upStmt = conn.prepareStatement(
                                    "UPDATE users SET password_hash = ? WHERE user_id = ?");
                            upStmt.setString(1, newHash);
                            upStmt.setInt(2, userId);
                            upStmt.executeUpdate();
                            upStmt.close();
                            System.out.println("Password upgraded to BCrypt for admin user_id=" + userId);
                        } catch (SQLException ex) {
                            System.err.println("Non-fatal: failed to upgrade admin hash: " + ex.getMessage());
                        }
                    }
                }
            }

            if (isAdmin) {
                verified = true;
                if (onVerifiedCallback != null) {
                    onVerifiedCallback.run();
                }
                Stage stage = (Stage) verifyBtn.getScene().getWindow();
                stage.close();
            } else {
                showAlert(AlertType.ERROR, "Verification Failed",
                        "Invalid admin credentials. Only existing admins can authorise new admin accounts.");
            }

        } catch (SQLException e) {
            System.err.println("Admin verify SQL error: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Verification failed. Please try again.");
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException ex) {
                System.err.println("Error closing resources: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void cancelClicked(ActionEvent event) {
        verified = false;
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
