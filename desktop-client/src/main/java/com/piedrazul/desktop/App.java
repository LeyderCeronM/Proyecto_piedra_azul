package com.piedrazul.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piedrazul/desktop/LoginView.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Piedrazul — Desktop Client");
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// 👑 EL TRUCO: Agrega esta clase secundaria aquí abajo, FUERA de la clase App
class MainLauncher {
    public static void main(String[] args) {
        App.main(args);
    }
}