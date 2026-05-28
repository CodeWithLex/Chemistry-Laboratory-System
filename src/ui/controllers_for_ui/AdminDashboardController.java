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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.SVGPath;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.model.BorrowRequest;
import chemlab_system.util.EmailService;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.SVGPath;

/**
 * FXML Controller class for Admin Dashboard
 *
 * @author User
 */
public class AdminDashboardController implements Initializable {

    // Header
    @FXML
    private Button logoutBtn;

    @FXML
    private HBox scanActivityBadge;
    @FXML
    private Label scanActivityLabel;

    private java.sql.Timestamp lastScanTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

    // Stats
    @FXML
    private Label headerTitleLabel;

    // Sidebar Navigation Buttons
    @FXML
    private Button sideNavDashboard;
    @FXML
    private Button sideNavPending;
    @FXML
    private Button sideNavClaim;
    @FXML
    private Button sideNavInUse;
    @FXML
    private Button sideNavHistory;
    @FXML
    private Button sideNavInventory;
    @FXML
    private Button sideNavGroups;
    @FXML
    private Button sideNavSpecial;
    @FXML
    private Button sideNavAnalytics;

    private List<Button> navButtons;

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
    private TableColumn<BorrowRequest, Boolean> colSelect;
    @FXML
    private TableColumn<BorrowRequest, Integer> colRequestId;
    @FXML
    private TableColumn<BorrowRequest, String> colGroupName;
    @FXML
    private TableColumn<BorrowRequest, String> colLabActivity;
    @FXML
    private TableColumn<BorrowRequest, String> colApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colQty;
    @FXML
    private TableColumn<BorrowRequest, String> colStatus;
    @FXML
    private TableColumn<BorrowRequest, String> colDate;

    // Batch action controls
    @FXML
    private CheckBox selectAllCheckBox;
    @FXML
    private Button batchApproveBtn;
    @FXML
    private Button batchRejectBtn;

    // All Requests Table
    @FXML
    private TableView<BorrowRequest> allRequestsTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colAllId;
    @FXML
    private TableColumn<BorrowRequest, String> colAllGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colAllLabActivity;
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

    // Analytics Components
    @FXML
    private Label mostRequestedLabel;
    @FXML
    private Label leastRequestedLabel;
    @FXML
    private Label peakActivityLabel;
    @FXML
    private BarChart<String, Number> apparatusUsageChart;
    @FXML
    private PieChart stockPieChart;
    @FXML
    private LineChart<String, Number> activityLineChart;
    @FXML
    private DatePicker analyticsStartDatePicker;
    @FXML
    private DatePicker analyticsEndDatePicker;
    @FXML
    private Label lastUpdatedLabel;

    // Student Groups Table
    @FXML
    private TableView<StudentGroupItem> groupsTable;
    @FXML
    private TableColumn<StudentGroupItem, Integer> colGroupId;
    @FXML
    private TableColumn<StudentGroupItem, String> colGrpName;
    @FXML
    private TableColumn<StudentGroupItem, String> colGrpUsername;
    @FXML
    private TableColumn<StudentGroupItem, String> colGrpDepartment;

    // Currently In Use Table
    @FXML
    private TableView<BorrowRequest> inUseTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colInUseId;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseLabActivity;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colInUseQty;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseDate;
    @FXML
    private TableColumn<BorrowRequest, String> colInUseCollected;
    @FXML
    private TableColumn<BorrowRequest, Integer> colInUseRemaining;
    @FXML
    private TableColumn<BorrowRequest, Boolean> colInUseSelect;
    @FXML
    private CheckBox selectAllInUseCheckBox;

    // Claiming Table
    @FXML
    private TableView<BorrowRequest> claimingTable;
    @FXML
    private TableColumn<BorrowRequest, Boolean> colClaimSelect;
    @FXML
    private TableColumn<BorrowRequest, Integer> colClaimId;
    @FXML
    private TableColumn<BorrowRequest, String> colClaimGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colClaimLabActivity;
    @FXML
    private TableColumn<BorrowRequest, String> colClaimApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colClaimQty;
    @FXML
    private TableColumn<BorrowRequest, String> colClaimDate;
    @FXML
    private TextField claimingSearchField;
    @FXML
    private Button confirmReceiptBtn;

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

    // Apparatus Requests Table (Feature 3 admin side)
    @FXML
    private TableView<ApparatusRequestItem> apparatusRequestsTable;
    @FXML
    private TableColumn<ApparatusRequestItem, Integer> colArId;
    @FXML
    private TableColumn<ApparatusRequestItem, String> colArGroup;
    @FXML
    private TableColumn<ApparatusRequestItem, String> colArLabActivity;
    @FXML
    private TableColumn<ApparatusRequestItem, String> colArApparatus;
    @FXML
    private TableColumn<ApparatusRequestItem, String> colArReason;
    @FXML
    private TableColumn<ApparatusRequestItem, String> colArStatus;
    @FXML
    private TableColumn<ApparatusRequestItem, String> colArDate;
    private ObservableList<ApparatusRequestItem> apparatusRequestsMaster = FXCollections.observableArrayList();

