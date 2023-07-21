package it.fulminazzo.fulmichat.Commands;

import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.FulmiChat;

public class ShowItem extends ShowChatGUI {
    public ShowItem(FulmiChat plugin) {
        super(plugin, "showitem", ChatPermission.ITEM, ChatGUIType.ITEM);
    }
}