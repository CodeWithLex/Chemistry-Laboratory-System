package ui.controllers_for_ui;

import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.model.ActivityTemplateItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddActivityTemplateController {

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<ApparatusOption> apparatusComboBox;
    @FXML
    private TextField qtyField;
    @FXML
    private TableView<ActivityTemplateItem> itemsTable;
    @FXML
    private TableColumn<ActivityTemplateItem, String> colItemName;
    @FXML
    private TableColumn<ActivityTemplateItem, Integer> colItemQty;

    private ObservableList<ActivityTemplateItem> templateItems = FXCollections.observableArrayList();
    private List<ApparatusOption> allApparatusOptions = new ArrayList<>();

    public void initialize() {
        colItemName.setCellValueFactory(data -> data.getValue().apparatusNameProperty());
        colItemQty.setCellValueFactory(data -> data.getValue().fixedQtyProperty().asObject());
        itemsTable.setItems(templateItems);

        loadApparatusOptions();
    }

    private void loadApparatusOptions() {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT apparatus_id, item_name FROM apparatus ORDER BY item_name")) {
            while (rs.next()) {
                allApparatusOptions.add(new ApparatusOption(
                        rs.getInt("apparatus_id"),
                        rs.getString("item_name")));
            }
            apparatusComboBox.setItems(FXCollections.observableArrayList(allApparatusOptions));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addItemToList(ActionEvent event) {
        ApparatusOption selected = apparatusComboBox.getSelectionModel().getSelectedItem();
        String qtyStr = qtyField.getText();

        if (selected == null || qtyStr == null || qtyStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Select an apparatus and enter a quantity.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0)
                throw new NumberFormatException();

            // Check if already in list
            for (ActivityTemplateItem item : templateItems) {
                if (item.getApparatusId() == selected.getId()) {
                    showAlert(Alert.AlertType.WARNING, "Duplicate", "Item already added to this template.");
                    return;
                }
            }

            templateItems.add(new ActivityTemplateItem(selected.getId(), selected.getName(), qty));
            qtyField.clear();
            apparatusComboBox.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Please enter a positive whole number.");
        }
    }

    @FXML
    private void removeItemFromList(ActionEvent event) {
        ActivityTemplateItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            templateItems.remove(selected);
        }
    }

    @FXML
    private void saveTemplate(ActionEvent event) {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Activity name is required.");
            return;
        }

        if (templateItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please add at least one apparatus to the template.");
            return;
        }

        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        try {
            conn.setAutoCommit(false);

            // 1. Insert template
            String insertTemplateSql = "INSERT INTO activity_templates (activity_name, description) VALUES (?, ?) RETURNING template_id";
            java.util.UUID templateId = null;

            try (PreparedStatement ps = conn.prepareStatement(insertTemplateSql)) {
                ps.setString(1, name.trim());
                ps.setString(2, descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        templateId = (java.util.UUID) rs.getObject("template_id");
                    }
                }
            }

            if (templateId == null)
                throw new SQLException("Failed to retrieve template_id.");

            // 2. Insert items
            String insertItemsSql = "INSERT INTO activity_template_items (template_id, apparatus_id, fixed_qty) VALUES (?, ?, ?)";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemsSql)) {
                for (ActivityTemplateItem item : templateItems) {
                    psItem.setObject(1, templateId);
                    psItem.setInt(2, item.getApparatusId());
                    psItem.setInt(3, item.getFixedQty());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Template created successfully!");
            closeWindow();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save template: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for ComboBox selection
    public static class ApparatusOption {
        private final int id;
        private final String name;

        public ApparatusOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
