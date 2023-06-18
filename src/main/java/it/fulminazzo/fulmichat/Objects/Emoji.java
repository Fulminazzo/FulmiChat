package it.fulminazzo.fulmichat.Objects;

import it.angrybear.Utils.StringUtils;
import it.fulminazzo.fulmichat.Enums.LoggingMessage;
import it.fulminazzo.fulmichat.Exceptions.UnrecognizedEmoji;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Emoji {
    private final String name;
    private final List<String> emoticons;
    private final List<String> emojis;

    public Emoji(ConfigurationSection emojiSection) throws UnrecognizedEmoji {
        this.name = emojiSection.getName().toLowerCase();

        Object emoticons = emojiSection.get("emoticon");
        if (emoticons instanceof String) this.emoticons = Collections.singletonList(emoticons.toString());
        else if (emoticons instanceof List) {
            this.emoticons = new ArrayList<>();
            for (Object o : (List<?>) emoticons) this.emoticons.add(String.valueOf(o));
        } else throw new UnrecognizedEmoji(LoggingMessage.INVALID_EMOTICON_TYPE,
                "%name%", name, "%type%", emoticons == null ? "null" : emoticons.getClass().getSimpleName());

        Object emojis = emojiSection.get("emoji");
        if (emojis instanceof String) this.emojis = Collections.singletonList(emojis.toString());
        else if (emojis instanceof List) {
            this.emojis = new ArrayList<>();
            for (Object o : (List<?>) emojis) this.emojis.add(String.valueOf(o));
        } else throw new UnrecognizedEmoji(LoggingMessage.INVALID_EMOJI_TYPE,
                "%name%", name, "%type%", emojis == null ? "null" : emojis.getClass().getSimpleName());
    }

    public String parseMessage(String message) {
        if (emojis.isEmpty()) return message;
        for (String emoticon : emoticons)
            message = StringUtils.replaceChatColors(message, emoticon,
                    StringUtils.parseMessage(emojis.get(new Random().nextInt(emojis.size()))), true);
        return message;
    }

    public List<String> getEmojis() {
        return emojis;
    }

    public List<String> getEmoticons() {
        return emoticons;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", emoticons, emojis);
    }
}