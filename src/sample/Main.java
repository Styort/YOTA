package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("YotaMemoAndStatement.fxml"));
        primaryStage.setTitle("YOTA");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
