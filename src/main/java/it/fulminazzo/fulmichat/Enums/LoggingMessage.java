package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Enums.BearLoggingMessage;

public class LoggingMessage extends BearLoggingMessage {
    public static final LoggingMessage INVALID_EMOTICON_TYPE = new LoggingMessage("Got invalid emoticon type %type% while parsing emoji %name%.");
    public static final LoggingMessage INVALID_EMOJI_TYPE = new LoggingMessage("Got invalid emoji type %type% while parsing emoji %name%.");
    public static final LoggingMessage CHAT_MESSAGE_CANCELLED = new LoggingMessage("Chat message of player %player% got cancelled because ChatMessage was null!");

    public LoggingMessage(String message) {
        super(message);
    }
}
