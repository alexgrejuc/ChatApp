package com.alexgrejuc.chatmessage;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageParserTest {
    ArrayList<File> noAttachments = new ArrayList<>();

    @Test
    void messageAll() {
        var expectedMessage = "Hello, world!";

        // A null recipients list indicates that the message should go to all clients
        // This occurs when the user does not specify any dm recipients
        ArrayList<String> messageAll = null;

        var expectedChatMessage = new ChatMessage(expectedMessage, "Alice", messageAll, noAttachments);
        var parsedChatMessage = ChatMessageParser.parse(expectedMessage, "Alice", noAttachments);
        assertEquals(expectedChatMessage, parsedChatMessage);
    }


    @Test
    void messageSome() {
        var expectedMessageText = "Hi, Alice and Bob!!";
        var expectedRecipients = new ArrayList<>(Arrays.asList("Alice", "Bob"));

        var expectedChatMessage = new ChatMessage(expectedMessageText, "Alice", expectedRecipients, noAttachments);
        var parsedInfo = ChatMessageParser.parse("@Alice @Bob " + expectedMessageText, "Alice", noAttachments);

        assertEquals(expectedChatMessage, parsedInfo);
    }


    @Test
    void messageOne() {
        var expectedMessageText = "Hi, Bob!";
        var expectedRecipients = new ArrayList<>(Arrays.asList("Bob"));

        var expectedChatMessage = new ChatMessage(expectedMessageText, "Alice", expectedRecipients, noAttachments);
        var parsedInfo = ChatMessageParser.parse("@Bob " + expectedMessageText, "Alice", noAttachments);

        assertEquals(expectedChatMessage, parsedInfo);
    }

    @Test
    void containsAtInMessage() {
        var expectedMessageText = "You can can DM me by typing @Alice";
        var expectedRecipients = new ArrayList<>(Arrays.asList("Bob"));

        var expectedChatMessage = new ChatMessage(expectedMessageText, "Alice", expectedRecipients, noAttachments);
        var parsedInfo = ChatMessageParser.parse("@Bob " + expectedMessageText, "Alice", noAttachments);

        assertEquals(expectedChatMessage, parsedInfo);
    }
}