package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Enums.BearPermission;
import it.fulminazzo.fulmichat.FulmiChat;

public class Permission extends BearPermission {
    public static final Permission GROUP_PERMISSION = new Permission("emoji.%s");
    public static final Permission MENTION_PERMISSION = new Permission("mention");
    public static final Permission ITEM_PERMISSION = new Permission("item");
    public static final Permission MOD = new Permission("mod");
    public static final Permission COLORED_CHAT = new Permission("colored-chat");

    public Permission(String permission) {
        super(permission);
    }

    @Override
    public String getPermission() {
        return getPermission(FulmiChat.getPlugin());
    }
}