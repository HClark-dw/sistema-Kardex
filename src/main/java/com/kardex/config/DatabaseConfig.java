package com.kardex.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor de conexión a MySQL mediante pool de conexiones HikariCP.
 * Permite configuración externa persistente y prueba interactiva de conexión.
 */
public class DatabaseConfig {

    private static HikariDataSource dataSource;
    private static Properties properties = new Properties();
    private static final String EXTERNAL_CONFIG_FILE = "database.properties";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // 1. Intentar cargar archivo externo en directorio de ejecución
        File externalFile = new File(EXTERNAL_CONFIG_FILE);
        if (externalFile.exists()) {
            try (InputStream in = new FileInputStream(externalFile)) {
                properties.load(in);
                return;
            } catch (Exception ignored) {}
        }

        // 2. Intentar cargar desde el classpath interno
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                // Valores por defecto
                properties.setProperty("db.host", "localhost");
                properties.setProperty("db.port", "3306");
                properties.setProperty("db.name", "kardex_db");
                properties.setProperty("db.user", "root");
                properties.setProperty("db.password", "");
            }
        } catch (Exception e) {
            properties.setProperty("db.host", "localhost");
            properties.setProperty("db.port", "3306");
            properties.setProperty("db.name", "kardex_db");
            properties.setProperty("db.user", "root");
            properties.setProperty("db.password", "");
        }
    }

    public static synchronized void initDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String dbName = properties.getProperty("db.name", "kardex_db");
        String user = properties.getProperty("db.user", "root");
        String pass = properties.getProperty("db.password", "");

        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            host, port, dbName
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("db.connectionTimeout", "5000")));
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setPoolName("KardexHikariPool");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public static boolean testConnection(String host, String port, String dbName, String user, String pass) {
        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=4000",
            host, port, dbName
        );
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = java.sql.DriverManager.getConnection(url, user, pass)) {
                return conn.isValid(3);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static void updateAndSaveConfig(String host, String port, String dbName, String user, String pass) throws Exception {
        properties.setProperty("db.host", host);
        properties.setProperty("db.port", port);
        properties.setProperty("db.name", dbName);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", pass);

        try (FileOutputStream out = new FileOutputStream(EXTERNAL_CONFIG_FILE)) {
            properties.store(out, "Configuracion MySQL Sistema Kardex");
        }

        // Reiniciar pool
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
        initDataSource();
    }

    public static String getProperty(String key, String def) {
        return properties.getProperty(key, def);
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
