package app;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DateTimeException;
import java.lang.NullPointerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dbTableMapper.Activity;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

@Component
public class FormActivityController {
	
	@Value("${spring.datasource.url}")
	private String jdbcUrl;

	@Value("${spring.datasource.username}")
	private String user;

	@Value("${spring.datasource.password}")
	private String password;
	
	@FXML private TextField uid;
	@FXML private TextField txtNome;
    @FXML private TextArea txtDescrizione;
    @FXML private TextField txtInizio;
    @FXML private TextField txtFinePrev;
    @FXML private TextField txtFine;
    @FXML private TextField txtPriorita;
    @FXML private CheckBox chkDone;
    @FXML private Label formTitle;
    @FXML private Button btnSalva;
    
    @Autowired
    private ActivitiesTableController tableController;
    
    private String mode;
    @Setter
    private Activity existingActivity;
    
    private static final String INSERTQUERY = "INSERT INTO ATTIVITA (nome, descrizione, inizio, fine_prev, fine, ore, done, priorita)"
    		+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATEQUERY = "UPDATE ATTIVITA "
    		+ "SET nome = ?, descrizione = ?, inizio = ?, fine_prev = ?, fine = ?, ore = ?, done = ?, priorita = ? "
    		+ "WHERE id = ?";
    
    
    public void init(String mode, Activity activity) {
        this.existingActivity = activity;
        setMode(mode);
    }

    
    public void setMode (String mode) {
    	this.mode = mode;
    	
    	
    	if(mode.equalsIgnoreCase("update")) {
    		formTitle.setText("Modifica attività ID = " + existingActivity.getId());
    		
    		uid.setText(String.valueOf(existingActivity.getId()));
    		uid.setDisable(true);
    		
    		txtNome.setText(existingActivity.getNome());
            txtDescrizione.setText(existingActivity.getDescrizione());
            txtInizio.setText(dateParser(existingActivity.getInizio().toString()));
            txtFinePrev.setText(existingActivity.getFine_prev() != null ? existingActivity.getFine_prev().toString() : "");
            txtFine.setText(existingActivity.getFine() != null ? dateParser(existingActivity.getFine().toString()) : "");
            txtPriorita.setText(existingActivity.getPriority());
            chkDone.setSelected(existingActivity.isDone());
    	} else {
    		formTitle.setText("Crea nuova attività");
    		uid.setDisable(true);
    	}
    	
    	btnSalva.setOnAction(e -> onSave());
    	
    }
    
    private String dateParser(String data) {
		try {
			return LocalDateTime.parse(data).toString();
		} catch (Exception e) {
			return LocalDateTime.of(LocalDate.parse(data), LocalTime.now().withSecond(0).withNano(0)).toString();
		}
	}

	private void onSave() {
    	try {
    		String nome = txtNome.getText();
            String descrizione = txtDescrizione.getText();
            LocalDateTime inizio = null;
            if (!txtInizio.getText().isBlank()) inizio = LocalDateTime.parse(dateParser(txtInizio.getText()));
            LocalDate finePrev = txtFinePrev.getText().isBlank() ? null : LocalDate.parse(txtFinePrev.getText());
            LocalDateTime fine = txtFine.getText().isBlank() ? null : LocalDateTime.parse(dateParser(txtFine.getText()));
            String priorita = txtPriorita.getText();
            boolean done = chkDone.isSelected();
            
            Activity activity = Activity.builder()
            		.nome(nome)
            		.descrizione(descrizione)
            		.inizio(inizio)
            		.fine_prev(finePrev)
            		.fine(fine)
            		.priority(priorita)
            		.done(done)
            		.build();
            
            if (mode.equalsIgnoreCase("update")) {
            	long id = existingActivity.getId();
            	updateActivity(id, activity);
            } else {
            	insertAcrivity(activity);
            }
            
            Stage stage = (Stage) btnSalva.getScene().getWindow();
            stage.close();
            if (tableController != null && tableController.isTableReady()) tableController.loadData(tableController.getMode());

// Quando creo attività la tabella può essere chiusa, cercare di aggiornarla genererebbe un'eccezione
            
//    	} catch (NullPointerException e) {
//    		System.err.println("Table not ready yet, skipping refresh.");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	private void insertAcrivity(Activity activity) {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
				PreparedStatement stmt = conn.prepareStatement(INSERTQUERY)
				) {
			if (activity.getInizio() == null) activity.setInizio(LocalDateTime.now());
			if (activity.isDone() && activity.getFine() == null) activity.setFine(LocalDateTime.now());
			activity.setOre();
			stmt.setString(1, activity.getNome());
			stmt.setString(2, activity.getDescrizione());
			stmt.setTimestamp(3, activity.getInizio() != null ? Timestamp.valueOf(activity.getInizio()) : null);
			stmt.setDate(4, activity.getFine_prev() != null ? java.sql.Date.valueOf(activity.getFine_prev()) : null);
			stmt.setTimestamp(5, activity.getFine() != null ? Timestamp.valueOf(activity.getFine()) : null);
			stmt.setDouble(6, activity.getOre());
			stmt.setBoolean(7, activity.isDone());
			stmt.setString(8, activity.getPriority());
			stmt.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	private void updateActivity(long id, Activity activity) {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
				PreparedStatement stmt = conn.prepareStatement(UPDATEQUERY)
				) {
			if (activity.getInizio() == null) activity.setInizio(LocalDateTime.now());
			if (activity.isDone() && activity.getFine() == null) activity.setFine(LocalDateTime.now());
			activity.setOre();
			stmt.setString(1, activity.getNome());
			stmt.setString(2, activity.getDescrizione());
			// Conversione LocalDateTime -> Timestamp
	        stmt.setTimestamp(3, activity.getInizio() != null ? Timestamp.valueOf(activity.getInizio()) : null);
	        // Conversione LocalDate -> Date
	        stmt.setDate(4, activity.getFine_prev() != null ? java.sql.Date.valueOf(activity.getFine_prev()) : null);
	        // Conversione LocalDateTime -> Timestamp
	        stmt.setTimestamp(5, activity.getFine() != null ? Timestamp.valueOf(activity.getFine()) : null);
			stmt.setDouble(6, activity.getOre());
			stmt.setBoolean(7, activity.isDone());
			stmt.setString(8, activity.getPriority());
			stmt.setLong(9, id);
			
			stmt.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
    

}
