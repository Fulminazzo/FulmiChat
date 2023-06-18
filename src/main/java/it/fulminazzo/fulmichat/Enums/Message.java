package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Utils.StringUtils;
import it.fulminazzo.fulmichat.FulmiChat;

public enum Message {
    PREFIX("prefix"),
    CONSOLE_CANNOT_EXECUTE("console-cannot-execute"),
    NOT_ENOUGH_ARGUMENTS("not-enough-arguments"),
    PLAYER_NOT_FOUND("player-not-found"),
    GUI_ERROR("gui-error"),
    SHOW_INVENTORY("click-to-show-inventory"),
    SHOW_ENDER("click-to-show-ender");

    private final String path;

    Message(String path) {
        this.path = path;
    }

    public String getMessage(boolean prefix) {
        String message = FulmiChat.getPlugin().getLang().getString(path);
        if (message == null) message = "";
        if (prefix) message = Message.PREFIX.getMessage(false) + message;
        return StringUtils.parseMessage(message);
    }
}