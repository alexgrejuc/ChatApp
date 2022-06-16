module com.alexgrejuc.chatclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires ChatMessage;


    opens com.alexgrejuc.chatclient to javafx.fxml;
    exports com.alexgrejuc.chatclient;
}