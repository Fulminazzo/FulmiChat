package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Enums.BearPermission;
import it.fulminazzo.fulmichat.FulmiChat;

public class Permission extends BearPermission {
    public static final Permission EMOJI = new Permission("emoji.%s");
    public static final Permission MENTION = new Permission("mention");
    public static final Permission ITEM = new Permission("item");
    public static final Permission INVENTORY = new Permission("inventory");
    public static final Permission ENDER = new Permission("ender");
    public static final Permission PING = new Permission("ping");
    public static final Permission MOD = new Permission("mod");
    public static final Permission COLORED_CHAT = new Permission("colored-chat");

    public Permission(String permission) {
        super(permission);
    }

    @Override
    public String getPermission() {
        return getPermission(FulmiChat.getPlugin());
    }

    public static Permission[] values() {
        return values(Permission.class);
    }
}
