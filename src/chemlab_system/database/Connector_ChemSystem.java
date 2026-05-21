package chemlab_system.database;

import chemlab_system.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton-style connector for the ChemLab System using Supabase (PostgreSQL).
 * Reuses a single connection across the application session.
 *
 * Credentials are loaded from config.properties next to the JAR (dist/).
 * See config.properties.template for the required format.
 * Never hardcode credentials in this file.
 */
public class Connector_ChemSystem {

    private static Connection AppConnection = null;

    /**
     * Returns a shared (singleton) PostgreSQL connection.
     * Creates a new connection the first time it is called, or after the previous
     * one was closed.
     */
    public static Connection getConnection() {
        try {
            if (AppConnection == null || AppConnection.isClosed()) {
                String host = AppConfig.getRequired("supabase.host");
                String port = AppConfig.get("supabase.port", "6543");
                String db = AppConfig.get("supabase.dbname", "postgres");
                String user = AppConfig.getRequired("supabase.user");
                String pass = AppConfig.getRequired("supabase.password");

                String url = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=require";

                Class.forName("org.postgresql.Driver");
                AppConnection = DriverManager.getConnection(url, user, pass);
                System.out.println("Connected to Supabase successfully.");
            }
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found. Ensure postgresql-42.7.4.jar is in the classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection to Supabase failed. Check credentials and internet connection.");
            e.printStackTrace();
        }
        return AppConnection;
    }
}
