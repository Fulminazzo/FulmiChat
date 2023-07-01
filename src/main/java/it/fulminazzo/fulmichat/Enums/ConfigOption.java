package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Enums.BearConfigOption;
import it.fulminazzo.fulmichat.FulmiChat;

public class ConfigOption extends BearConfigOption {
    public static final ConfigOption ITEM_PLACEHOLDER = new ConfigOption("item-placeholder");
    public static final ConfigOption ITEM_PLACEHOLDER_PARSED = new ConfigOption("item-placeholder-parsed");
    public static final ConfigOption INVENTORY_PLACEHOLDER = new ConfigOption("inventory-placeholder");
    public static final ConfigOption INVENTORY_PLACEHOLDER_PARSED = new ConfigOption("inventory-placeholder-parsed");
    public static final ConfigOption ENDER_PLACEHOLDER = new ConfigOption("ender-placeholder");
    public static final ConfigOption ENDER_PLACEHOLDER_PARSED = new ConfigOption("ender-placeholder-parsed");
    public static final ConfigOption CHEST_PLACEHOLDER = new ConfigOption("chest-placeholder");
    public static final ConfigOption CHEST_PLACEHOLDER_PARSED = new ConfigOption("chest-placeholder-parsed");
    public static final ConfigOption PING_PLACEHOLDER = new ConfigOption("ping-placeholder");
    public static final ConfigOption PING_PLACEHOLDER_PARSED = new ConfigOption("ping-placeholder-parsed");
    public static final ConfigOption PLAYER_MENTION = new ConfigOption("player-mention");
    public static final ConfigOption PLAYER_MENTION_PARSED = new ConfigOption("player-mention-parsed");
    public static final ConfigOption PLAYER_MENTION_SOUND = new ConfigOption("player-mention-sound");

    public ConfigOption(String path) {
        super(FulmiChat.getPlugin(), path);
    }
}
