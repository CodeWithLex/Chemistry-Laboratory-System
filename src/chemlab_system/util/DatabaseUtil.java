package chemlab_system.util;

import java.sql.*;

public class DatabaseUtil {
    
    private static final String URL = "jdbc:mysql://localhost:3306/chemlab_system";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }
}
