/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui.controllers_for_ui;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.model.BorrowRequest;
import chemlab_system.util.EmailService;

/**
 * FXML Controller class for Admin Dashboard
 *
 * @author User
 */
public class AdminDashboardController implements Initializable {

    // Header
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutBtn;

    // Stats
    @FXML
    private Label pendingCountLabel;
    @FXML
    private Label approvedCountLabel;
    @FXML
    private Label totalApparatusLabel;
    @FXML
    private Label totalUsersLabel;

    // Tabs
    @FXML
    private TabPane tabPane;

    // Pending Requests Table
    @FXML
    private TableView<BorrowRequest> requestsTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colRequestId;
    @FXML
    private TableColumn<BorrowRequest, String> colGroupName;
    @FXML
    private TableColumn<BorrowRequest, String> colApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colQty;
    @FXML
    private TableColumn<BorrowRequest, String> colStatus;
    @FXML
    private TableColumn<BorrowRequest, String> colDate;

    // All Requests Table
    @FXML
    private TableView<BorrowRequest> allRequestsTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colAllId;
    @FXML
    private TableColumn<BorrowRequest, String> colAllGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colAllApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colAllQty;
    @FXML
    private TableColumn<BorrowRequest, String> colAllStatus;
    @FXML
    private TableColumn<BorrowRequest, String> colAllDate;

    // Apparatus Table
    @FXML
    private TableView<ApparatusItem> apparatusTable;
    @FXML
    private TableColumn<ApparatusItem, Integer> colAppId;
    @FXML
    private TableColumn<ApparatusItem, String> colAppName;
    @FXML
    private TableColumn<ApparatusItem, Integer> colAppQty;
    @FXML
    private TableColumn<ApparatusItem, Integer> colAppRemaining;

    // Student Groups Table
    @FXML
    private TableView<StudentGroupItem> groupsTable;
    @FXML
    private TableColumn<StudentGroupItem, Integer> colGroupId;
    @FXML
    private TableColumn<StudentGroupItem, String> colGrpName;
    @FXML
    private TableColumn<StudentGroupItem, String> colGrpUsername;

    // Currently In Use Table
    @FXML
    private TableView<BorrowRequest> inUseTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colInUseId;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colInUseQty;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseDate;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseRemaining;

    // Date filters
    @FXML
    private DatePicker pendingDatePicker;
    @FXML
    private DatePicker inUseDatePicker;

    // History Table
    @FXML
    private TableView<BorrowRequest> historyTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colHistId;
    @FXML
    private TableColumn<BorrowRequest, String> colHistGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colHistApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colHistQty;
    @FXML
    private TableColumn<BorrowRequest, String> colHistBorrowed;
    @FXML
    private TableColumn<BorrowRequest, String> colHistReturned;
    @FXML
    private TableColumn<BorrowRequest, String> colHistDuration;

    // Action Buttons
    @FXML
    private Button refreshBtn;
    @FXML
    private Button approveBtn;
    @FXML
    private Button rejectBtn;
    @FXML
    private Button returnedBtn;

    // Logged in admin name
    private String adminFullName = "Admin";

    /**
     * Sets the admin's full name (called from LoginPageController after login).
     */
    public void setAdminName(String name) {
        this.adminFullName = name;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + name);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup Pending Requests Table columns
        colRequestId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colGroupName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colDate.setCellValueFactory(data -> {
            Timestamp ts = data.getValue().getCreatedAt();
            return new SimpleStringProperty(ts != null ? ts.toString() : "");
        });

        // Setup All Requests Table columns
        colAllId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colAllGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colAllApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colAllQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colAllStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colAllDate.setCellValueFactory(data -> {
            Timestamp ts = data.getValue().getCreatedAt();
            return new SimpleStringProperty(ts != null ? ts.toString() : "");
        });

        // Setup Apparatus Table columns
        colAppId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAppName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colAppQty.setCellValueFactory(new PropertyValueFactory<>("currentQuantity"));
        if (colAppRemaining != null) {
            colAppRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingQuantity"));
        }

