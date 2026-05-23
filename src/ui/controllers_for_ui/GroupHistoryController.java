package ui.controllers_for_ui;

import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.model.BorrowRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class GroupHistoryController {

    @FXML
    private Label titleLabel;
    @FXML
    private TableView<BorrowRequest> historyTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colReqId;
    @FXML
    private TableColumn<BorrowRequest, String> colApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colQty;
    @FXML
    private TableColumn<BorrowRequest, String> colStatus;
    @FXML
    private TableColumn<BorrowRequest, String> colRequested;

    private int groupId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    @FXML
    public void initialize() {
        colReqId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colApparatus.setCellValueFactory(new PropertyValueFactory<>("apparatusName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colRequested.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getCreatedAt();
            if (ts != null) {
                return new SimpleStringProperty(dateFormat.format(ts));
            }
            return new SimpleStringProperty("");
        });
    }

    public void initData(int groupId, String groupName) {
        this.groupId = groupId;
        titleLabel.setText("History for " + groupName);
        loadGroupHistory();
    }

    private void loadGroupHistory() {
        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("GroupHistoryController: Connection is null!");
            return;
        }

        String sql = "SELECT r.request_id, a.item_name, r.qty, r.status, r.created_at " +
                "FROM requests r " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.group_id = ? " +
                "ORDER BY r.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = new BorrowRequest();
                    req.setRequestId(rs.getInt("request_id"));
                    req.setApparatusName(rs.getString("item_name") != null ? rs.getString("item_name") : "Unknown");
                    req.setQty(rs.getInt("qty"));
                    req.setStatus(rs.getString("status"));
                    req.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("GroupHistoryController SQL error: " + e.getMessage());
            e.printStackTrace();
        }

        historyTable.setItems(list);
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) historyTable.getScene().getWindow();
        stage.close();
    }
}
