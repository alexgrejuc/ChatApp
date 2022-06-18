package com.alexgrejuc.chatclient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;

/**
 * Methods for creating display elements.
 */
public class Displayer {

    public static HBox createSentBox() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 10));
        return hBox;
    }

    /**
     * Creates a blue right-aligned box for displaying a message sent by the client.
     * @param message
     * @return
     */
    public static HBox createSentMessageBox(String message) {
        HBox hBox = createSentBox();

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
     * Creates a blue right-aligned box for displaying an image that the client received.
     * @param imageBytes
     */
    public static HBox createSentImageBox(byte[] imageBytes) {
        var v = createImageView(imageBytes);
        HBox hBox = createSentBox();
        hBox.getChildren().add(v);
        return hBox;
    }

    /**
     * Creates a left-aligned, padded box without any contents.
     * @return An empty, stylized box.
     */
    public static HBox createReceivedBox() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));
        return hBox;
    }

    /**
     * Creates a gray left-aligned box for displaying text contents of a message that the client received.
     * @param message
     * @return A box containing the message text.
     */
    public static HBox createReceivedMessageBox(String message) {
        HBox hBox = createReceivedBox();

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" +
                " -fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hBox.getChildren().add(textFlow);
        return hBox;
    }

    /**
     * Creates a stylized ImageView.
     * @param imageBytes
     */
    public static ImageView createImageView(byte[] imageBytes) {
        Image i = new Image(new ByteArrayInputStream(imageBytes), 350, 0, true, false);
        return new ImageView(i);
    }

    /**
     * Creates a gray left-aligned box for displaying an image that the client received.
     * @param imageBytes
     */
    public static HBox createReceivedImageBox(byte[] imageBytes) {
        var v = createImageView(imageBytes);
        HBox hBox = createReceivedBox();
        hBox.getChildren().add(v);
        return hBox;
    }
}
