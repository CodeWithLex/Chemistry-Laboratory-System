package ui.controllers_for_ui;

import chemlab_system.database.Connector_ChemSystem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

public class AddApparatusController {

    @FXML
    private TextField apparatusNameField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> unitComboBox;

    @FXML
    private Spinner<Double> quantitySpinner;

    @FXML
    public void initialize() {
        // Initialize Type ComboBox
        typeComboBox.setItems(FXCollections.observableArrayList("Apparatus", "Chemical"));
        typeComboBox.setValue("Apparatus");

        // Initialize Unit ComboBox
        unitComboBox.setItems(FXCollections.observableArrayList("pcs", "mL", "L", "g", "kg", "mg"));
        unitComboBox.setValue("pcs");

        // For chemicals, we might need decimals (e.g. 2.5L), so we use Double spinner
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 10000.0, 1.0,
                1.0);
        quantitySpinner.setValueFactory(valueFactory);

        // Ensure manual text input is committed (Fix for value not updating when typed)
        quantitySpinner.getEditor().setOnAction(e -> {
            try {
                String text = quantitySpinner.getEditor().getText();
                Double val = Double.parseDouble(text);
                quantitySpinner.getValueFactory().setValue(val);
            } catch (Exception ex) {
            }
        });
        quantitySpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                try {
                    String text = quantitySpinner.getEditor().getText();
                    Double val = Double.parseDouble(text);
                    quantitySpinner.getValueFactory().setValue(val);
                } catch (Exception ex) {
                }
            }
        });

        // Update unit based on type
        typeComboBox.setOnAction(e -> {
            if ("Chemical".equals(typeComboBox.getValue())) {
                unitComboBox.setValue("mL");
            } else {
                unitComboBox.setValue("pcs");
            }
        });
    }

    @FXML
    private void addApparatus(ActionEvent event) {
        String name = apparatusNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Apparatus name cannot be empty.");
            return;
        }

        double quantity = quantitySpinner.getValue();
        String type = typeComboBox.getValue();
        String unit = unitComboBox.getValue();

        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to database.");
            return;
        }

        String sql = "INSERT INTO apparatus (item_name, item_type, unit, current_quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            stmt.setString(2, type);
            stmt.setString(3, unit);
            stmt.setDouble(4, quantity);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Item added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add item.");
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
