package it.fulminazzo.fulmichat.Managers;

import it.fulminazzo.fulmichat.Enums.LoggingMessage;
import it.fulminazzo.fulmichat.Exceptions.NotEmojiGroup;
import it.fulminazzo.fulmichat.Exceptions.UnrecognizedEmoji;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.fulmichat.Objects.Emoji;
import it.fulminazzo.fulmichat.Objects.EmojiGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmojiGroupsManager {
    private final List<EmojiGroup> emojiGroups;

    public EmojiGroupsManager(ConfigurationSection emojisSection) {
        this.emojiGroups = new ArrayList<>();
        YamlConfiguration tmp = new YamlConfiguration();
        tmp.createSection("default");
        try {
            this.emojiGroups.add(new EmojiGroup(tmp.getConfigurationSection("default")));
        } catch (NotEmojiGroup ignored) {}

        if (emojisSection == null) return;
        emojisSection.getKeys(false).stream()
                .map(emojisSection::getConfigurationSection)
                .filter(Objects::nonNull)
                .forEach(s -> {
                    try {
                        EmojiGroup emojiGroup = new EmojiGroup(s);
                        EmojiGroup prevEmojiGroup = getEmojiGroup(emojiGroup.getName());
                        if (prevEmojiGroup == null) emojiGroups.add(emojiGroup);
                        else prevEmojiGroup.addEmojis(emojiGroup.getEmojis());
                    } catch (NotEmojiGroup e) {
                        try {
                            getEmojiGroup("default").addEmojis(new Emoji(s));
                        } catch (UnrecognizedEmoji ex) {
                            FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                                    "%task%", "parsing a group",
                                    "%error%", e.getMessage());
                        }
                    }
                });
    }

    public EmojiGroup getEmojiGroup(String name) {
        if (name == null) return null;
        return emojiGroups.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    public List<EmojiGroup> getEmojiGroups() {
        return emojiGroups;
    }
}