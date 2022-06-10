package com.alexgrejuc.chatapp.message;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * A class that holds information about a message.
 */
public class MessageInfo {
    public String message;

    // Empty recipients indicates that a message should be sent to all users
    public Optional<ArrayList<String>> recipients;

    public MessageInfo(String message, Optional<ArrayList<String>> recipients) {
        this.message = message;
        this.recipients = recipients;
    }

    public MessageInfo(String message, ArrayList<String> recipients) {
        this(message, Optional.of(recipients));
    }

    public MessageInfo(String message) {
        this(message, Optional.empty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageInfo that = (MessageInfo) o;
        return message.equals(that.message) && recipients.equals(that.recipients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, recipients);
    }
}