    // History Table
    @FXML
    private TableView<BorrowRequest> historyTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colHistId;
    @FXML
    private TableColumn<BorrowRequest, String> colHistGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colHistLabActivity;
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
    private Timeline autoRefreshTimeline;
    private Timeline heartbeatTimeline;

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
        // Initialize Navigation Buttons List
        navButtons = java.util.Arrays.asList(
                sideNavDashboard, sideNavPending, sideNavClaim, sideNavInUse,
                sideNavHistory, sideNavInventory, sideNavGroups, sideNavSpecial,
                sideNavAnalytics);

        // Setup Pending Requests Table columns
        // Checkbox column for batch selection
        if (colSelect != null) {
            colSelect.setCellValueFactory(data -> data.getValue().selectedProperty());
            colSelect.setCellFactory(col -> new TableCell<BorrowRequest, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        BorrowRequest req = getTableView().getItems().get(getIndex());
                        checkBox.setSelected(req.isSelected());
                        checkBox.setOnAction(e -> req.setSelected(checkBox.isSelected()));
                        setGraphic(checkBox);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            });
        }

        colRequestId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colGroupName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colLabActivity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabActivity()));
        colApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getCreatedAt())));

        // Setup All Requests Table columns
        colAllId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colAllGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colAllLabActivity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabActivity()));
        colAllApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colAllQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colAllStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colAllDate
                .setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getCreatedAt())));

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
        if (colGrpDepartment != null) {
            colGrpDepartment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));
        }

        // Setup Claiming Table columns
        colClaimSelect.setCellValueFactory(new PropertyValueFactory<>("selected"));
        colClaimSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colClaimSelect));
        colClaimSelect.setEditable(true);
        claimingTable.setEditable(true);

        colClaimId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colClaimGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colClaimLabActivity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabActivity()));
        colClaimApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colClaimQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colClaimDate.setCellValueFactory(
                data -> new SimpleStringProperty(formatTimestamp(data.getValue().getApprovedAt())));

        colClaimId.setMinWidth(60);
        colClaimGroup.setMinWidth(130);
        colClaimLabActivity.setMinWidth(150);
        colClaimApparatus.setMinWidth(150);
        colClaimQty.setMinWidth(50);
        colClaimDate.setMinWidth(170);

        applyTextCellStyle(colClaimId, colClaimGroup, colClaimLabActivity, colClaimApparatus, colClaimQty,
                colClaimDate);
        disableReorderAndSort(colClaimSelect, colClaimId, colClaimGroup, colClaimLabActivity, colClaimApparatus,
                colClaimQty, colClaimDate);
        claimingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        if (colInUseSelect != null) {
            colInUseSelect.setCellValueFactory(new PropertyValueFactory<>("selected"));
            colInUseSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colInUseSelect));
            colInUseSelect.setEditable(true);
            inUseTable.setEditable(true);
        }

        colInUseId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colInUseGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colInUseLabActivity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabActivity()));
        colInUseApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colInUseQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        if (colInUseDate != null) {
            colInUseDate.setCellValueFactory(
                    data -> new SimpleStringProperty(formatTimestamp(data.getValue().getUpdatedAt())));
        }
        if (colInUseCollected != null) {
            colInUseCollected.setCellValueFactory(
                    data -> new SimpleStringProperty(formatTimestamp(data.getValue().getUpdatedAt())));
        }

        if (colInUseRemaining != null) {
            colInUseRemaining.setCellValueFactory(
                    data -> new SimpleIntegerProperty(data.getValue().getApparatusRemaining()).asObject());
        }

        // Setup History Table columns
        if (colHistId != null) {
            colHistId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
            colHistGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
            colHistLabActivity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabActivity()));
            colHistApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
            colHistQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
            colHistBorrowed.setCellValueFactory(
                    data -> new SimpleStringProperty(formatTimestamp(data.getValue().getCreatedAt())));
            colHistReturned.setCellValueFactory(
                    data -> new SimpleStringProperty(formatTimestamp(data.getValue().getUpdatedAt())));

            // Duration: from request creation to return time
            colHistDuration.setCellValueFactory(data -> {
                Timestamp start = data.getValue().getCreatedAt();
                return new SimpleStringProperty(
                        calculateDurationText(start, data.getValue().getUpdatedAt()));
            });
        }

        // Apply cell style to ensure text is visible (fix for CSS inheritance issues)
        applyTextCellStyle(colRequestId, colGroupName, colLabActivity, colApparatus, colQty, colStatus, colDate);
        applyTextCellStyle(colAllId, colAllGroup, colAllLabActivity, colAllApparatus, colAllQty, colAllStatus,
                colAllDate);
        if (colAppRemaining != null) {
            applyTextCellStyle(colAppId, colAppName, colAppQty, colAppRemaining);
        } else {
            applyTextCellStyle(colAppId, colAppName, colAppQty);
        }
        if (colGrpDepartment != null) {
            applyTextCellStyle(colGroupId, colGrpName, colGrpUsername, colGrpDepartment);
        } else {
            applyTextCellStyle(colGroupId, colGrpName, colGrpUsername);
        }
        applyTextCellStyle(colInUseId, colInUseGroup, colInUseLabActivity, colInUseApparatus, colInUseQty,
                colInUseCollected,
                colInUseRemaining);

        if (colHistId != null) {
            applyTextCellStyle(colHistId, colHistGroup, colHistLabActivity, colHistApparatus, colHistQty,
                    colHistBorrowed, colHistReturned,
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
        disableReorderAndSort(colRequestId, colGroupName, colLabActivity, colApparatus, colQty, colStatus, colDate);
        disableReorderAndSort(colAllId, colAllGroup, colAllLabActivity, colAllApparatus, colAllQty, colAllStatus,
                colAllDate);
        if (colAppRemaining != null) {
            disableReorderAndSort(colAppId, colAppName, colAppQty, colAppRemaining);
        } else {
            disableReorderAndSort(colAppId, colAppName, colAppQty);
        }
        if (colGrpDepartment != null) {
            disableReorderAndSort(colGroupId, colGrpName, colGrpUsername, colGrpDepartment);
        } else {
            disableReorderAndSort(colGroupId, colGrpName, colGrpUsername);
        }
        disableReorderAndSort(colInUseId, colInUseGroup, colInUseLabActivity, colInUseApparatus, colInUseQty,
                colInUseCollected,
                colInUseRemaining);

        if (colHistId != null) {
            disableReorderAndSort(colHistId, colHistGroup, colHistLabActivity, colHistApparatus, colHistQty,
                    colHistBorrowed,
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
        if (colGrpDepartment != null) {
            colGrpDepartment.setMinWidth(150);
        }

        colInUseId.setMinWidth(60);
        colInUseGroup.setMinWidth(130);
        colInUseApparatus.setMinWidth(150);
        colInUseQty.setMinWidth(50);
        if (colInUseCollected != null) {
            colInUseCollected.setMinWidth(170);
        }

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

        // Setup Apparatus Requests Table columns
        if (apparatusRequestsTable != null && colArId != null) {
            colArId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
            colArGroup.setCellValueFactory(new PropertyValueFactory<>("groupName"));
            colArLabActivity.setCellValueFactory(new PropertyValueFactory<>("labActivity"));
            colArApparatus.setCellValueFactory(new PropertyValueFactory<>("apparatusName"));
            colArReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
            colArStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colArDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

            apparatusRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            applyTextCellStyle(colArId, colArGroup, colArLabActivity, colArApparatus, colArReason, colArStatus,
                    colArDate);
            disableReorderAndSort(colArId, colArGroup, colArLabActivity, colArApparatus, colArReason, colArStatus,
                    colArDate);
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
                    case "Apparatus Requests":
                        loadApparatusRequests();
                        break;
                }
                loadStats();
            }
        });

        // Initialize Data
        loadPendingRequests();

        // Start Auto-Activity Heartbeat (Keep Supabase Active)
        startHeartbeat();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(8), event -> {
            loadStats();
            if (tabPane == null || tabPane.getSelectionModel().getSelectedItem() == null) {
                return;
            }

            String selectedTab = tabPane.getSelectionModel().getSelectedItem().getText();
            if ("Pending Requests".equals(selectedTab)) {
                loadPendingRequests(
                        pendingStartDatePicker != null ? pendingStartDatePicker.getValue() : null,
                        pendingEndDatePicker != null ? pendingEndDatePicker.getValue() : null);
            } else if ("All Requests".equals(selectedTab)) {
                loadAllRequests(
                        allStartDatePicker != null ? allStartDatePicker.getValue() : null,
                        allEndDatePicker != null ? allEndDatePicker.getValue() : null);
            } else if ("Apparatus Requests".equals(selectedTab)) {
                loadApparatusRequests();
            } else if ("To Be Claimed".equals(selectedTab)) {
                loadClaimingRequests(
                        claimingSearchField != null ? claimingSearchField.getText() : "");
            } else if ("Currently In Use".equals(selectedTab)) {
                loadCurrentlyInUse(
                        inUseStartDatePicker != null ? inUseStartDatePicker.getValue() : null,
                        inUseEndDatePicker != null ? inUseEndDatePicker.getValue() : null);
            }

            // Phase 2: Check for new scans from Web Companion
            checkForNewScans();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void checkForNewScans() {
        new Thread(() -> {
            try (Connection conn = Connector_ChemSystem.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT session_id, updated_at, " +
                                    "(SELECT g.group_name FROM requests r2 JOIN student_groups g ON r2.group_id = g.group_id WHERE r2.session_id = requests.session_id LIMIT 1) as group_name "
                                    +
                                    "FROM requests WHERE status = 'Returned' AND updated_at > ? " +
                                    "ORDER BY updated_at DESC LIMIT 1")) {

                ps.setTimestamp(1, lastScanTimestamp);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String groupName = rs.getString("group_name");
                        lastScanTimestamp = rs.getTimestamp("updated_at");

                        javafx.application.Platform.runLater(() -> showScanNotification(groupName));
                    }
                }
            } catch (SQLException ex) {
                // Silently fail for background polling to avoid interrupting admin
            }
        }).start();
    }

    private void showScanNotification(String groupName) {
        if (scanActivityBadge == null || scanActivityLabel == null)
            return;

        scanActivityLabel.setText("Group Processed: " + groupName);
        scanActivityBadge.setVisible(true);

        // Hide after 6 seconds
        Timeline hideTimeline = new Timeline(new KeyFrame(Duration.seconds(6), e -> {
            scanActivityBadge.setVisible(false);
        }));
        hideTimeline.play();
    }

    /**
     * Disables sorting on the given columns.
     */
    @SafeVarargs
    private final void disableReorderAndSort(TableColumn<?, ?>... columns) {
        for (TableColumn<?, ?> col : columns) {
            if (col != null) {
                col.setSortable(false);
            }
        }
    }

    /**
     * Sets a custom cell factory on each column to force readable text style.
     */
    @SafeVarargs
    private final void applyTextCellStyle(TableColumn<?, ?>... columns) {
        for (TableColumn col : columns) {
            if (col == null)
                continue;
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
            // Pending count (standard + apparatus requests)
            try (java.sql.Statement st1 = conn.createStatement();
                    ResultSet rs1 = st1.executeQuery(
                            "SELECT " +
                                    "(SELECT COUNT(*) FROM requests WHERE status = 'Pending'::request_status) + " +
                                    "(SELECT COUNT(*) FROM apparatus_requests WHERE status = 'Pending'::apparatus_request_status) AS cnt")) {
                if (rs1.next())
                    pendingCountLabel.setText(String.valueOf(rs1.getInt("cnt")));
            }

            // Approved today count
            try (java.sql.Statement st2 = conn.createStatement();
                    ResultSet rs2 = st2.executeQuery(
                            "SELECT COUNT(*) AS cnt FROM requests WHERE status = 'Approved'::request_status AND DATE(updated_at) = CURRENT_DATE")) {
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
                "r.qty AS rqty, r.status AS rstatus, r.lab_activity AS lactivity, r.created_at AS rdate " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Pending'::request_status";

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
                    req.setLabActivity(rs.getString("lactivity"));
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
                "r.qty AS rqty, r.status AS rstatus, r.lab_activity AS lactivity, r.created_at AS rdate " +
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
                    req.setLabActivity(rs.getString("lactivity"));
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
                "GREATEST(a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved'::request_status,'Pending'::request_status) THEN r.qty ELSE 0 END), 0), 0) AS remaining_qty "
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

        String sql = "SELECT sg.group_id, sg.group_name, sg.username, " +
                "COALESCE(d.department_name, 'N/A') AS department_name " +
                "FROM student_groups sg " +
                "LEFT JOIN departments d ON sg.department_id = d.department_id " +
                "ORDER BY sg.group_name";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new StudentGroupItem(
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getString("username"),
                        rs.getString("department_name")));
            }
            System.out.println("loadStudentGroups: Loaded " + list.size() + " student groups");
        } catch (SQLException e) {
            System.err.println("loadStudentGroups SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        groupsMaster = list;
        applyGroupsSearchFilter();
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
    }

    @FXML
    private void refreshClaiming(ActionEvent event) {
        loadClaimingRequests();
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
        // Collect checked items first
        List<BorrowRequest> checked = new ArrayList<>();
        for (BorrowRequest req : requestsTable.getItems()) {
            if (req.isSelected()) {
                checked.add(req);
            }
        }
        // If checkboxes are used, do batch; otherwise fall back to row selection
        if (!checked.isEmpty()) {
            processBatchAction("Approved");
        } else {
            BorrowRequest selected = requestsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select or check request(s) to approve.");
                return;
            }
            updateRequestStatus(selected.getRequestId(), "Approved");
        }
    }

    @FXML
    private void rejectSelected(ActionEvent event) {
        // Collect checked items first
        List<BorrowRequest> checked = new ArrayList<>();
        for (BorrowRequest req : requestsTable.getItems()) {
            if (req.isSelected()) {
                checked.add(req);
            }
        }
        // If checkboxes are used, do batch; otherwise fall back to row selection
        if (!checked.isEmpty()) {
            processBatchAction("Rejected");
        } else {
            BorrowRequest selected = requestsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select or check request(s) to reject.");
                return;
            }
            updateRequestStatus(selected.getRequestId(), "Rejected");
        }
    }

    @FXML
    private void markReturnedSelected(ActionEvent event) {
        // Collect checked items first
        List<BorrowRequest> checked = new ArrayList<>();
        for (BorrowRequest req : inUseTable.getItems()) {
            if (req.isSelected()) {
                checked.add(req);
            }
        }

        if (!checked.isEmpty()) {
            processInUseBatchAction("Returned", checked);
        } else {
            BorrowRequest selected = inUseTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select or check request(s) to mark as returned.");
                return;
            }
            updateRequestStatus(selected.getRequestId(), "Returned");
        }
    }

    private void processInUseBatchAction(String newStatus, List<BorrowRequest> items) {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        int count = 0;
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE requests SET status = ?::request_status WHERE request_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (BorrowRequest req : items) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, req.getRequestId());
                    stmt.addBatch();
                    count++;
                }
                stmt.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);

            showAlert(AlertType.INFORMATION, "Success", count + " items marked as returned.");
            loadCurrentlyInUse();
            loadStats();
            loadApparatus(); // Update remaining counts
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Batch update failed: " + e.getMessage());
        }
    }

    private void updateRequestStatus(int requestId, String newStatus) {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql;
        if ("Approved".equalsIgnoreCase(newStatus)) {
            sql = "UPDATE requests SET status = ?::request_status, approved_at = NOW() WHERE request_id = ?";
        } else {
            sql = "UPDATE requests SET status = ?::request_status WHERE request_id = ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, requestId);
            int result = stmt.executeUpdate();
            if (result > 0) {
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
            if (autoRefreshTimeline != null) {
                autoRefreshTimeline.stop();
            }
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
                "r.qty AS rqty, r.created_at AS created_at, r.updated_at AS updated_at, r.lab_activity AS lactivity " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Returned'::request_status ";

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
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
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
                    req.setLabActivity(rs.getString("lactivity"));
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

    public void loadCurrentlyInUse() {
        loadCurrentlyInUse(null, null);
    }

    public void loadCurrentlyInUse(LocalDate startDate, LocalDate endDate) {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        String sql = "SELECT r.request_id, r.group_id, g.group_name, r.apparatus_id, a.item_name, r.qty, r.status, r.lab_activity, r.approved_at, r.updated_at, a.current_quantity "
                +
                "FROM requests r " +
                "JOIN student_groups g ON r.group_id = g.group_id " +
                "JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Released'::request_status";

        if (startDate != null && endDate != null) {
            sql += " AND DATE(r.updated_at) BETWEEN ? AND ?";
        }
        sql += " ORDER BY r.updated_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (startDate != null && endDate != null) {
                stmt.setDate(1, java.sql.Date.valueOf(startDate));
                stmt.setDate(2, java.sql.Date.valueOf(endDate));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = new BorrowRequest(
                            rs.getInt("request_id"),
                            rs.getInt("group_id"),
                            rs.getString("group_name"),
                            rs.getInt("apparatus_id"),
                            rs.getString("item_name"),
                            rs.getInt("qty"),
                            rs.getString("status"),
                            rs.getTimestamp("updated_at"));
                    req.setLabActivity(rs.getString("lab_activity"));
                    req.setApprovedAt(rs.getTimestamp("approved_at"));
                    req.setApparatusRemaining(rs.getInt("current_quantity"));
                    list.add(req);
                }
            }
            inUseTable.setItems(list);
            inUseTable.refresh();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void loadClaimingRequests() {
        loadClaimingRequests(null);
    }

    public void loadClaimingRequests(String search) {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        String sql = "SELECT r.request_id, r.group_id, g.group_name, r.apparatus_id, a.item_name, r.qty, r.status, r.lab_activity, r.approved_at, r.updated_at, g.email "
                +
                "FROM requests r " +
                "JOIN student_groups g ON r.group_id = g.group_id " +
                "JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Approved'::request_status";

        if (search != null && !search.trim().isEmpty()) {
            sql += " AND (g.group_name ILIKE ? OR r.request_id::text ILIKE ? OR r.lab_activity ILIKE ?)";
        }
        sql += " ORDER BY r.updated_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (search != null && !search.trim().isEmpty()) {
                String p = "%" + search + "%";
                stmt.setString(1, p);
                stmt.setString(2, p);
                stmt.setString(3, p);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = new BorrowRequest(
                            rs.getInt("request_id"),
                            rs.getInt("group_id"),
                            rs.getString("group_name"),
                            rs.getInt("apparatus_id"),
                            rs.getString("item_name"),
                            rs.getInt("qty"),
                            rs.getString("status"),
                            rs.getTimestamp("updated_at"));
                    req.setLabActivity(rs.getString("lab_activity"));
                    req.setApprovedAt(rs.getTimestamp("approved_at"));
                    list.add(req);
                }
            }
            claimingTable.setItems(list);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
    private void switchTab(ActionEvent event) {
        Button clickedBtn = (Button) event.getSource();
        String text = clickedBtn.getText();

        headerTitleLabel.setText(text);

        // Update active button styling
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
        }
        clickedBtn.getStyleClass().add("nav-button-active");

        // Switch TabPane selection
        if (tabPane == null)
            return;
        switch (text) {
            case "Overview":
                // Map Overview to All Requests (Index 2) for a broader visual summary
                tabPane.getSelectionModel().select(2);
                break;
            case "Pending Requests":
                tabPane.getSelectionModel().select(0);
                break;
            case "To Be Claimed":
                tabPane.getSelectionModel().select(1);
                break;
            case "Currently In Use":
                tabPane.getSelectionModel().select(3);
                break;
            case "Borrowing History":
                tabPane.getSelectionModel().select(4);
                break;
            case "Apparatus Inventory":
                tabPane.getSelectionModel().select(6);
                break;
            case "Student Groups":
                tabPane.getSelectionModel().select(5);
                break;
            case "System Analytics":
                tabPane.getSelectionModel().select(8); // Index of System Analytics tab
                loadAnalyticsData();
                break;
            case "Apparatus Requests":
                tabPane.getSelectionModel().select(7);
                break;
        }
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
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword) ||
                    (request.getLabActivity() != null
                            && request.getLabActivity().toLowerCase().contains(lowerCaseKeyword));
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
                    request.getStatus().toLowerCase().contains(lowerCaseKeyword) ||
                    (request.getLabActivity() != null
                            && request.getLabActivity().toLowerCase().contains(lowerCaseKeyword));
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
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword) ||
                    (request.getLabActivity() != null
                            && request.getLabActivity().toLowerCase().contains(lowerCaseKeyword));
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
                    group.getUsername().toLowerCase().contains(lowerCaseKeyword) ||
                    group.getDepartment().toLowerCase().contains(lowerCaseKeyword);
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
                    request.getApparatusName().toLowerCase().contains(lowerCaseKeyword) ||
                    (request.getLabActivity() != null
                            && request.getLabActivity().toLowerCase().contains(lowerCaseKeyword));
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
        private final String department;

        public StudentGroupItem(int id, String groupName, String username, String department) {
            this.id = id;
            this.groupName = groupName;
            this.username = username;
            this.department = department;
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

        public String getDepartment() {
            return department;
        }
    }

    // ==================== INNER CLASS: ApparatusRequestItem ====================

    /**
     * Simple model for the Apparatus Requests table (student requests for unlisted
     * apparatus).
     */
    public static class ApparatusRequestItem {
        private final int id;
        private final String groupName;
        private final String labActivity;
        private final String apparatusName;
        private final String reason;
        private final String status;
        private final String createdAt;

        public ApparatusRequestItem(int id, String groupName, String labActivity, String apparatusName, String reason,
                String status,
                String createdAt) {
            this.id = id;
            this.groupName = groupName;
            this.labActivity = labActivity;
            this.apparatusName = apparatusName;
            this.reason = reason;
            this.status = status;
            this.createdAt = createdAt;
        }

        public int getId() {
            return id;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getLabActivity() {
            return labActivity;
        }

        public String getApparatusName() {
            return apparatusName;
        }

        public String getReason() {
            return reason;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    // ==================== BATCH OPERATIONS ====================

    @FXML
    private void toggleSelectAll(ActionEvent event) {
        boolean selectAll = selectAllCheckBox != null && selectAllCheckBox.isSelected();
        for (BorrowRequest req : pendingRequestsMaster) {
            req.setSelected(selectAll);
        }
        requestsTable.refresh();
    }

    @FXML
    private void batchApproveSelected(ActionEvent event) {
        processBatchAction("Approved");
    }

    @FXML
    private void batchRejectSelected(ActionEvent event) {
        processBatchAction("Rejected");
    }

    private void processBatchAction(String newStatus) {
        List<BorrowRequest> selected = new ArrayList<>();
        for (BorrowRequest req : requestsTable.getItems()) {
            if (req.isSelected()) {
                selected.add(req);
            }
        }

        if (selected.isEmpty()) {
            showAlert(AlertType.WARNING, "No Selection",
                    "Please check at least one request to " + newStatus.toLowerCase() + ".");
            return;
        }

        // Build confirmation message
        StringBuilder sb = new StringBuilder();
        sb.append("You are about to ").append(newStatus.toLowerCase()).append(" the following ");
        sb.append(selected.size()).append(" request(s):\n\n");
        for (BorrowRequest req : selected) {
            sb.append("  • #").append(req.getRequestId())
                    .append(" - ").append(req.getGroupName())
                    .append(" → ").append(req.getApparatusName())
                    .append("\n");
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Batch " + newStatus);
        confirm.setHeaderText(null);
        confirm.setContentText(sb.toString());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = Connector_ChemSystem.getConnection();
            if (conn == null)
                return;

            try {
                conn.setAutoCommit(false);
                String sql = "UPDATE requests SET status = ?::request_status, updated_at = NOW() WHERE request_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (BorrowRequest req : selected) {
                        stmt.setString(1, newStatus);
                        stmt.setInt(2, req.getRequestId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                conn.commit();

                // --- Grouped Email Notifications ---
                if ("Approved".equalsIgnoreCase(newStatus)) {
                    groupAndSendEmails(selected, newStatus);
                } else if ("Rejected".equalsIgnoreCase(newStatus)) {
                    // Rejection can still be individual or grouped, but grouping is better
                    groupAndSendEmails(selected, newStatus);
                }

                loadPendingRequests();
                loadClaimingRequests();
                loadCurrentlyInUse();
                loadStats();
                showAlert(AlertType.INFORMATION, "Success", "Selected requests updated to " + newStatus);
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                }
                showAlert(AlertType.ERROR, "Database Error", "Failed to update requests: " + ex.getMessage());
            }
        }
    }

    /**
     * Groups requests by Group and Lab Activity to send consolidated emails.
     */
    private void groupAndSendEmails(List<BorrowRequest> requests, String status) {
        // Map<GroupId, Map<LabActivity, List<BorrowRequest>>>
        java.util.Map<Integer, java.util.Map<String, List<BorrowRequest>>> groups = new java.util.HashMap<>();

        for (BorrowRequest req : requests) {
            groups.computeIfAbsent(req.getGroupId(), k -> new java.util.HashMap<>())
                    .computeIfAbsent(req.getLabActivity() != null ? req.getLabActivity() : "", k -> new ArrayList<>())
                    .add(req);
        }

        // For each group, get their email and send
        for (Integer groupId : groups.keySet()) {
            String groupEmail = getGroupEmail(groupId);
            if (groupEmail == null)
                continue;

            java.util.Map<String, List<BorrowRequest>> activities = groups.get(groupId);
            for (String activity : activities.keySet()) {
                List<BorrowRequest> items = activities.get(activity);
                String gname = items.get(0).getGroupName();

                java.util.List<EmailService.ApparatusInfo> emailItems = new ArrayList<>();
                for (BorrowRequest item : items) {
                    emailItems.add(new EmailService.ApparatusInfo(item.getApparatusName(), item.getQty()));
                }

                EmailService.sendBatchStatusNotification(groupEmail, gname, emailItems, activity, status);
            }
        }
    }

    private String getGroupEmail(int groupId) {
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return null;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT email FROM student_groups WHERE group_id = ?")) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("email");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @FXML
    private void batchReleasedSelected(ActionEvent event) {
        List<BorrowRequest> selected = new ArrayList<>();
        for (BorrowRequest req : claimingTable.getItems()) {
            if (req.isSelected()) {
                selected.add(req);
            }
        }

        if (selected.isEmpty()) {
            showAlert(AlertType.WARNING, "No Selection", "Please check at least one request to confirm receipt.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Receipt");
        confirm.setHeaderText("Tracking Releasing Time");
        confirm.setContentText("Confirm that students have received " + selected.size()
                + " item(s)? This will set the releasing time to now.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Connection conn = Connector_ChemSystem.getConnection();
            if (conn == null)
                return;
            try {
                conn.setAutoCommit(false);
                String sql = "UPDATE requests SET status = 'Released'::request_status, updated_at = NOW() WHERE request_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (BorrowRequest req : selected) {
                        stmt.setInt(1, req.getRequestId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                conn.commit();
                refreshClaiming(null);
                refreshInUse(null);
                loadStats();
                showAlert(AlertType.INFORMATION, "Success", "Items marked as Released. Tracking started.");
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                }
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void filterClaimingBySearch(KeyEvent event) {
        loadClaimingRequests(claimingSearchField.getText());
    }

    @FXML
    private void clearClaimingSearch(ActionEvent event) {
        claimingSearchField.clear();
        loadClaimingRequests();
    }

    // ==================== APPARATUS REQUESTS (Feature 3 admin side)
    // ====================

    private void loadApparatusRequests() {
        if (apparatusRequestsTable == null)
            return;

        ObservableList<ApparatusRequestItem> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql = "SELECT ar.ar_id, g.group_name, ar.apparatus_name, ar.lab_activity, ar.reason, ar.status, ar.created_at "
                +
                "FROM apparatus_requests ar " +
                "LEFT JOIN student_groups g ON ar.group_id = g.group_id " +
                "ORDER BY ar.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ApparatusRequestItem(
                        rs.getInt("ar_id"),
                        rs.getString("group_name") != null ? rs.getString("group_name") : "Unknown",
                        rs.getString("lab_activity") != null ? rs.getString("lab_activity") : "",
                        rs.getString("apparatus_name"),
                        rs.getString("reason") != null ? rs.getString("reason") : "",
                        rs.getString("status"),
                        formatTimestamp(rs.getTimestamp("created_at"))));
            }
        } catch (SQLException e) {
            System.err.println("loadApparatusRequests SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        apparatusRequestsMaster = list;
        apparatusRequestsTable.setItems(list);
    }

    @FXML
    private void refreshApparatusRequests(ActionEvent event) {
        loadApparatusRequests();
    }

    @FXML
    private void markApparatusRequestReviewed(ActionEvent event) {
        if (apparatusRequestsTable == null)
            return;

        ApparatusRequestItem selected = apparatusRequestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select an apparatus request to mark as reviewed.");
            return;
        }

        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql = "UPDATE apparatus_requests SET status = 'Reviewed'::apparatus_request_status WHERE ar_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selected.getId());
            if (stmt.executeUpdate() > 0) {
                showAlert(AlertType.INFORMATION, "Success",
                        "Apparatus request #" + selected.getId() + " marked as reviewed.");
                loadApparatusRequests();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to update apparatus request: " + e.getMessage());
        }
    }

    @FXML
    private void toggleSelectAllInUse(ActionEvent event) {
        boolean selected = selectAllInUseCheckBox.isSelected();
        for (BorrowRequest req : inUseTable.getItems()) {
            req.setSelected(selected);
        }
        inUseTable.refresh();
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null)
            return "";
        return new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a").format(ts);
    }

    @FXML
    private void refreshAnalytics(ActionEvent event) {
        loadAnalyticsData();
        lastUpdatedLabel
                .setText("Last updated: " + new java.text.SimpleDateFormat("hh:mm:ss a").format(new java.util.Date()));
    }

    private void loadAnalyticsData() {
        if (apparatusUsageChart == null)
            return;

        LocalDate start = analyticsStartDatePicker.getValue();
        LocalDate end = analyticsEndDatePicker.getValue();

        String dateFilter = "";
        if (start != null && end != null) {
            dateFilter = " WHERE created_at::date BETWEEN '" + start + "' AND '" + end + "' ";
        } else if (start != null) {
            dateFilter = " WHERE created_at::date >= '" + start + "' ";
        } else if (end != null) {
            dateFilter = " WHERE created_at::date <= '" + end + "' ";
        }

        // 1. Load Apparatus Usage Chart (Top 8)
        XYChart.Series<String, Number> usageSeries = new XYChart.Series<>();
        usageSeries.setName("Requests");

        String usageSql = "SELECT a.item_name, COUNT(*) as count FROM requests r " +
                "JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                dateFilter +
                " GROUP BY a.item_name ORDER BY count DESC LIMIT 8";

        try (Connection conn = Connector_ChemSystem.getConnection();
                PreparedStatement ps = conn.prepareStatement(usageSql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usageSeries.getData().add(new XYChart.Data<>(rs.getString("item_name"), rs.getInt("count")));
            }
            apparatusUsageChart.getData().clear();
            apparatusUsageChart.getData().add(usageSeries);

            if (!usageSeries.getData().isEmpty()) {
                mostRequestedLabel.setText(usageSeries.getData().get(0).getXValue());
            } else {
                mostRequestedLabel.setText("No Data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Load Stock Composition Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        String invSql = "SELECT SUM(current_quantity) as available FROM apparatus";
        String inUseSql = "SELECT SUM(qty) as in_use FROM requests WHERE status = 'Approved'";

        try (Connection conn = Connector_ChemSystem.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(invSql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    pieData.add(new PieChart.Data("Available", rs.getInt("available")));
            }
            try (PreparedStatement ps = conn.prepareStatement(inUseSql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    pieData.add(new PieChart.Data("In Use", rs.getInt("in_use")));
            }
            stockPieChart.setData(pieData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Activity Trends
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        String trendSql = "SELECT created_at::date as day, COUNT(*) as count FROM requests " +
                (dateFilter.isEmpty() ? " WHERE created_at > NOW() - INTERVAL '30 days' " : dateFilter) +
                " GROUP BY day ORDER BY day ASC";

        try (Connection conn = Connector_ChemSystem.getConnection();
                PreparedStatement ps = conn.prepareStatement(trendSql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trendSeries.getData().add(new XYChart.Data<>(rs.getString("day"), rs.getInt("count")));
            }
            activityLineChart.getData().clear();
            activityLineChart.getData().add(trendSeries);
            peakActivityLabel.setText(usageSeries.getData().isEmpty() ? "None" : "Active");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Least Requested
        String leastSql = "SELECT a.item_name, COUNT(*) as count FROM requests r " +
                "JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                dateFilter +
                " GROUP BY a.item_name ORDER BY count ASC LIMIT 1";
        try (Connection conn = Connector_ChemSystem.getConnection();
                PreparedStatement ps = conn.prepareStatement(leastSql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                leastRequestedLabel.setText(rs.getString("item_name"));
            else
                leastRequestedLabel.setText("No Data");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startHeartbeat() {
        heartbeatTimeline = new Timeline(new KeyFrame(Duration.minutes(10), e -> {
            new Thread(() -> {
                try (Connection conn = Connector_ChemSystem.getConnection();
                        PreparedStatement ps = conn.prepareStatement("SELECT record_heartbeat()")) {
                    ps.executeQuery();
                    System.out.println("Auto-Activity: Heartbeat recorded.");
                } catch (SQLException ex) {
                    System.err.println("Auto-Activity Error: " + ex.getMessage());
                }
            }).start();
        }));
        heartbeatTimeline.setCycleCount(Timeline.INDEFINITE);
        heartbeatTimeline.play();

        // Initial heartbeat
        refreshAnalytics(null);
    }
}