        // Setup Student Groups Table columns
        colGroupId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colGrpName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colGrpUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        // Setup Currently In Use Table columns
        colInUseId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colInUseGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colInUseApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colInUseQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colInUseDate.setCellValueFactory(data -> {
            Timestamp ts = data.getValue().getUpdatedAt();
            return new SimpleStringProperty(ts != null ? ts.toString() : "");
        });
        if (colInUseRemaining != null) {
            colInUseRemaining.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        }

        // Setup History Table columns
        if (colHistId != null) {
            colHistId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
            colHistGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
            colHistApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
            colHistQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
            colHistBorrowed.setCellValueFactory(data -> {
                Timestamp ts = data.getValue().getCreatedAt();
                return new SimpleStringProperty(ts != null ? ts.toString() : "");
            });
            colHistReturned.setCellValueFactory(data -> {
                Timestamp ts = data.getValue().getUpdatedAt();
                return new SimpleStringProperty(ts != null ? ts.toString() : "");
            });
            colHistDuration.setCellValueFactory(data -> new SimpleStringProperty(calculateDurationText(data.getValue().getCreatedAt(), data.getValue().getUpdatedAt())));
        }

        // Apply cell style to ensure text is visible (fix for CSS inheritance issues)
        applyTextCellStyle(colRequestId, colGroupName, colApparatus, colQty, colStatus, colDate);
        applyTextCellStyle(colAllId, colAllGroup, colAllApparatus, colAllQty, colAllStatus, colAllDate);
        if (colAppRemaining != null) {
            applyTextCellStyle(colAppId, colAppName, colAppQty, colAppRemaining);
        } else {
            applyTextCellStyle(colAppId, colAppName, colAppQty);
        }
        applyTextCellStyle(colGroupId, colGrpName, colGrpUsername);
        applyTextCellStyle(colInUseId, colInUseGroup, colInUseApparatus, colInUseQty, colInUseDate, colInUseRemaining);
        if (colHistId != null) {
            applyTextCellStyle(colHistId, colHistGroup, colHistApparatus, colHistQty, colHistBorrowed, colHistReturned, colHistDuration);
        }

