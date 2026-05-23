package ui.controllers_for_ui;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import chemlab_system.database.Connector_ChemSystem;
import chemlab_system.model.BorrowRequest;

/**
 * FXML Controller class for Instructor Dashboard.
 * Feature 4: Instructors can monitor their students' history and pending
 * requests.
 * Instructors see a read-only view filtered by department.
 */
public class InstructorDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private TabPane tabPane;

    // Student Groups Table
    @FXML
    private TableView<StudentGroupRow> studentGroupsTable;
    @FXML
    private TableColumn<StudentGroupRow, Integer> colSgId;
    @FXML
    private TableColumn<StudentGroupRow, String> colSgName;
    @FXML
    private TableColumn<StudentGroupRow, String> colSgUsername;
    @FXML
    private TableColumn<StudentGroupRow, String> colSgDepartment;
    @FXML
    private TextField studentSearchField;

    // Pending Requests Table
    @FXML
    private TableView<BorrowRequest> pendingTable;
    @FXML
    private TableColumn<BorrowRequest, Integer> colPendId;
    @FXML
    private TableColumn<BorrowRequest, String> colPendGroup;
    @FXML
    private TableColumn<BorrowRequest, String> colPendApparatus;
    @FXML
    private TableColumn<BorrowRequest, Integer> colPendQty;
    @FXML
    private TableColumn<BorrowRequest, String> colPendStatus;
    @FXML
    private TableColumn<BorrowRequest, String> colPendDate;
    @FXML
    private TextField pendingSearchField;

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
    private TableColumn<BorrowRequest, String> colHistStatus;
    @FXML
    private TableColumn<BorrowRequest, String> colHistDate;
    @FXML
    private TextField historySearchField;

    private ObservableList<StudentGroupRow> studentGroupsMaster = FXCollections.observableArrayList();
    private ObservableList<BorrowRequest> pendingMaster = FXCollections.observableArrayList();
    private ObservableList<BorrowRequest> historyMaster = FXCollections.observableArrayList();

    private int instructorId;
    private String instructorName;

    public void initData(int instructorId, String name) {
        this.instructorId = instructorId;
        this.instructorName = name;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + name + " (Instructor)");
        }
        loadStudentGroups();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Student Groups columns
        colSgId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colSgName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colSgUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colSgDepartment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));

        // Pending columns
        colPendId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colPendGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colPendApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colPendQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colPendStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colPendDate
                .setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getCreatedAt())));

        // History columns
        colHistId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRequestId()).asObject());
        colHistGroup.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        colHistApparatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApparatusName()));
        colHistQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQty()).asObject());
        colHistStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colHistDate
                .setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getCreatedAt())));

        // Column resize policy
        studentGroupsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Tab switch handler
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                switch (newTab.getText()) {
                    case "My Student Groups":
                        loadStudentGroups();
                        break;
                    case "Pending Requests":
                        loadPendingRequests();
                        break;
                    case "History":
                        loadHistory();
                        break;
                }
            }
        });
    }

    // ==================== DATA LOADING ====================

    private void loadStudentGroups() {
        ObservableList<StudentGroupRow> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        // Load all student groups (optionally filtered by department if instructor is
        // linked)
        String sql = "SELECT sg.group_id, sg.group_name, sg.username, " +
                "COALESCE(d.department_name, 'Unassigned') AS dept_name " +
                "FROM student_groups sg " +
                "LEFT JOIN departments d ON sg.department_id = d.department_id " +
                "ORDER BY sg.group_name";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new StudentGroupRow(
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getString("username"),
                        rs.getString("dept_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        studentGroupsMaster = list;
        studentGroupsTable.setItems(list);
    }

    private void loadPendingRequests() {
        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql = "SELECT r.request_id, g.group_name, a.item_name, r.qty, r.status, r.created_at " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status IN ('Pending', 'Approved') " +
                "ORDER BY r.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId(rs.getInt("request_id"));
                req.setGroupName(rs.getString("group_name") != null ? rs.getString("group_name") : "Unknown");
                req.setApparatusName(rs.getString("item_name") != null ? rs.getString("item_name") : "Unknown");
                req.setQty(rs.getInt("qty"));
                req.setStatus(rs.getString("status"));
                req.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pendingMaster = list;
        pendingTable.setItems(list);
    }

    private void loadHistory() {
        ObservableList<BorrowRequest> list = FXCollections.observableArrayList();
        Connection conn = Connector_ChemSystem.getConnection();
        if (conn == null)
            return;

        String sql = "SELECT r.request_id, g.group_name, a.item_name, r.qty, r.status, r.created_at " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "ORDER BY r.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId(rs.getInt("request_id"));
                req.setGroupName(rs.getString("group_name") != null ? rs.getString("group_name") : "Unknown");
                req.setApparatusName(rs.getString("item_name") != null ? rs.getString("item_name") : "Unknown");
                req.setQty(rs.getInt("qty"));
                req.setStatus(rs.getString("status"));
                req.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        historyMaster = list;
        historyTable.setItems(list);
    }

    // ==================== ACTIONS ====================

    @FXML
    private void refreshStudentGroups(ActionEvent event) {
        loadStudentGroups();
    }

    @FXML
    private void refreshPending(ActionEvent event) {
        loadPendingRequests();
    }

    @FXML
    private void refreshHistory(ActionEvent event) {
        loadHistory();
    }

    @FXML
    private void filterStudents(KeyEvent event) {
        String keyword = studentSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            studentGroupsTable.setItems(studentGroupsMaster);
            return;
        }
        String lc = keyword.toLowerCase();
        studentGroupsTable.setItems(studentGroupsMaster.filtered(g -> g.getGroupName().toLowerCase().contains(lc) ||
                g.getUsername().toLowerCase().contains(lc) ||
                g.getDepartment().toLowerCase().contains(lc)));
    }

    @FXML
    private void filterPending(KeyEvent event) {
        String keyword = pendingSearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            pendingTable.setItems(pendingMaster);
            return;
        }
        String lc = keyword.toLowerCase();
        pendingTable.setItems(pendingMaster.filtered(r -> String.valueOf(r.getRequestId()).contains(lc) ||
                r.getGroupName().toLowerCase().contains(lc) ||
                r.getApparatusName().toLowerCase().contains(lc)));
    }

    @FXML
    private void filterHistory(KeyEvent event) {
        String keyword = historySearchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            historyTable.setItems(historyMaster);
            return;
        }
        String lc = keyword.toLowerCase();
        historyTable.setItems(historyMaster.filtered(r -> String.valueOf(r.getRequestId()).contains(lc) ||
                r.getGroupName().toLowerCase().contains(lc) ||
                r.getApparatusName().toLowerCase().contains(lc)));
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
        }
    }

    // ==================== INNER CLASS ====================

    public static class StudentGroupRow {
        private final int id;
        private final String groupName;
        private final String username;
        private final String department;

        public StudentGroupRow(int id, String groupName, String username, String department) {
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

    private String formatTimestamp(Timestamp ts) {
        if (ts == null)
            return "";
        return new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a").format(ts);
    }
}
