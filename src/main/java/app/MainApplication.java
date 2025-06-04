package app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class MainApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Avvia Spring
    	context = new SpringApplicationBuilder(MainApplication.class)
                .headless(false) // Importante per JavaFX
                .run();
        SpringContextHolder.setContext(context);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Parent root = SpringFXMLLoader.load("/views/main.fxml");
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Task Manager con Spring & JavaFX");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento dell'interfaccia principale: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        // Imposta la proprietà per JavaFX
        //System.setProperty("javafx.preloader", "");
    	System.out.println("START");
        launch(args);
    }
}