        // Set column resize policy so columns auto-fill the table width
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        allRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        apparatusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        groupsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        inUseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        if (historyTable != null) {
            historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Disable column reordering and sorting on all columns
        disableReorderAndSort(colRequestId, colGroupName, colApparatus, colQty, colStatus, colDate);
        disableReorderAndSort(colAllId, colAllGroup, colAllApparatus, colAllQty, colAllStatus, colAllDate);
        if (colAppRemaining != null) {
            disableReorderAndSort(colAppId, colAppName, colAppQty, colAppRemaining);
        } else {
            disableReorderAndSort(colAppId, colAppName, colAppQty);
        }
        disableReorderAndSort(colGroupId, colGrpName, colGrpUsername);
        disableReorderAndSort(colInUseId, colInUseGroup, colInUseApparatus, colInUseQty, colInUseDate, colInUseRemaining);
        if (colHistId != null) {
            disableReorderAndSort(colHistId, colHistGroup, colHistApparatus, colHistQty, colHistBorrowed, colHistReturned, colHistDuration);
        }

        // Set minimum widths to prevent text truncation
        colRequestId.setMinWidth(60);
        colGroupName.setMinWidth(130);
        colApparatus.setMinWidth(150);
        colQty.setMinWidth(50);
        colStatus.setMinWidth(90);
        colDate.setMinWidth(170);

        colAllId.setMinWidth(60);
        colAllGroup.setMinWidth(130);
        colAllApparatus.setMinWidth(150);
        colAllQty.setMinWidth(50);
        colAllStatus.setMinWidth(90);
        colAllDate.setMinWidth(170);

        colAppId.setMinWidth(60);
        colAppName.setMinWidth(200);
        colAppQty.setMinWidth(120);
        if (colAppRemaining != null) {
            colAppRemaining.setMinWidth(140);
        }

        colGroupId.setMinWidth(60);
        colGrpName.setMinWidth(200);
        colGrpUsername.setMinWidth(150);

        colInUseId.setMinWidth(60);
        colInUseGroup.setMinWidth(130);
        colInUseApparatus.setMinWidth(150);
        colInUseQty.setMinWidth(50);
        colInUseDate.setMinWidth(170);
        if (colInUseRemaining != null) {
            colInUseRemaining.setMinWidth(150);
        }

        if (colHistId != null) {
            colHistId.setMinWidth(60);
            colHistGroup.setMinWidth(130);
            colHistApparatus.setMinWidth(150);
            colHistQty.setMinWidth(50);
            colHistBorrowed.setMinWidth(170);
            colHistReturned.setMinWidth(170);
            colHistDuration.setMinWidth(130);
        }

        // Load initial data
        loadStats();
        loadPendingRequests();

        // Auto-load data when switching tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                switch (newTab.getText()) {
                    case "Pending Requests":
                        loadPendingRequests();
                        break;
                    case "All Requests":
                        loadAllRequests();
                        break;
                    case "Currently In Use":
                        loadCurrentlyInUse();
                        break;
                    case "Apparatus Inventory":
                        loadApparatus();
                        break;
                    case "Student Groups":
                        loadStudentGroups();
                        break;
                    case "History":
                        loadHistory();
                        break;
                }
                loadStats();
            }
        });
    }

    /**
     * Disables sorting on the given columns.
     */
    @SafeVarargs
    private final void disableReorderAndSort(TableColumn<?, ?>... columns) {
        for (TableColumn<?, ?> col : columns) {
            col.setSortable(false);
        }
    }

    /**
     * Sets a custom cell factory on each column to force readable text style.
     */
    @SafeVarargs
    private final void applyTextCellStyle(TableColumn<?, ?>... columns) {
        for (TableColumn col : columns) {
            col.setCellFactory(column -> {
                return new javafx.scene.control.TableCell<Object, Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                            setStyle(
                                    "-fx-font-size: 13px; -fx-text-fill: #333333; -fx-padding: 4 8;");
                        }
                    }
                };
            });
        }
    }

    // ==================== DATA LOADING ====================

    private void loadStats() {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        try {
            // Pending count
            try (java.sql.Statement st1 = conn.createStatement();
                    ResultSet rs1 = st1.executeQuery(
                            "SELECT COUNT(*) AS cnt FROM requests WHERE status = 'Pending'")) {
                if (rs1.next())
                    pendingCountLabel.setText(String.valueOf(rs1.getInt("cnt")));
            }

            // Approved today count
            try (java.sql.Statement st2 = conn.createStatement();
                    ResultSet rs2 = st2.executeQuery(
                            "SELECT COUNT(*) AS cnt FROM requests WHERE status = 'Approved' AND DATE(updated_at) = CURDATE()")) {
                if (rs2.next())
                    approvedCountLabel.setText(String.valueOf(rs2.getInt("cnt")));
            }

            // Total apparatus
            try (java.sql.Statement st3 = conn.createStatement();
                    ResultSet rs3 = st3.executeQuery(
                            "SELECT COUNT(*) AS cnt FROM apparatus")) {
                if (rs3.next())
                    totalApparatusLabel.setText(String.valueOf(rs3.getInt("cnt")));
            }

            // Total users
            try (java.sql.Statement st4 = conn.createStatement();
                    ResultSet rs4 = st4.executeQuery(
                            "SELECT COUNT(*) AS cnt FROM users")) {
                if (rs4.next())
                    totalUsersLabel.setText(String.valueOf(rs4.getInt("cnt")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingRequests() {
        loadPendingRequests(null);
    }

    private void loadPendingRequests(LocalDate filterDate) {
        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("loadPendingRequests: Connection is null!");
            return;
        }

        // Use LEFT JOINs so requests still show even if group/apparatus data is missing
        // Use aliases for the selected columns to avoid column-name issues
        String sql = "SELECT r.request_id AS rid, " +
                "g.group_name AS gname, " +
                "a.item_name AS aname, " +
                "r.qty AS rqty, r.status AS rstatus, r.created_at AS rdate " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Pending'";

        if (filterDate != null) {
            sql += " AND DATE(r.created_at) = ?";
        }
        sql += " ORDER BY r.created_at";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filterDate != null) {
                stmt.setDate(1, Date.valueOf(filterDate));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = new BorrowRequest();
                    req.setRequestId(rs.getInt("rid"));
                    req.setGroupName(rs.getString("gname") != null ? rs.getString("gname") : "Unknown Group");
                    req.setApparatusName(rs.getString("aname") != null ? rs.getString("aname") : "Unknown");
                    req.setQty(rs.getInt("rqty"));
                    req.setStatus(rs.getString("rstatus"));
                    req.setCreatedAt(rs.getTimestamp("rdate"));
                    list.add(req);
                }
            }

            System.out.println("loadPendingRequests: Loaded " + list.size() + " pending requests");
        } catch (SQLException e) {
            System.err.println("loadPendingRequests SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        requestsTable.setItems(list);
    }

    private void loadAllRequests() {
        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("loadAllRequests: Connection is null!");
            return;
        }

        String sql = "SELECT r.request_id AS rid, " +
                "g.group_name AS gname, " +
                "a.item_name AS aname, " +
                "r.qty AS rqty, r.status AS rstatus, r.created_at AS rdate " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "ORDER BY r.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId(rs.getInt("rid"));
                req.setGroupName(rs.getString("gname") != null ? rs.getString("gname") : "Unknown Group");
                req.setApparatusName(rs.getString("aname") != null ? rs.getString("aname") : "Unknown");
                req.setQty(rs.getInt("rqty"));
                req.setStatus(rs.getString("rstatus"));
                req.setCreatedAt(rs.getTimestamp("rdate"));
                list.add(req);
            }
            System.out.println("loadAllRequests: Loaded " + list.size() + " total requests");
        } catch (SQLException e) {
            System.err.println("loadAllRequests SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        allRequestsTable.setItems(list);
    }

    private void loadApparatus() {
        ObservableList<ApparatusItem> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("loadApparatus: Connection is null!");
            return;
        }

        String sql = "SELECT a.apparatus_id, a.item_name, a.current_quantity, " +
                "GREATEST(a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved','Pending') THEN r.qty ELSE 0 END), 0), 0) AS remaining_qty " +
                "FROM apparatus a " +
                "LEFT JOIN requests r ON a.apparatus_id = r.apparatus_id " +
                "GROUP BY a.apparatus_id, a.item_name, a.current_quantity " +
                "ORDER BY a.item_name";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ApparatusItem(
                        rs.getInt("apparatus_id"),
                        rs.getString("item_name"),
                        rs.getInt("current_quantity"),
                        rs.getInt("remaining_qty")));
            }
            System.out.println("loadApparatus: Loaded " + list.size() + " apparatus items");
        } catch (SQLException e) {
            System.err.println("loadApparatus SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        apparatusTable.setItems(list);
    }

    private void loadStudentGroups() {
        ObservableList<StudentGroupItem> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("loadStudentGroups: Connection is null!");
            return;
        }

        String sql = "SELECT group_id, group_name, username FROM student_groups ORDER BY group_name";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new StudentGroupItem(
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getString("username")));
            }
            System.out.println("loadStudentGroups: Loaded " + list.size() + " student groups");
        } catch (SQLException e) {
            System.err.println("loadStudentGroups SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        groupsTable.setItems(list);
    }

    private void loadCurrentlyInUse() {
        loadCurrentlyInUse(null);
    }

    private void loadCurrentlyInUse(LocalDate filterDate) {
        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("loadCurrentlyInUse: Connection is null!");
            return;
        }

        String sql = "SELECT r.request_id AS rid, " +
                "g.group_name AS gname, " +
                "a.item_name AS aname, " +
                "r.qty AS rqty, r.updated_at AS rdate, " +
                "GREATEST(a.current_quantity - COALESCE(SUM(CASE WHEN r2.status IN ('Approved','Pending') THEN r2.qty ELSE 0 END), 0), 0) AS remaining_qty " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "LEFT JOIN requests r2 ON r2.apparatus_id = r.apparatus_id " +
                "WHERE r.status = 'Approved'";

        if (filterDate != null) {
            sql += " AND DATE(r.updated_at) = ?";
        }
        sql += " GROUP BY r.request_id, g.group_name, a.item_name, r.qty, r.updated_at, a.current_quantity";
        sql += " ORDER BY r.updated_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filterDate != null) {
                stmt.setDate(1, Date.valueOf(filterDate));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = new BorrowRequest();
                    req.setRequestId(rs.getInt("rid"));
                    req.setGroupName(rs.getString("gname") != null ? rs.getString("gname") : "Unknown Group");
                    req.setApparatusName(rs.getString("aname") != null ? rs.getString("aname") : "Unknown");
                    req.setQty(rs.getInt("rqty"));
                    req.setUpdatedAt(rs.getTimestamp("rdate"));
                    // store remaining in status temporarily for display column
                    req.setStatus("Remaining: " + rs.getInt("remaining_qty"));
                    list.add(req);
                }
            }

            System.out.println("loadCurrentlyInUse: Loaded " + list.size() + " in-use items");
        } catch (SQLException e) {
            System.err.println("loadCurrentlyInUse SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        inUseTable.setItems(list);
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void refreshRequests(ActionEvent event) {
        loadPendingRequests();
        loadStats();
    }

    @FXML
    private void refreshAllRequests(ActionEvent event) {
        loadAllRequests();
        loadStats();
    }

    @FXML
    private void refreshApparatus(ActionEvent event) {
        loadApparatus();
        loadStats();
    }

    @FXML
    private void refreshGroups(ActionEvent event) {
        loadStudentGroups();
        loadStats();
    }

    @FXML
    private void refreshInUse(ActionEvent event) {
        loadCurrentlyInUse();
        loadStats();
    }

    @FXML
    private void openAddGroupDialog(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/createStudentgroups.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create Student Group");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh the groups table after dialog closes
            loadStudentGroups();
            loadStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open Add Group dialog: " + e.getMessage());
        }
    }

    @FXML
    private void approveSelected(ActionEvent event) {
        BorrowRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a request to approve.");
            return;
        }
        updateRequestStatus(selected.getRequestId(), "Approved");
    }

    @FXML
    private void rejectSelected(ActionEvent event) {
        BorrowRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a request to reject.");
            return;
        }
        updateRequestStatus(selected.getRequestId(), "Rejected");
    }

    @FXML
    private void markReturnedSelected(ActionEvent event) {
        BorrowRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null && inUseTable != null) {
            selected = inUseTable.getSelectionModel().getSelectedItem();
        }
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a request to mark as returned.");
            return;
        }
        updateRequestStatus(selected.getRequestId(), "Returned");
    }

    private void updateRequestStatus(int requestId, String newStatus) {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql = "UPDATE requests SET status = ? WHERE request_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, requestId);
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert(AlertType.INFORMATION, "Success",
                        "Request #" + requestId + " has been " + newStatus + ".");

                if ("Approved".equalsIgnoreCase(newStatus) || "Rejected".equalsIgnoreCase(newStatus)) {
                    sendEmailNotificationForRequest(requestId, newStatus);
                }

                loadPendingRequests();
                loadCurrentlyInUse();
                loadApparatus();
                loadStats();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to update request: " + e.getMessage());
        }
    }

    @FXML
    private void logoutClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/loginPage.fxml"));
            Parent root = loader.load();
            chemlab_system.ChemLab_System.setContent(root, 1100, 650);
            chemlab_system.ChemLab_System.setTitle("Chemistry Laboratory System - Login");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error navigating to login: " + e.getMessage());
        }
    }

    // ==================== UTILITY ====================

    private void sendEmailNotificationForRequest(int requestId, String newStatus) {
        Thread t = new Thread(() -> {
            Connection conn = Connector_ChemSystem.getConnection();
            if (conn == null)
                return;

            String sql = "SELECT g.email AS email, g.group_name AS gname, a.item_name AS aname, r.qty AS qty " +
                    "FROM requests r " +
                    "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                    "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                    "WHERE r.request_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, requestId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        EmailService.sendStatusNotification(
                                rs.getString("email"),
                                rs.getString("gname") != null ? rs.getString("gname") : "Student Group",
                                rs.getString("aname") != null ? rs.getString("aname") : "Apparatus",
                                rs.getInt("qty"),
                                newStatus);
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Email notification query failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, "AdminDashboardController-emailQuery");
        t.setDaemon(true);
        t.start();
    }

    private void loadHistory() {
        if (historyTable == null)
            return;

        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql = "SELECT r.request_id AS rid, g.group_name AS gname, a.item_name AS aname, " +
                "r.qty AS rqty, r.created_at AS created_at, r.updated_at AS updated_at " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Returned' " +
                "ORDER BY r.updated_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId(rs.getInt("rid"));
                req.setGroupName(rs.getString("gname") != null ? rs.getString("gname") : "Unknown Group");
                req.setApparatusName(rs.getString("aname") != null ? rs.getString("aname") : "Unknown");
                req.setQty(rs.getInt("rqty"));
                req.setCreatedAt(rs.getTimestamp("created_at"));
                req.setUpdatedAt(rs.getTimestamp("updated_at"));
                req.setStatus("Returned");
                list.add(req);
            }
        } catch (SQLException e) {
            System.err.println("loadHistory SQL error: " + e.getMessage());
            e.printStackTrace();
        }

        historyTable.setItems(list);
    }

    private String calculateDurationText(Timestamp start, Timestamp end) {
        if (start == null || end == null)
            return "";

        long diffMs = end.getTime() - start.getTime();
        if (diffMs < 0)
            diffMs = 0;

        long totalHours = diffMs / (1000L * 60L * 60L);
        long days = totalHours / 24;
        long hours = totalHours % 24;

        if (days > 0) {
            return days + " days, " + hours + " hours";
        }
        return hours + " hours";
    }

    @FXML
    private void refreshHistory(ActionEvent event) {
        loadHistory();
        loadStats();
    }

    @FXML
    private void filterPendingByDate() {
        LocalDate d = pendingDatePicker != null ? pendingDatePicker.getValue() : null;
        loadPendingRequests(d);
    }

    @FXML
    private void clearPendingFilter() {
        if (pendingDatePicker != null) {
            pendingDatePicker.setValue(null);
        }
        loadPendingRequests(null);
    }

    @FXML
    private void filterInUseByDate() {
        LocalDate d = inUseDatePicker != null ? inUseDatePicker.getValue() : null;
        loadCurrentlyInUse(d);
    }

    @FXML
    private void clearInUseFilter() {
        if (inUseDatePicker != null) {
            inUseDatePicker.setValue(null);
        }
        loadCurrentlyInUse(null);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== INNER CLASS: ApparatusItem ====================

    /**
     * Simple model for the Apparatus Inventory table.
     */
    public static class ApparatusItem {
        private final int id;
        private final String itemName;
        private final int currentQuantity;
        private final int remainingQuantity;

        public ApparatusItem(int id, String itemName, int currentQuantity, int remainingQuantity) {
            this.id = id;
            this.itemName = itemName;
            this.currentQuantity = currentQuantity;
            this.remainingQuantity = remainingQuantity;
        }

        public int getId() {
            return id;
        }

        public String getItemName() {
            return itemName;
        }

        public int getCurrentQuantity() {
            return currentQuantity;
        }

        public int getRemainingQuantity() {
            return remainingQuantity;
        }
    }

    // ==================== INNER CLASS: StudentGroupItem ====================

    /**
     * Simple model for the Student Groups table.
     */
    public static class StudentGroupItem {
        private final int id;
        private final String groupName;
        private final String username;

        public StudentGroupItem(int id, String groupName, String username) {
            this.id = id;
            this.groupName = groupName;
            this.username = username;
        }

        public int getId() {
            return id;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getUsername() {
            return username;
        }
    }
}
