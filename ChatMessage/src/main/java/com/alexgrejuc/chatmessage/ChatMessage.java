package com.alexgrejuc.chatmessage;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @param message The message as typed by the user, excluding recipient names.
 * @param senderName The name of the sender.
 * @param recipientNames The names of the intended recipients. An empty list indicates a broadcast to all other clients.
 * @param attachments Files attached to this message.
 */
public record ChatMessage(
        String message,
        String senderName,
        ArrayList<String> recipientNames,
        ArrayList<Attachment> attachments
) implements Serializable {}
