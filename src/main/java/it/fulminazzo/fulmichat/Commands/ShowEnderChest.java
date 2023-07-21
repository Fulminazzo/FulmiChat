package it.fulminazzo.fulmichat.Commands;

import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.FulmiChat;

public class ShowEnderChest extends ShowChatGUI {
    public ShowEnderChest(FulmiChat plugin) {
        super(plugin, "showenderchest", ChatPermission.ENDER, ChatGUIType.ENDER_CHEST);
    }
}