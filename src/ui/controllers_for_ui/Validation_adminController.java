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

    // This flag indicates whether admin verification was successful
    private boolean verified = false;

    // Callback to run after successful verification
    private Runnable onVerifiedCallback;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set initial focus on admin username field
        if (adminUsernameField != null) {
            javafx.application.Platform.runLater(() -> adminUsernameField.requestFocus());
        }

        // Enter key navigation
        if (adminUsernameField != null) {
            adminUsernameField.setOnAction(event -> {
                if (adminPasswordField != null) {
                    adminPasswordField.requestFocus();
                }
            });
        }

        if (adminPasswordField != null) {
            adminPasswordField.setOnAction(event -> {
                verifyClicked(null);
            });
        }
    }

    /**
     * Sets the callback to run when admin verification succeeds.
     */
    public void setOnVerifiedCallback(Runnable callback) {
        this.onVerifiedCallback = callback;
    }

    /**
     * Returns whether admin verification was successful.
     */
    public boolean isVerified() {
        return verified;
    }

    @FXML
    private void verifyClicked(ActionEvent event) {
        String adminUsername = adminUsernameField.getText().trim();
        String adminPassword = adminPasswordField.getText();

        // Validation
        if (adminUsername.isEmpty() || adminPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter both admin username and password.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connector_ChemSystem.getConnection();

            if (conn == null) {
                showAlert(AlertType.ERROR, "Connection Error", "Failed to connect to database.");
                return;
            }

            // Check if the provided credentials belong to an existing Admin account
            String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND role = 'Admin'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, adminUsername);
            pstmt.setString(2, adminPassword);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Admin verified successfully
                verified = true;

                // Run the callback (which will create the admin account)
                if (onVerifiedCallback != null) {
                    onVerifiedCallback.run();
                }

                // Close the popup
                Stage stage = (Stage) verifyBtn.getScene().getWindow();
                stage.close();
            } else {
                showAlert(AlertType.ERROR, "Verification Failed",
                        "Invalid admin credentials. Only existing admins can authorize new admin accounts.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error verifying admin: " + e.getMessage());
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                // Keep connection open for reuse
            } catch (SQLException ex) {
                ex.printStackTrace();
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
