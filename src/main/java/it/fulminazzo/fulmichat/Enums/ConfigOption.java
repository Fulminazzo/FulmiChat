package it.fulminazzo.fulmichat.Enums;

import it.angrybear.Enums.BearConfigOption;
import it.fulminazzo.fulmichat.FulmiChat;

public class ConfigOption extends BearConfigOption {
    public static final ConfigOption ITEM_PLACEHOLDER = new ConfigOption("item-placeholder");
    public static final ConfigOption ITEM_PLACEHOLDER_PARSED = new ConfigOption("item-placeholder-parsed");
    public static final ConfigOption PLAYER_MENTION = new ConfigOption("player-mention");
    public static final ConfigOption PLAYER_MENTION_PARSED = new ConfigOption("player-mention-parsed");
    public static final ConfigOption PLAYER_MENTION_SOUND = new ConfigOption("player-mention-sound");

    public ConfigOption(String path) {
        super(FulmiChat.getPlugin(), path);
    }
}
