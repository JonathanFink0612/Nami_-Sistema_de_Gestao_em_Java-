package com.exemplo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carrega o FXML do caminho correto

        URL fxmlLocation = getClass().getResource("/view/login.fxml");

        Parent root = FXMLLoader.load(fxmlLocation);


        // Configura a janela
        primaryStage.setScene(new Scene(root, 650, 450));
        primaryStage.setTitle("Thunder");
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Adiciona estas configurações programaticamente
        System.setProperty("prism.verbose", "true");  // Para debug
        launch(args);
    }
}