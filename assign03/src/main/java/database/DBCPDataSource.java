package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class DBCPDataSource {
    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    static Map<String, String> env = new ProcessBuilder().environment();
    private static final String HOST_NAME = env.get("HOST_NAME");
    private static final String PORT = env.get("MYSQL_PORT");
    private static final String DATABASE = env.get("DB_NAME");
    private static final String USERNAME = env.get("DB_USERNAME");
    private static final String PASSWORD = env.get("DB_PASSWORD");

    public static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
            config.setJdbcUrl(url);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(120);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}