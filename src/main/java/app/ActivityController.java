package app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dbTableMapper.Activity;

// CONTROLLER PAGINA PRINCIPALE

@Component
public class ActivityController {

    @FXML private Button showActive;
    @FXML private Button showAll;
    @FXML private Button create;
    @FXML private Button close;
    @FXML private Button update;
    @FXML private Button delete;
    
    @FXML private TableView<Activity> table;
    @FXML private TableColumn<Activity, Long> colId;
    @FXML private TableColumn<Activity, String> colNome;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colInizio;
    @FXML private TableColumn<Activity, String> colFinePrev;
    @FXML private TableColumn<Activity, String> colPriority;
    
    @FXML
    private void initialize() {
        showActive.setOnAction(e -> openShowWindow("active"));
        showAll.setOnAction(e -> openShowWindow("all"));
        create.setOnAction(e -> openFormWindow("create", null));
    }

    private void openShowWindow(String mode) {
        try {
            String filePath = mode.equals("active") ? "/views/active_table.fxml" : "/views/all_table.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(filePath)); // Carica GUI fxml
            loader.setControllerFactory(SpringContextHolder.getContext()::getBean);
            Parent root = loader.load();

            ActivitiesTableController showTableController = loader.getController();
            showTableController.loadData(mode);

            Stage stage = new Stage();
            stage.setTitle(mode.equals("active") ? "Attività attive" : "Tutte le attività");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore nell'apertura della finestra: " + e.getMessage());
        }
    }

    
    public void openFormWindow(String mode, Activity activity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/form_activity.fxml"));
            loader.setControllerFactory(SpringContextHolder.getContext()::getBean);
            Parent root = loader.load();

            FormActivityController controller = loader.getController();
            controller.init(mode, activity);
            
            Stage stage = new Stage();
            stage.setTitle(mode.equals("create") ? "Crea attività" : "Modifica attività");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore nell'apertura del form: " + e.getMessage());
        } 
    }

}