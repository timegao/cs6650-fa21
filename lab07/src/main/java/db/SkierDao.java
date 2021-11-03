package db;

import java.sql.*;

import db.model.Skier;
import org.apache.commons.dbcp2.*;

public class SkierDao {
    private static BasicDataSource dataSource;

    public SkierDao() {
        dataSource = DBCPDataSource.getDataSource();
    }

    public void createLiftRide(Skier newSkier) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO skier (skierId, resortId, seasonId, dayId, time, liftId) " +
                "VALUES (?,?,?,?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, newSkier.getSkierId());
            preparedStatement.setInt(2, newSkier.getResortId());
            preparedStatement.setInt(3, newSkier.getSeasonId());
            preparedStatement.setInt(4, newSkier.getDayId());
            preparedStatement.setInt(5, newSkier.getTime());
            preparedStatement.setInt(6, newSkier.getLiftId());

            // execute insert SQL statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}