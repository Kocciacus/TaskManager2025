package app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public class SpringFXMLLoader {
    public static Parent load(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SpringFXMLLoader.class.getResource(fxmlPath));
        loader.setControllerFactory(SpringContextHolder.getContext()::getBean);
        return loader.load();
    }
}
