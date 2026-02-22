package ui.controllers_for_ui;

import chemlab_system.database.Connector_ChemSystem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddApparatusController {

    @FXML
    private TextField apparatusNameField;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    public void initialize() {
        // Configure spinner for quantities (min 1, max 1000, default 1)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1);
        quantitySpinner.setValueFactory(valueFactory);
    }

    @FXML
    private void addApparatus(ActionEvent event) {
        String name = apparatusNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Apparatus name cannot be empty.");
            return;
        }

        int quantity = quantitySpinner.getValue();

        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to database.");
            return;
        }

        String sql = "INSERT INTO apparatus (item_name, current_quantity) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            stmt.setInt(2, quantity);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Apparatus added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add apparatus.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error adding apparatus: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) apparatusNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
