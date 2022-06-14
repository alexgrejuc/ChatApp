package com.alexgrejuc.chatclient;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Button button_send;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane sp_main;

    private Client client;

    /**
     * Reads the message from the text field and clears it.
     * @return
     */
    private String takeMessage() {
        String message = tf_message.getText();
        tf_message.clear();
        return message;
    }

    /**
     * Creates a blue right-aligned box for displaying a message sent by the client.
     * @param message
     * @return
     */
    private static HBox createSentMessageBox(String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-color: rgb(239, 242, 255);" +
                " -fx-background-color: rgb(15, 125, 242);" +
                " -fx-background-radius: 20px;");

        textFlow.setPadding((new Insets(5, 10, 5, 10)));
        text.setFill(Color.color(0.934, 0.945, 0.996));
        hBox.getChildren().add(textFlow);
        return hBox;
    }

    /**
     * Creates a gray left-aligned box for displaying a message that the client received.
     * @param message
     * @return
     */
    private static HBox createReceivedMessageBox(String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" +
                " -fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        hBox.getChildren().add(textFlow);
        return hBox;
    }

    /**
     * Initializes the event handlers for signing in, sending, receiving, and updating the display.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tf_message.setText("Enter a username:");

        // Scroll to bottom when messages are sent.
        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        // A one-time event that clears the username prompt text.
        tf_message.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                tf_message.clear();
                tf_message.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
            }
        });

        // Sets a one-time handler for getting a username and a recurring handler for sending messages.
        button_send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String username = takeMessage();

                // Try to connect to the server with the given username.
                try {
                    Socket socket = new Socket("localhost", 7777);
                    client = new Client(socket, username);
                    client.logIn();
                } catch (IOException ioe) {
                    System.out.println("Error connecting to the server. Perhaps it is offline or your configuration is incorrect.");
                }

                // This is a one-time action for getting a username. The handler has fired, so remove it.
                button_send.removeEventHandler(ActionEvent.ANY, this);

                client.listenForMessages(vbox_messages);

                // Set the message sending event handler.
                button_send.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String message = takeMessage();

                        if (!message.isEmpty()) {
                            HBox messageBox = createSentMessageBox(message);
                            vbox_messages.getChildren().add(messageBox);
                            client.sendMessage(message);
                        }
                    }
                });
            }
        });
    }

    /**
     * Attaches a received message to the bottom of the message display.
     * @param message
     * @param vbox
     */
    public static void attachReceivedMessage(String message, VBox vbox) {
        HBox messageBox = createReceivedMessageBox(message);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vbox.getChildren().add(messageBox);
            }
        });
    }

    /**
     * Logs the client out if it has been connected to the server.
     */
    public void logOut() {
        if (client != null) {
            client.logOut();
        }
    }

    public EventHandler<WindowEvent> getOnCloseHandler() {
        return windowEvent -> logOut();
    }
}
