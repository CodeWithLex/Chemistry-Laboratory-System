/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.util.PasswordUtil;
import chemlab_system.util.PasswordUtil.VerifyResult;

/**
 * FXML Controller class
 *
 * @author User
 */
public class LoginPageController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button createButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (usernameField != null) {
            javafx.application.Platform.runLater(() -> usernameField.requestFocus());
        }
        if (usernameField != null) {
            usernameField.setOnAction(event -> {
                if (passwordField != null)
                    passwordField.requestFocus();
            });
        }
        if (passwordField != null) {
            passwordField.setOnAction(event -> {
                if (loginButton != null)
                    loginButton.requestFocus();
            });
        }
        if (loginButton != null) {
            loginButton.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                    loginClicked(null);
            });
        }
        if (createButton != null) {
            createButton.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                    createClciked(null);
            });
        }
    }

    @FXML
    private void loginClicked(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter both username and password.");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Connector_ChemSystem.getConnection();
            if (conn == null) {
                showAlert(AlertType.ERROR, "Connection Error",
                        "Could not connect to the database. Please check your internet connection.");
                return;
            }
            if (conn.isClosed()) {
                showAlert(AlertType.ERROR, "Connection Error", "Database connection is not available.");
                return;
            }

            // Fetch user by username ONLY — never compare plaintext in SQL
            String sql = "SELECT user_id, username, password_hash, full_name, role FROM users WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (!rs.next()) {
                // Generic message — do NOT reveal whether the username exists
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
                return;
            }

            int userId = rs.getInt("user_id");
            String storedHash = rs.getString("password_hash");
            String fullName = rs.getString("full_name");
            String role = rs.getString("role");
            if (role == null || role.isEmpty())
                role = "admin";

            // BCrypt-aware verification with legacy plaintext fallback
            VerifyResult result = PasswordUtil.verify(password, storedHash);

            if (!result.matches) {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
                return;
            }

            // Silently upgrade legacy plaintext passwords to BCrypt on first login
            if (result.needsRehash) {
                try {
                    String newHash = PasswordUtil.hash(password);
                    PreparedStatement upStmt = conn.prepareStatement(
                            "UPDATE users SET password_hash = ? WHERE user_id = ?");
                    upStmt.setString(1, newHash);
                    upStmt.setInt(2, userId);
                    upStmt.executeUpdate();
                    upStmt.close();
                    System.out.println("Password upgraded to BCrypt for user_id=" + userId);
                } catch (SQLException ex) {
                    // Non-fatal — login still proceeds even if rehash fails
                    System.err.println("Non-fatal: failed to upgrade password hash: " + ex.getMessage());
                }
            }

            showAlert(AlertType.INFORMATION, "Success", "Login successful. Welcome, " + fullName + "!");

            // Navigate based on role
            try {
                if ("instructor".equalsIgnoreCase(role)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/instructorDashboard.fxml"));
                    Parent root = loader.load();
                    InstructorDashboardController instrController = loader.getController();
                    instrController.initData(userId, fullName);

                    javafx.geometry.Rectangle2D sb = javafx.stage.Screen.getPrimary().getVisualBounds();
                    double w = Math.max(Math.min(sb.getWidth() * 0.85, 1400), 1000);
                    double h = Math.max(Math.min(sb.getHeight() * 0.85, 850), 600);

                    chemlab_system.ChemLab_System.setMaximizeButtonVisible(true);
                    chemlab_system.ChemLab_System.setContent(root, w, h);
                    chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Instructor Dashboard");
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/adminDashboard.fxml"));
                    Parent root = loader.load();
                    AdminDashboardController dashController = loader.getController();
                    dashController.setAdminName(fullName);

                    javafx.geometry.Rectangle2D sb = javafx.stage.Screen.getPrimary().getVisualBounds();
                    double w = Math.max(Math.min(sb.getWidth() * 0.85, 1400), 1100);
                    double h = Math.max(Math.min(sb.getHeight() * 0.9, 950), 880);

                    chemlab_system.ChemLab_System.setMaximizeButtonVisible(true);
                    chemlab_system.ChemLab_System.setContent(root, w, h);
                    chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Dashboard");
                }
            } catch (Exception ex) {
                System.err.println("Error loading dashboard: " + ex.getMessage());
                ex.printStackTrace();
                showAlert(AlertType.ERROR, "Navigation Error", "Could not load dashboard. Please try again.");
            }

        } catch (SQLException e) {
            System.err.println("Login SQL error: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
        } catch (Exception e) {
            System.err.println("Login unexpected error: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An unexpected error occurred. Please try again.");
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                // Keep connection open for reuse (singleton pattern)
            } catch (SQLException ex) {
                System.err.println("Error closing resources: " + ex.getMessage());
            }
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void createClciked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/create_acc_as.fxml"));
            Parent root = loader.load();
            chemlab_system.ChemLab_System.setContent(root, 500, 400);
            chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Create Account");
        } catch (Exception e) {
            System.err.println("Error loading create account page: " + e.getMessage());
        }
    }
}
