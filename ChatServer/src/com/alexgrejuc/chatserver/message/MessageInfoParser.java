package com.alexgrejuc.chatserver.message;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MessageInfoParser {

    /**
     * Parses a user's input string into message information.
     * @param inputString Everything the user has typed, including recipients. Must not be null.
     * @return the parsed message information
     */
    public static MessageInfo parse(String inputString) {
        var recipientPattern = Pattern.compile("(@\\w+\\s)");
        var matcher = recipientPattern.matcher(inputString);

        var recipients = new ArrayList<String>();
        int end = -1;

        // Find all the recipients, if any
        while (matcher.find()) {
            recipients.add(matcher.group().replace("@", "").stripTrailing());
            end = matcher.end();
        }

        // The message is any text that comes after the last recipient
        var message = inputString.substring(Math.max(end, 0));

        if (recipients.isEmpty()) {
            return new MessageInfo(message);
        }
        else {
            return new MessageInfo(message, recipients);
        }
    }
}
