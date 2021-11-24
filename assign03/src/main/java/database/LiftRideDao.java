package database;

import com.zaxxer.hikari.HikariDataSource;
import model.LiftRide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LiftRideDao {
    private static HikariDataSource dataSource;

    public LiftRideDao() {
        dataSource = DBCPDataSource.getDataSource();
    }

    public void createLiftRide(LiftRide liftRide) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO LiftRides (skierId, resortId, seasonId, dayId, time, liftId) " +
                "VALUES (?,?,?,?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, liftRide.getSkierId());
            preparedStatement.setInt(2, liftRide.getResortId());
            preparedStatement.setInt(3, liftRide.getSeasonId());
            preparedStatement.setInt(4, liftRide.getDayId());
            preparedStatement.setInt(5, liftRide.getTime());
            preparedStatement.setInt(6, liftRide.getLiftId());

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