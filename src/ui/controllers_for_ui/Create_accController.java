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
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import chemlab_system.database.Connector_ChemSystem;

/**
 * FXML Controller class
 *
 * @author User
 */
public class Create_accController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullnameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPass;

    @FXML
    private ComboBox<String> roleBox;

    @FXML
    private ComboBox<Integer> yearBox;

    @FXML
    private ComboBox<String> sectionBox;

    @FXML
    private Button createAccButton;

    @FXML
    private Button loginBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set initial focus on username field
        if (usernameField != null) {
            javafx.application.Platform.runLater(() -> usernameField.requestFocus());
        }

        // Populate role combo box
        if (roleBox != null) {
            roleBox.getItems().addAll("Admin", "Instructor", "Student");
        }

        // Populate year level combo box
        if (yearBox != null) {
            yearBox.getItems().addAll(1, 2, 3, 4);
        }

        // Populate section combo box
        if (sectionBox != null) {
            sectionBox.getItems().addAll("BSCPE-1A", "BSCPE-2A");
        }

        // Set focus traversal order
        if (usernameField != null && fullnameField != null && passwordField != null &&
                confirmPass != null && roleBox != null && yearBox != null &&
                sectionBox != null && createAccButton != null && loginBtn != null) {

            usernameField.setFocusTraversable(true);
            fullnameField.setFocusTraversable(true);
            passwordField.setFocusTraversable(true);
            confirmPass.setFocusTraversable(true);
            roleBox.setFocusTraversable(true);
            yearBox.setFocusTraversable(true);
            sectionBox.setFocusTraversable(true);
            createAccButton.setFocusTraversable(true);
            loginBtn.setFocusTraversable(true);
        }

        // Set up Enter key navigation
        if (usernameField != null) {
            usernameField.setOnAction(event -> {
                if (fullnameField != null) {
                    fullnameField.requestFocus();
                }
            });
        }

        if (fullnameField != null) {
            fullnameField.setOnAction(event -> {
                if (passwordField != null) {
                    passwordField.requestFocus();
                }
            });
        }

        if (passwordField != null) {
            passwordField.setOnAction(event -> {
                if (confirmPass != null) {
                    confirmPass.requestFocus();
                }
            });
        }

        if (confirmPass != null) {
            confirmPass.setOnAction(event -> {
                if (roleBox != null) {
                    roleBox.requestFocus();
                }
            });
        }

        // Set up Enter key on create button to trigger click
        if (createAccButton != null) {
            createAccButton.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    createAccClicked(null);
                }
            });
        }

        // Set up Enter key on login button to trigger click
        if (loginBtn != null) {
            loginBtn.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    loginClicked(null);
                }
            });
        }
    }

    @FXML
    private void createAccClicked(ActionEvent event) {
        String username = usernameField.getText().trim();
        String fullname = fullnameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPass.getText();
        String role = roleBox.getValue();
        Integer yearLevel = yearBox.getValue();
        String section = sectionBox.getValue();

        // Validation
        if (username.isEmpty() || fullname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || role == null) {
            showAlert(AlertType.ERROR, "Validation Error",
                    "Please fill in all required fields (Username, Full Name, Password, and Role).");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Password Error", "The password doesn't seem to match.");
            return;
        }

        try {
            Connection conn = Connector_ChemSystem.getConnection();
            String sql = "INSERT INTO users (username, password_hash, full_name, role, year_level, section) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, username);
            pstmt.setString(2, password); // You should hash this in production
            pstmt.setString(3, fullname);
            pstmt.setString(4, role);

            if (yearLevel != null) {
                pstmt.setInt(5, yearLevel);
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }

            if (section != null && !section.isEmpty()) {
                pstmt.setString(6, section);
            } else {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }

            int result = pstmt.executeUpdate();

            if (result > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Account Created");
                pstmt.close();
                // Do NOT close connection - keep it open for reuse

                // Navigate back to login page
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/ui/loginPage.fxml"));
                Parent root = loader.load();
                chemlab_system.ChemLab_System.setContent(root, 1100, 650);
                chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Login");
            } else {
                pstmt.close();
                // Do NOT close connection - keep it open for reuse
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error creating account: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void loginClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/loginPage.fxml"));
            Parent root = loader.load();
            chemlab_system.ChemLab_System.setContent(root, 1100, 650);
            chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Login");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading login page: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        usernameField.clear();
        fullnameField.clear();
        roleBox.setValue(null);
        yearBox.setValue(null);
        sectionBox.setValue(null);
        usernameField.requestFocus();
    }
}
