package com.alexgrejuc.chatserver.message;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MessageInfoParserTest {
    @Test
    void messageAll() {
        var expectedMessage = "Hello, world!";
        var expectedMessageInfo = new MessageInfo(expectedMessage);
        var parsedMessageInfo = MessageInfoParser.parse(expectedMessage);
        assertEquals(expectedMessageInfo, parsedMessageInfo);
    }

    @Test
    void messageSome() {
        var expectedMessageText = "Hi, Alice and Bob!!";
        var expectedRecipients = new ArrayList<String>(Arrays.asList("Alice", "Bob"));

        var expectedMessageInfo = new MessageInfo(expectedMessageText, expectedRecipients);
        var parsedInfo = MessageInfoParser.parse("@Alice @Bob " + expectedMessageText);

        assertEquals(expectedMessageInfo, parsedInfo);
    }

    @Test
    void messageOne() {
        var expectedMessageText = "Hi, Bob!";
        var expectedRecipients = new ArrayList<String>(Arrays.asList("Bob"));

        var expectedMessageInfo = new MessageInfo(expectedMessageText, expectedRecipients);
        var parsedInfo = MessageInfoParser.parse("@Bob " + expectedMessageText);

        assertEquals(expectedMessageInfo, parsedInfo);
    }

    @Test
    void containsAtInMessage() {
        var expectedMessageText = "You can can DM me by typing @Alice";
        var expectedRecipients = new ArrayList<String>(Arrays.asList("Bob"));

        var expectedMessageInfo = new MessageInfo(expectedMessageText, expectedRecipients);
        var parsedInfo = MessageInfoParser.parse("@Bob " + expectedMessageText);

        assertEquals(expectedMessageInfo, parsedInfo);
    }
}