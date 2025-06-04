package app;

import dbTableMapper.Activity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import app.service.ActivityService;

@Component
public class ActivitiesTableController {

	// Controller FXML show tabelle
	
	@FXML private TableView<Activity> table;
    @FXML private TableColumn<Activity, Long> colId;
    @FXML private TableColumn<Activity, String> colNome;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colInizio;
    @FXML private TableColumn<Activity, String> colFinePrev;
    @FXML private TableColumn<Activity, String> colFine;
    @FXML private TableColumn<Activity, Double> colOre;
    @FXML private TableColumn<Activity, Boolean> colDone;
    @FXML private TableColumn<Activity, String> colPriority;
    @FXML private TableColumn<Activity, Void> colClose;
    @FXML private TableColumn<Activity, Void> colActions;
    @FXML private TableColumn<Activity, Void> colDelete;
    @FXML private Button export;
    
    private ObservableList<Activity> data;

    @Autowired
    private ActivityController activityController;
    
    @Autowired
    private ActivityService service;
    
    @Getter
    private String mode; // ALL o ACTIVE
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @Value("${export.excel.pathAll}")
    private String pathAll;

    @Value("${export.excel.pathActive}")
    private String pathActive;

    
    @FXML
    private void initialize() {
        export.setOnAction(e -> setupExportButton());
    }

    public void loadData(String type) {
    	this.mode=type;

        List<Activity> tutte = service.getAllActivities();

        if ("active".equals(mode)) {
            data = FXCollections.observableArrayList(tutte.stream().filter(a -> !a.isDone()).toList());
            
        } else {
            data = FXCollections.observableArrayList(tutte);
        }
        
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleLongProperty(cell.getValue().getId()).asObject());
        colNome.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNome()));
        colDescrizione.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescrizione()));
        colInizio.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getInizio().format(formatter)));
        colFinePrev.setCellValueFactory(cell -> {
            return new SimpleStringProperty(
                cell.getValue().getFine_prev() != null ? cell.getValue().getFine_prev().toString() : ""
            );
        });
        if (colFine != null) {
            colFine.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getFine() != null ? cell.getValue().getFine().format(formatter) : ""
            ));
        }
        if (colOre != null) {
            colOre.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getOre()).asObject());
        }
        if (colDone != null) {
            colDone.setCellValueFactory(cell -> new javafx.beans.property.SimpleBooleanProperty(cell.getValue().isDone()).asObject());
        }
        colPriority.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPriority()));
        
        setupCloseButtons();
        setupActionButtons();
        setupDeleteButtons();

        table.setItems(data);
    }

    private void setupActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Modifica");

            {
                btn.setOnAction(e -> {
                    Activity activity = getTableView().getItems().get(getIndex());
                    activityController.openFormWindow("update", activity);
                    loadData(mode);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }
    
    private void setupDeleteButtons() {
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Elimina");

            {
                btn.setOnAction(e -> {
                    Activity activity = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(activity);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }
    
    private void showDeleteConfirmation(Activity activity) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Sei sicuro di voler eliminare l'attività?");
        alert.setContentText("Attività: " + activity.getNome());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	service.deleteActivity(activity.getId());
            	loadData(mode); // Ricarica la tabella
            }
        });
    }
    
    private void setupCloseButtons() {
        colClose.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Chiudi");
            {
                btn.setOnAction(e -> {
                	Activity activity = getTableView().getItems().get(getIndex());
                    service.closeAsctivity(activity.getId());
                    loadData(mode);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                	Activity activity = getTableView().getItems().get(getIndex());
                	btn.setDisable(activity.isDone());
                    setGraphic(btn);
                }
            }
        });
    }
    
    public boolean isTableReady() {
    	return colId != null && colNome != null;
    }
    
    @Deprecated
    public void setupExportButtonOld() {
    	export.setOnAction(e -> {
    		FileChooser fileChooser = new FileChooser();
    		fileChooser.setTitle("Esporta dati in Excel");
    		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx")); // Apertura finestra salvataggio file
    		File file = fileChooser.showSaveDialog(export.getScene().getWindow());
    		if (file != null) {
                try {
                    List<Activity> activities = table.getItems();
                    service.exportToExcel(activities, file); // delega al service
                    System.out.println("Esportazione completata.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
    		
    	});
    	
    }
    
    public void setupExportButton() {
        export.setOnAction(e -> {
            try {
                List<Activity> activities = table.getItems();
                File file;
                if (mode.equals("active")) {
                	file = new File(pathActive);
                	service.exportToExcel(activities, file); // delega al service
                } else {
                	file = new File(pathAll);
                	service.exportToExcel(activities, file, true);
                }

                service.exportToExcel(activities, file); // delega al service
                System.out.println("Esportazione completata in: " + file.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
