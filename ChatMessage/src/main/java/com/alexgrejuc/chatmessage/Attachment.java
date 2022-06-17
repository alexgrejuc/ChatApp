package com.alexgrejuc.chatmessage;

import java.io.Serializable;

/**
 * The name of a file and its contents.
 * @param name
 * @param contents
 */
public record Attachment(
        String name,
        byte[] contents
) implements Serializable {}
