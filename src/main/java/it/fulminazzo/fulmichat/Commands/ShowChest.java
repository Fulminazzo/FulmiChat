package it.fulminazzo.fulmichat.Commands;

import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.FulmiChat;

public class ShowChest extends ShowChatGUI {
    public ShowChest(FulmiChat plugin) {
        super(plugin, ChatGUIType.CHEST);
    }
}