package app.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import dbTableMapper.Activity;
import lombok.Getter;

@Repository
public class ActivityRepository {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;
    
    @Getter
    private static final String SELECTQUERY = "SELECT * FROM ATTIVITA";
    @Getter
    private static final String DELETEQUERY = "DELETE FROM ATTIVITA WHERE id = ?";
    @Getter
    private static final String CLOSEACTIVITYQUERY = "UPDATE ATTIVITA "
    		+ "SET done = 1, fine = GETDATE(), ore = DATEDIFF(MINUTE, inizio, GETDATE()) / 60.0 " 
    		+ "WHERE id = ?";
 // Si DATEDIFF(MINUTE, inizio, GETDATE()) / 60.0 invece che DATEDIFF(HOUR, inizio, GETDATE()) perché altimenti si avrebbe approssimazione all'ora intera
    
    public List<Activity> findAll() {
        List<Activity> activities = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement(SELECTQUERY);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Activity activity = mapResultSetToActivity(rs);
                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }

    private Activity mapResultSetToActivity(ResultSet rs) throws SQLException {
    	Timestamp tsFinePrev = rs.getTimestamp("fine_prev");
		var finePrev = tsFinePrev != null ? rs.getTimestamp("fine_prev").toLocalDateTime().toLocalDate() : rs.getTimestamp("inizio").toLocalDateTime().toLocalDate();
		
		Activity activity = Activity.builder()
				.id(rs.getLong("id"))
				.nome(rs.getString("nome"))
				.descrizione(rs.getString("descrizione"))
				.inizio(rs.getTimestamp("inizio").toLocalDateTime())
				.fine_prev(finePrev)
				.fine(rs.getTimestamp("fine") != null ? rs.getTimestamp("fine").toLocalDateTime() : null)
				.ore(rs.getDouble("ore"))
				.done(rs.getBoolean("done"))
				.priority(rs.getString("priorita"))
				.build();
		return activity;
    }
    
    public void deleteActivity(Long id) {
    	try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
                PreparedStatement stmt = conn.prepareStatement(DELETEQUERY)) {
               stmt.setLong(1, id);
               stmt.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
    }
    
    public void closeActivity(Long id) {
    	try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
                PreparedStatement stmt = conn.prepareStatement(CLOSEACTIVITYQUERY)) {
               stmt.setLong(1,id);
               stmt.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
    }
}
