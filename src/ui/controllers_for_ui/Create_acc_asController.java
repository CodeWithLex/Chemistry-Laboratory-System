/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui.controllers_for_ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

/**
 * FXML Controller class
 * Chooser page: Admin or Student Assistant account creation.
 *
 * @author User
 */
public class Create_acc_asController implements Initializable {

    @FXML
    private Button adminBtn;

    @FXML
    private Button saBtn;

    @FXML
    private Button backBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // No initialization needed
    }

    @FXML
    private void adminClicked(ActionEvent event) {
        // Admin path: show admin verification popup first
        try {
            FXMLLoader validationLoader = new FXMLLoader();
            validationLoader.setLocation(getClass().getResource("/ui/validation_admin.fxml"));
            Parent validationRoot = validationLoader.load();

            // Get the validation controller and set callback
            Validation_adminController validationController = validationLoader.getController();
            validationController.setOnVerifiedCallback(() -> {
                // After verification, navigate to the admin creation form
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/ui/create_admin.fxml"));
                    Parent root = loader.load();
                    chemlab_system.ChemLab_System.setContent(root, 454, 590);
                    chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Create Admin Account");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error loading admin creation page: " + e.getMessage());
                }
            });

            // Show verification as a modal popup
            javafx.scene.Scene scene = new javafx.scene.Scene(validationRoot);
            javafx.stage.Stage popupStage = new javafx.stage.Stage();
            popupStage.setTitle("Admin Verification Required");
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.initStyle(javafx.stage.StageStyle.UTILITY);
            popupStage.setScene(scene);
            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open admin verification: " + e.getMessage());
        }
    }

    @FXML
    private void studentAssistantClicked(ActionEvent event) {
        // Navigate directly to student assistant account creation
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/createAcc_page.fxml"));
            Parent root = loader.load();
            chemlab_system.ChemLab_System.setContent(root, 454, 590);
            chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Create Student Assistant Account");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open account creation: " + e.getMessage());
        }
    }

    @FXML
    private void backClicked(ActionEvent event) {
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
}
