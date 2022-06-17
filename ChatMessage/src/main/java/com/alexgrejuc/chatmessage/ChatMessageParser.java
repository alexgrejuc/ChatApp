package com.alexgrejuc.chatmessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

public class ChatMessageParser {
    /**
     * Parses a user's input string and combines that with additional information to make a ChatMessage.
     * @param inputString Everything the user has typed, including recipientNames. Must not be null.
     * @param senderName The name of the sender.
     * @param attachments Attachments associated with this message.
     * @return the parsed message information
     */
    public static ChatMessage parse(String inputString, String senderName, ArrayList<Attachment> attachments) {
        var recipientPattern = Pattern.compile("(@\\w+\\s)");
        var matcher = recipientPattern.matcher(inputString);

        var recipients = new ArrayList<String>();
        int end = -1;

        // Find all the recipientNames, if any
        while (matcher.find()) {
            recipients.add(matcher.group().replace("@", "").stripTrailing());
            end = matcher.end();
        }

        // The message is any text that comes after the last recipient
        var message = inputString.substring(Math.max(end, 0));

        // No recipients specified means it is a message to all other clients.
        // In this case, the server will populate the recipients
        recipients = recipients.isEmpty() ? null : recipients;

        return new ChatMessage(message, senderName, recipients, attachments);
    }
}
