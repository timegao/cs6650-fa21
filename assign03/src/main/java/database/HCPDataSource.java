package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

public class HCPDataSource {
    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    private static final Dotenv dotenv = Dotenv.configure().load();
    private static final String HOST_NAME = dotenv.get("HOST_NAME");
    private static final String PORT = dotenv.get("MYSQL_PORT");
    private static final String DATABASE = dotenv.get("DB_NAME");
    private static final String USERNAME = dotenv.get("DB_USERNAME");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
            config.setJdbcUrl(url);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(640);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}