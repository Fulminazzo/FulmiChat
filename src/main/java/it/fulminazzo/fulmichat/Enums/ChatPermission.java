package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Enums.BearPermission;
import it.fulminazzo.fulmichat.FulmiChat;

public class ChatPermission extends BearPermission {
    public static final ChatPermission EMOJI = new ChatPermission("emoji.%s");
    public static final ChatPermission MENTION = new ChatPermission("mention");
    public static final ChatPermission ITEM = new ChatPermission("item");
    public static final ChatPermission INVENTORY = new ChatPermission("inventory");
    public static final ChatPermission ENDER = new ChatPermission("ender");
    public static final ChatPermission CHEST = new ChatPermission("chest");
    public static final ChatPermission PING = new ChatPermission("ping");
    public static final ChatPermission MOD = new ChatPermission("mod");
    public static final ChatPermission COLORED_CHAT = new ChatPermission("colored-chat");

    public ChatPermission(String permission) {
        super(permission);
    }

    @Override
    public String getPermission() {
        return getPermission(FulmiChat.getPlugin());
    }

    public static ChatPermission[] values() {
        return values(ChatPermission.class);
    }
}
