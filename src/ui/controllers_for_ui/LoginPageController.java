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
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set initial focus on username field
        if (usernameField != null) {
            javafx.application.Platform.runLater(() -> usernameField.requestFocus());
        }

        // Set up Enter key to move from username to password field
        if (usernameField != null) {
            usernameField.setOnAction(event -> {
                if (passwordField != null) {
                    passwordField.requestFocus();
                }
            });
        }

        // Set up Enter key in password field to focus login button
        if (passwordField != null) {
            passwordField.setOnAction(event -> {
                if (loginButton != null) {
                    loginButton.requestFocus();
                }
            });
        }

        // Set up Enter key on login button to trigger click
        if (loginButton != null) {
            loginButton.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    loginClicked(null);
                }
            });
        }

        // Set up Enter key on create button to trigger click
        if (createButton != null) {
            createButton.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    createClciked(null);
                }
            });
        }
    }

    @FXML
    private void loginClicked(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter both username and password.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            System.out.println("Attempting to get connection...");
            conn = Connector_ChemSystem.getConnection();

            // Check if connection is valid
            if (conn == null) {
                showAlert(AlertType.ERROR, "Connection Error",
                        "Failed to connect to database. Please check if MySQL is running.");
                return;
            }

            if (conn.isClosed()) {
                showAlert(AlertType.ERROR, "Connection Error", "Database connection is closed.");
                return;
            }

            System.out.println("Connection successful, preparing statement...");
            String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            System.out.println("Executing query...");
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Login successful - get data before closing
                String fullName = rs.getString("full_name");
                String role = "admin"; // default
                try {
                    role = rs.getString("role");
                    if (role == null || role.isEmpty())
                        role = "admin";
                } catch (SQLException ignored) {
                    // role column might not exist yet in older databases
                }
                int userId = rs.getInt("user_id");

                System.out.println("Login successful for: " + fullName + " (role: " + role + ")");

                showAlert(AlertType.INFORMATION, "Success", "Login Successfully");

                // Navigate based on role
                try {
                    if ("instructor".equalsIgnoreCase(role)) {
                        // Load Instructor Dashboard
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/instructorDashboard.fxml"));
                        Parent root = loader.load();

                        InstructorDashboardController instrController = loader.getController();
                        instrController.initData(userId, fullName);

                        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                        double width = Math.min(screenBounds.getWidth() * 0.85, 1400);
                        double height = Math.min(screenBounds.getHeight() * 0.85, 850);
                        width = Math.max(width, 1000);
                        height = Math.max(height, 600);

                        chemlab_system.ChemLab_System.setContent(root, width, height);
                        chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Instructor Dashboard");
                    } else {
                        // Load Admin Dashboard (default)
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/adminDashboard.fxml"));
                        Parent root = loader.load();

                        AdminDashboardController dashboardController = loader.getController();
                        dashboardController.setAdminName(fullName);

                        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                        double width = Math.min(screenBounds.getWidth() * 0.85, 1400);
                        double height = Math.min(screenBounds.getHeight() * 0.85, 850);
                        width = Math.max(width, 1000);
                        height = Math.max(height, 600);

                        chemlab_system.ChemLab_System.setContent(root, width, height);
                        chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Dashboard");
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading dashboard: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert(AlertType.ERROR, "Navigation Error", "Failed to load dashboard.");
                }
            } else {
                // Login failed
                System.out.println("Invalid credentials");
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }

        } catch (SQLException e) {
            System.err.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        } finally {
            // Close only ResultSet and PreparedStatement, keep Connection open for reuse
            try {
                if (rs != null) {
                    rs.close();
                    System.out.println("ResultSet closed");
                }
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("PreparedStatement closed");
                }
                // Do NOT close the connection - it will be reused
                System.out.println("Connection kept open for reuse");
            } catch (SQLException ex) {
                System.err.println("Error closing resources: " + ex.getMessage());
                ex.printStackTrace();
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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/create_acc_as.fxml"));
            javafx.scene.Parent root = loader.load();
            chemlab_system.ChemLab_System.setContent(root, 500, 400);
            chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Create Account");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading create account page: " + e.getMessage());
        }
    }

}
