package chemlab_system.util;

import chemlab_system.model.BorrowRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    // Get all pending requests
    public static List<BorrowRequest> getPendingRequests() {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT r.request_id, g.group_name, a.item_name, r.qty, r.status, r.created_at " +
                "FROM requests r " +
                "LEFT JOIN student_groups g ON r.group_id = g.group_id " +
                "LEFT JOIN apparatus a ON r.apparatus_id = a.apparatus_id " +
                "WHERE r.status = 'Pending' ORDER BY r.created_at";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId(rs.getInt("request_id"));
                req.setGroupName(rs.getString("group_name"));
                req.setApparatusName(rs.getString("item_name"));
                req.setQty(rs.getInt("qty"));
                req.setStatus(rs.getString("status"));
                req.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Approve request
    public static boolean approveRequest(int requestId) {
        String sql = "UPDATE requests SET status = 'Approved' WHERE request_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Reject request
    public static boolean rejectRequest(int requestId) {
        String sql = "UPDATE requests SET status = 'Rejected' WHERE request_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mark as returned
    public static boolean markReturned(int requestId) {
        String sql = "UPDATE requests SET status = 'Returned' WHERE request_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
