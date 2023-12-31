package it.fulminazzo.fulmichat.Commands;

import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.FulmiChat;

public class ShowInventory extends ShowChatGUI {
    public ShowInventory(FulmiChat plugin) {
        super(plugin, "showinventory", ChatPermission.INVENTORY, ChatGUIType.INVENTORY);
    }
}