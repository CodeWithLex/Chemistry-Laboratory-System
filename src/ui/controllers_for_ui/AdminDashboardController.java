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
import javafx.scene.input.KeyEvent;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.model.BorrowRequest;
import chemlab_system.util.EmailService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

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
    private DatePicker pendingStartDatePicker;
    @FXML
    private DatePicker pendingEndDatePicker;

    @FXML
    private DatePicker allStartDatePicker;
    @FXML
    private DatePicker allEndDatePicker;

    @FXML
    private DatePicker inUseStartDatePicker;
    @FXML
    private DatePicker inUseEndDatePicker;

    @FXML
    private DatePicker historyStartDatePicker;
    @FXML
    private DatePicker historyEndDatePicker;

    // Search fields (per tab)
    @FXML
    private TextField pendingSearchField;
    @FXML
    private TextField allSearchField;
    @FXML
    private TextField inUseSearchField;
    @FXML
    private TextField apparatusSearchField;
    @FXML
    private TextField groupsSearchField;
    @FXML
    private TextField historySearchField;

    // Cached full lists for client-side searching
    private ObservableList<BorrowRequest> pendingRequestsMaster = FXCollections.observableArrayList();
    private ObservableList<BorrowRequest> allRequestsMaster = FXCollections.observableArrayList();
    private ObservableList<BorrowRequest> inUseMaster = FXCollections.observableArrayList();
    private ObservableList<ApparatusItem> apparatusMaster = FXCollections.observableArrayList();
    private ObservableList<StudentGroupItem> groupsMaster = FXCollections.observableArrayList();
    private ObservableList<BorrowRequest> historyMaster = FXCollections.observableArrayList();

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
            colHistDuration.setCellValueFactory(data -> new SimpleStringProperty(
                    calculateDurationText(data.getValue().getCreatedAt(), data.getValue().getUpdatedAt())));
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
            applyTextCellStyle(colHistId, colHistGroup, colHistApparatus, colHistQty, colHistBorrowed, colHistReturned,
                    colHistDuration);
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
        disableReorderAndSort(colInUseId, colInUseGroup, colInUseApparatus, colInUseQty, colInUseDate,
                colInUseRemaining);
        if (colHistId != null) {
            disableReorderAndSort(colHistId, colHistGroup, colHistApparatus, colHistQty, colHistBorrowed,
                    colHistReturned, colHistDuration);
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

        // Add listeners to search fields to apply filters dynamically
        if (pendingSearchField != null) {
            pendingSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyPendingSearchFilter());
        }
        if (allSearchField != null) {
            allSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyAllSearchFilter());
        }
        if (inUseSearchField != null) {
            inUseSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyInUseSearchFilter());
        }
        if (apparatusSearchField != null) {
            apparatusSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyApparatusSearchFilter());
        }
        if (groupsSearchField != null) {
            groupsSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyGroupsSearchFilter());
        }
        if (historySearchField != null) {
            historySearchField.textProperty().addListener((obs, oldVal, newVal) -> applyHistorySearchFilter());
        }

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
        loadPendingRequests(null, null);
    }

    private void loadPendingRequests(LocalDate startDate, LocalDate endDate) {
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

        if (startDate != null) {
            sql += " AND DATE(r.created_at) >= ?";
        }
        if (endDate != null) {
            sql += " AND DATE(r.created_at) <= ?";
        }
        sql += " ORDER BY r.created_at";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (startDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
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
        pendingRequestsMaster = list;
        applyPendingSearchFilter();
    }

    private void loadAllRequests() {
        loadAllRequests(null, null);
    }

    private void loadAllRequests(LocalDate startDate, LocalDate endDate) {
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
                "WHERE 1=1";

        if (startDate != null) {
            sql += " AND DATE(r.created_at) >= ?";
        }
        if (endDate != null) {
            sql += " AND DATE(r.created_at) <= ?";
        }
        sql += " ORDER BY r.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (startDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
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
            System.out.println("loadAllRequests: Loaded " + list.size() + " total requests");
        } catch (SQLException e) {
            System.err.println("loadAllRequests SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        allRequestsMaster = list;
        applyAllSearchFilter();
    }

    private void loadApparatus() {
        ObservableList<ApparatusItem> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null) {
            System.err.println("loadApparatus: Connection is null!");
            return;
        }

        String sql = "SELECT a.apparatus_id, a.item_name, a.current_quantity, " +
                "GREATEST(a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved','Pending') THEN r.qty ELSE 0 END), 0), 0) AS remaining_qty "
                +
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
        apparatusMaster = list;
        applyApparatusSearchFilter();
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
        groupsMaster = list;
        applyGroupsSearchFilter();
    }

    private void loadCurrentlyInUse() {
        loadCurrentlyInUse(null, null);
    }

    private void loadCurrentlyInUse(LocalDate startDate, LocalDate endDate) {
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
                "GREATEST(a.current_quantity - (" +
                "  SELECT COALESCE(SUM(r2.qty), 0) " +
                "  FROM requests r2 " +
                "  WHERE r2.apparatus_id = r.apparatus_id AND r2.status IN ('Approved', 'Pending')" +
                "), 0) AS remaining_qty " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Approved'";

        if (startDate != null) {
            sql += " AND DATE(r.updated_at) >= ?";
        }
        if (endDate != null) {
            sql += " AND DATE(r.updated_at) <= ?";
        }
        sql += " ORDER BY r.updated_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (startDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
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
        inUseMaster = list;
        applyInUseSearchFilter();
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
    private void openAddApparatusDialog(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/addApparatus.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Apparatus");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh the apparatus table after dialog closes
            loadApparatus();
            loadStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open Add Apparatus dialog: " + e.getMessage());
        }
    }

    @FXML
    private void showGroupHistorySelected(ActionEvent event) {
        StudentGroupItem selectedGroup = groupsTable.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a student group from the table first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/groupHistory.fxml"));
            Parent root = loader.load();

            GroupHistoryController controller = loader.getController();
            controller.initData(selectedGroup.getId(), selectedGroup.getGroupName());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("History for " + selectedGroup.getGroupName());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open Group History dialog: " + e.getMessage());
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

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        loadHistory(null, null);
    }

    private void loadHistory(LocalDate startDate, LocalDate endDate) {
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
                "WHERE r.status = 'Returned' ";

        if (startDate != null) {
            sql += " AND DATE(r.updated_at) >= ?";
        }
        if (endDate != null) {
            sql += " AND DATE(r.updated_at) <= ?";
        }
        sql += " ORDER BY r.updated_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (startDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
            }
            try (ResultSet rs = stmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            System.err.println("loadHistory SQL error: " + e.getMessage());
            e.printStackTrace();
        }

        historyMaster = list;
        applyHistorySearchFilter();
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
        LocalDate start = pendingStartDatePicker != null ? pendingStartDatePicker.getValue() : null;
        LocalDate end = pendingEndDatePicker != null ? pendingEndDatePicker.getValue() : null;
        loadPendingRequests(start, end);
    }

    @FXML
    private void clearPendingFilter() {
        if (pendingStartDatePicker != null)
            pendingStartDatePicker.setValue(null);
        if (pendingEndDatePicker != null)
            pendingEndDatePicker.setValue(null);
        loadPendingRequests(null, null);
    }

    @FXML
    private void filterAllByDate() {
        LocalDate start = allStartDatePicker != null ? allStartDatePicker.getValue() : null;
        LocalDate end = allEndDatePicker != null ? allEndDatePicker.getValue() : null;
        loadAllRequests(start, end);
    }

    @FXML
    private void clearAllFilter() {
        if (allStartDatePicker != null)
            allStartDatePicker.setValue(null);
        if (allEndDatePicker != null)
            allEndDatePicker.setValue(null);
        loadAllRequests(null, null);
    }

    @FXML
    private void filterInUseByDate() {
        LocalDate start = inUseStartDatePicker != null ? inUseStartDatePicker.getValue() : null;
        LocalDate end = inUseEndDatePicker != null ? inUseEndDatePicker.getValue() : null;
        loadCurrentlyInUse(start, end);
    }

    @FXML
    private void clearInUseFilter() {
        if (inUseStartDatePicker != null)
            inUseStartDatePicker.setValue(null);
        if (inUseEndDatePicker != null)
            inUseEndDatePicker.setValue(null);
        loadCurrentlyInUse(null, null);
    }

    @FXML
    private void filterHistoryByDate() {
        LocalDate start = historyStartDatePicker != null ? historyStartDatePicker.getValue() : null;
        LocalDate end = historyEndDatePicker != null ? historyEndDatePicker.getValue() : null;
        loadHistory(start, end);
    }

    @FXML
    private void clearHistoryFilter() {
        if (historyStartDatePicker != null)
            historyStartDatePicker.setValue(null);
        if (historyEndDatePicker != null)
            historyEndDatePicker.setValue(null);
        loadHistory(null, null);
    }

    // ==================== FXML EVENT HANDLERS FOR SEARCH ====================

    @FXML
    public void filterPendingBySearch(KeyEvent event) {
        applyPendingSearchFilter();
    }

    @FXML
    public void filterAllBySearch(KeyEvent event) {
        applyAllSearchFilter();
    }

    @FXML
    public void filterInUseBySearch(KeyEvent event) {
        applyInUseSearchFilter();
    }

    @FXML
    public void filterApparatusBySearch(KeyEvent event) {
        applyApparatusSearchFilter();
    }

    @FXML
    public void filterGroupsBySearch(KeyEvent event) {
        applyGroupsSearchFilter();
    }

    @FXML
    public void filterHistoryBySearch(KeyEvent event) {
        applyHistorySearchFilter();
    }

    @FXML
    public void clearPendingSearch(ActionEvent event) {
        if (pendingSearchField != null) {
            pendingSearchField.clear();
        }
        applyPendingSearchFilter();
    }

    @FXML
    public void clearAllSearch(ActionEvent event) {
        if (allSearchField != null) {
            allSearchField.clear();
        }
        applyAllSearchFilter();
    }

    @FXML
    public void clearInUseSearch(ActionEvent event) {
        if (inUseSearchField != null) {
            inUseSearchField.clear();
        }
        applyInUseSearchFilter();
    }

    @FXML
    public void clearApparatusSearch(ActionEvent event) {
        if (apparatusSearchField != null) {
            apparatusSearchField.clear();
        }
        applyApparatusSearchFilter();
    }

    @FXML
    public void clearGroupsSearch(ActionEvent event) {
        if (groupsSearchField != null) {
            groupsSearchField.clear();
        }
        applyGroupsSearchFilter();
    }

    @FXML
    public void clearHistorySearch(ActionEvent event) {
        if (historySearchField != null) {
            historySearchField.clear();
        }
        applyHistorySearchFilter();
    }

    // ==================== SEARCH/FILTER LOGIC ====================

    private void applyPendingSearchFilter() {
        String keyword = pendingSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            requestsTable.setItems(pendingRequestsMaster);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<BorrowRequest> filteredList = pendingRequestsMaster.filtered(request -> {
            return String.valueOf(request.getRequestId()).contains(lowerCaseKeyword) ||
                    request.getGroupName().toLowerCase().contains(lowerCaseKeyword) ||
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword);
        });
        requestsTable.setItems(filteredList);
    }

    private void applyAllSearchFilter() {
        String keyword = allSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            allRequestsTable.setItems(allRequestsMaster);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<BorrowRequest> filteredList = allRequestsMaster.filtered(request -> {
            return String.valueOf(request.getRequestId()).contains(lowerCaseKeyword) ||
                    request.getGroupName().toLowerCase().contains(lowerCaseKeyword) ||
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword) ||
                    request.getStatus().toLowerCase().contains(lowerCaseKeyword);
        });
        allRequestsTable.setItems(filteredList);
    }

    private void applyInUseSearchFilter() {
        String keyword = inUseSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            inUseTable.setItems(inUseMaster);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<BorrowRequest> filteredList = inUseMaster.filtered(request -> {
            return String.valueOf(request.getRequestId()).contains(lowerCaseKeyword) ||
                    request.getGroupName().toLowerCase().contains(lowerCaseKeyword) ||
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword);
        });
        inUseTable.setItems(filteredList);
    }

    private void applyApparatusSearchFilter() {
        String keyword = apparatusSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            apparatusTable.setItems(apparatusMaster);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<ApparatusItem> filteredList = apparatusMaster.filtered(item -> {
            return String.valueOf(item.getId()).contains(lowerCaseKeyword) ||
                    item.getItemName().toLowerCase().contains(lowerCaseKeyword);
        });
        apparatusTable.setItems(filteredList);
    }

    private void applyGroupsSearchFilter() {
        String keyword = groupsSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            groupsTable.setItems(groupsMaster);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<StudentGroupItem> filteredList = groupsMaster.filtered(group -> {
            return String.valueOf(group.getId()).contains(lowerCaseKeyword) ||
                    group.getGroupName().toLowerCase().contains(lowerCaseKeyword) ||
                    group.getUsername().toLowerCase().contains(lowerCaseKeyword);
        });
        groupsTable.setItems(filteredList);
    }

    private void applyHistorySearchFilter() {
        String keyword = historySearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            historyTable.setItems(historyMaster);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<BorrowRequest> filteredList = historyMaster.filtered(request -> {
            return String.valueOf(request.getRequestId()).contains(lowerCaseKeyword) ||
                    request.getGroupName().toLowerCase().contains(lowerCaseKeyword) ||
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword);
        });
        historyTable.setItems(filteredList);
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
