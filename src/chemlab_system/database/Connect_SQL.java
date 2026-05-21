/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chemlab_system.database;

import chemlab_system.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connector using Supabase (PostgreSQL).
 * Credentials are loaded from config.properties (placed next to the JAR)
 * via AppConfig. No hardcoded credentials.
 *
 * @author User
 */
public class Connect_SQL {

    /**
     * Creates and returns a new PostgreSQL connection each time.
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            String host = AppConfig.getRequired("supabase.host");
            String port = AppConfig.get("supabase.port", "6543");
            String db = AppConfig.get("supabase.dbname", "postgres");
            String user = AppConfig.getRequired("supabase.user");
            String pass = AppConfig.getRequired("supabase.password");

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=require";

            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, pass);
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found. Ensure postgresql-42.7.4.jar is in the classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection to Supabase failed. Check credentials and internet connection.");
            e.printStackTrace();
        }
        return conn;
    }
}
