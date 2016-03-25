package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View/Main.fxml"));
        primaryStage.setTitle("YOTA");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/image/taskbar.png")));
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        //File configFile = new File("config.properties");
        //InputStream inputStream = new FileInputStream(configFile);
        //Properties props = new Properties();

        //props.load(inputStream);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
