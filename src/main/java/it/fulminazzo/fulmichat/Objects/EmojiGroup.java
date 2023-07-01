package it.fulminazzo.fulmichat.Objects;

import it.fulminazzo.fulmichat.Enums.LoggingMessage;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.Exceptions.NotEmojiGroup;
import it.fulminazzo.fulmichat.Exceptions.UnrecognizedEmoji;
import it.fulminazzo.fulmichat.FulmiChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmojiGroup {
    private final String name;
    private final List<Emoji> emojis;

    public EmojiGroup(ConfigurationSection groupSection) throws NotEmojiGroup {
        this.name = groupSection.getName().toLowerCase();
        this.emojis = new ArrayList<>();

        if (groupSection.contains("emoticon") && groupSection.contains("emoji"))
            throw new NotEmojiGroup();

        groupSection.getKeys(false).stream()
                .map(groupSection::getConfigurationSection)
                .filter(Objects::nonNull)
                .forEach(s -> {
                    try {
                        Emoji emoji = new Emoji(s);
                        if (emojis.stream().noneMatch(e -> e.getName().equals(emoji.getName())))
                            emojis.add(emoji);
                    } catch (UnrecognizedEmoji e) {
                        FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                                "%task%", "parsing an Emoji",
                                "%error%", e.getMessage());
                    }
                });
    }

    public boolean isPlayerEligible(Player player) {
        return player.hasPermission(getPermission());
    }

    public String parseMessage(String message) {
        for (Emoji emoji : emojis) message = emoji.parseMessage(message);
        return message;
    }

    public void addEmojis(Emoji... emojis) {
        addEmojis(Arrays.asList(emojis));
    }

    public void addEmojis(List<Emoji> emojis) {
        emojis.stream()
                .filter(e -> this.emojis.stream().noneMatch(e2 -> e2.getName().equals(e.getName())))
                .forEach(this.emojis::add);
    }

    public List<Emoji> getEmojis() {
        return emojis;
    }

    public String getPermission() {
        return String.format(ChatPermission.EMOJI.getPermission(), name).toLowerCase();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\n" + this.emojis.stream().map(Emoji::toString).collect(Collectors.joining("\n"));
    }
}