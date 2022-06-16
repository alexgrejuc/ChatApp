package com.alexgrejuc.chatclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Chat Client");

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("chat-client-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 478, 396);
        stage.setScene(scene);

        ClientController controller = loader.getController();
        controller.setStage(stage);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}