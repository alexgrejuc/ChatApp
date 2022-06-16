package com.alexgrejuc.chatclient;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Button button_choose;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane sp_main;

    private Client client;

    private Stage stage;

    private FileChooser fileChooser;
    private ArrayList<File> attachments;

    /**
     * Initializes the event handlers for signing in, sending, receiving, and updating the display.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // This will be connected to button_choose when the stage is set
        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("PNG Images", "*.png");
        fileChooser.getExtensionFilters().add(filter);

        attachments = new ArrayList<>();

        tf_message.setText("Enter a username:");

        // Scroll to bottom when messages are sent.
        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        // A one-time event that clears the senderName prompt text.
        tf_message.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                tf_message.clear();
                tf_message.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
            }
        });

        // Sets a one-time handler for getting a senderName and a recurring handler for sending messages.
        tf_message.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    String username = takeMessage();

                    // Try to connect to the server with the given senderName.
                    try {
                        Socket socket = new Socket("localhost", 7777);
                        client = new Client(socket, username);
                        client.logIn();
                    } catch (IOException ioe) {
                        System.out.println("Error connecting to the server. Perhaps it is offline or your configuration is incorrect.");
                    }

                    // This is a one-time action for getting a senderName. The handler has fired, so remove it.
                    tf_message.removeEventHandler(KeyEvent.KEY_PRESSED, this);
                    client.listenForMessages(vbox_messages);
                    tf_message.setOnKeyPressed(ke -> handleTextFieldEnterKey(ke));
                }
            }
        });
    }

    /**
     * Sets the stage and register stage-based events
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        stage.setOnCloseRequest(getOnCloseHandler());
        button_choose.setOnMouseClicked(e -> chooseFile());
    }

    /**
     * Allows user to choose a file and adds it to the list of attachments.
     */
    private void chooseFile() {
        File chosenFile = fileChooser.showOpenDialog(stage);

        if (chosenFile != null) {
            attachments.add(chosenFile);
        }
    }

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
     * Sends the message in the text field along with any attachments and updates the display.
     */
    private void sendMessage() {
        String userInputMessage = takeMessage();

        if (!userInputMessage.isEmpty()) {
            // Display the message text
            HBox messageBox = createSentMessageBox(userInputMessage);
            vbox_messages.getChildren().add(messageBox);

            // Display the attachment names
            for (var a: attachments) {
                HBox attachmentBox = createSentMessageBox(a.getName());
                vbox_messages.getChildren().add(attachmentBox);
            }

            client.sendMessageFromInput(userInputMessage, attachments);

            // Reset the controller's attachments, but don't clear the original list since the client needs them
            attachments = new ArrayList<>();
        }
    }

    private void handleTextFieldEnterKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
        }
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
            public void run() { vbox.getChildren().add(messageBox); }
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
}
