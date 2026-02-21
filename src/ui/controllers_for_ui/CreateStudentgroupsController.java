/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui.controllers_for_ui;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
 * Handles student group creation from the admin dashboard.
 *
 * @author User
 */
public class CreateStudentgroupsController implements Initializable {

    @FXML
    private TextField groupNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPassField;

    @FXML
    private Button createGroupBtn;

    @FXML
    private Button cancelBtn;

    // Flag to indicate if a group was successfully created
    private boolean created = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set initial focus on group name field
        if (groupNameField != null) {
            javafx.application.Platform.runLater(() -> groupNameField.requestFocus());
        }

        // Enter key navigation between fields
        if (groupNameField != null) {
            groupNameField.setOnAction(event -> {
                if (usernameField != null)
                    usernameField.requestFocus();
            });
        }
        if (usernameField != null) {
            usernameField.setOnAction(event -> {
                if (emailField != null)
                    emailField.requestFocus();
            });
        }
        if (emailField != null) {
            emailField.setOnAction(event -> {
                if (passwordField != null)
                    passwordField.requestFocus();
            });
        }
        if (passwordField != null) {
            passwordField.setOnAction(event -> {
                if (confirmPassField != null)
                    confirmPassField.requestFocus();
            });
        }
        if (confirmPassField != null) {
            confirmPassField.setOnAction(event -> {
                createGroupClicked(null);
            });
        }
    }

    /**
     * Returns whether a group was successfully created.
     */
    public boolean isCreated() {
        return created;
    }

    @FXML
    private void createGroupClicked(ActionEvent event) {
        String groupName = groupNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPassField.getText();

        // Validation — all fields required
        if (groupName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error",
                    "Please fill in all fields (Group Name, Username, Gmail, Password, and Confirm Password)."
            );
            return;
        }

        // Basic email validation (gmail only as requested)
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter a valid Gmail address (ending with @gmail.com)."
            );
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Password Error", "The passwords don't match.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = Connector_ChemSystem.getConnection();
            if (conn == null) {
                showAlert(AlertType.ERROR, "Connection Error", "Failed to connect to database.");
                return;
            }

            String sql = "INSERT INTO student_groups (group_name, username, email, password) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                created = true;
                showAlert(AlertType.INFORMATION, "Success",
                        "Student Group \"" + groupName + "\" created successfully!");
                // Close the popup
                Stage stage = (Stage) createGroupBtn.getScene().getWindow();
                stage.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate")) {
                showAlert(AlertType.ERROR, "Duplicate Error",
                        "A group with that username already exists. Please choose a different username.");
            } else {
                showAlert(AlertType.ERROR, "Database Error", "Error creating student group: " + e.getMessage());
            }
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void cancelClicked(ActionEvent event) {
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
