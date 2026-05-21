package chemlab_system.util;

import chemlab_system.config.AppConfig;
import java.sql.*;

/**
 * Utility class for creating fresh (non-singleton) PostgreSQL connections
 * to Supabase for use in DAO classes.
 *
 * Credentials are loaded via AppConfig from config.properties next to the JAR.
 * See config.properties.template for the required format.
 */
public class DatabaseUtil {

    /**
     * Opens and returns a fresh PostgreSQL connection.
     * Callers are responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }

        String host = AppConfig.getRequired("supabase.host");
        String port = AppConfig.get("supabase.port", "6543");
        String db = AppConfig.get("supabase.dbname", "postgres");
        String user = AppConfig.getRequired("supabase.user");
        String pass = AppConfig.getRequired("supabase.password");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=require";
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Quietly closes a connection, swallowing any exception.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                /* ignore */ }
        }
    }
}
