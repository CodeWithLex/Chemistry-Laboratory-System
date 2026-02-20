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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import chemlab_system.database.Connector_ChemSystem;

/**
 * FXML Controller class
 * Handles admin account creation (after admin verification has passed).
 *
 * @author User
 */
public class Create_adminController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullnameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPass;

    @FXML
    private Button createAdminBtn;

    @FXML
    private Button backBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set initial focus on username field
        if (usernameField != null) {
            javafx.application.Platform.runLater(() -> usernameField.requestFocus());
        }

        // Enter key navigation
        if (usernameField != null) {
            usernameField.setOnAction(event -> {
                if (fullnameField != null)
                    fullnameField.requestFocus();
            });
        }
        if (fullnameField != null) {
            fullnameField.setOnAction(event -> {
                if (passwordField != null)
                    passwordField.requestFocus();
            });
        }
        if (passwordField != null) {
            passwordField.setOnAction(event -> {
                if (confirmPass != null)
                    confirmPass.requestFocus();
            });
        }
        if (confirmPass != null) {
            confirmPass.setOnAction(event -> {
                createAdminClicked(null);
            });
        }
        if (createAdminBtn != null) {
            createAdminBtn.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    createAdminClicked(null);
                }
            });
        }
    }

    @FXML
    private void createAdminClicked(ActionEvent event) {
        String username = usernameField.getText().trim();
        String fullname = fullnameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPass.getText();

        // Validation
        if (username.isEmpty() || fullname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error",
                    "Please fill in all fields (Username, Full Name, Password, and Confirm Password).");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Password Error", "The password doesn't seem to match.");
            return;
        }

        try {
            Connection conn = Connector_ChemSystem.getConnection();
            String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, 'Admin')";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, username);
            pstmt.setString(2, password); // You should hash this in production
            pstmt.setString(3, fullname);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Admin Account Created Successfully!");
                pstmt.close();

                // Navigate back to login page
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/ui/loginPage.fxml"));
                Parent root = loader.load();
                chemlab_system.ChemLab_System.setContent(root, 1100, 650);
                chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Login");
            } else {
                pstmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error creating admin account: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void backClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/create_acc_as.fxml"));
            Parent root = loader.load();
            chemlab_system.ChemLab_System.setContent(root, 500, 400);
            chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Create Account");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading chooser page: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
