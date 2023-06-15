package it.fulminazzo.fulmichat.Exceptions;

import it.angrybear.Exceptions.PluginException;
import it.fulminazzo.fulmichat.Enums.LoggingMessage;

public class UnrecognizedEmoji extends PluginException {
    public UnrecognizedEmoji(LoggingMessage message, String... strings) {
        super(message, strings);
    }
}
