package db;

import org.apache.commons.dbcp2.*;

import java.util.Map;

public class DBCPDataSource {
    private static BasicDataSource dataSource;

    // NEVER store sensitive information below in plain text!
    static Map<String, String> env = new ProcessBuilder().environment();
    private static final String HOST_NAME = env.get("HOST_NAME");
    private static final String PORT = env.get("MYSQL_PORT");
    private static final String DATABASE = "skiers";
    private static final String USERNAME = env.get("DB_USERNAME");
    private static final String PASSWORD = env.get("DB_PASSWORD");

    public static synchronized BasicDataSource getDataSource() {
        if (dataSource == null) {
            // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
            dataSource = new BasicDataSource();
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
            dataSource.setUrl(url);
            dataSource.setUsername(USERNAME);
            dataSource.setPassword(PASSWORD);
            dataSource.setInitialSize(10);
            dataSource.setMaxTotal(60);
        }
        return dataSource;
    }
}
