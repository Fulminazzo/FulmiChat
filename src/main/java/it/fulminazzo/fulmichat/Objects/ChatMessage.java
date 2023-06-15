package it.fulminazzo.fulmichat.Objects;

import it.fulminazzo.fulmichat.FulmiChat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ChatMessage {
    private ComponentBuilder message;
    private TextComponent check;

    public ChatMessage(String message, Player player) {
        this(new ComponentBuilder(message), player);
    }

    public ChatMessage(TextComponent message, Player player) {
        this(new ComponentBuilder(message), player);
    }

    public ChatMessage(ComponentBuilder message, Player player) {
        setMessage(message);
        TextComponent check = null;
        String moderationCheck = FulmiChat.getPlugin().getModerationCheck();
        if (moderationCheck != null) check = new TextComponent(moderationCheck);
        setCheck(check, player);
    }

    public BaseComponent[] getUserMessage() {
        return message == null ? new BaseComponent[0] : message.create();
    }

    public BaseComponent[] getModMessage() {
        if (message == null || check == null) return getUserMessage();
        else return new ComponentBuilder(message).append(" ").append(check).create();
    }

    public void setMessage(String message) {
        this.message = new ComponentBuilder(message);
    }

    public void setMessage(BaseComponent message) {
        this.message = new ComponentBuilder(message);
    }

    public void setMessage(ComponentBuilder message) {
        this.message = message;
    }

    public ComponentBuilder getMessage() {
        return message;
    }

    public void setCheck(TextComponent check) {
        this.check = check;
    }

    public void setCheck(TextComponent check, Player player) {
        if (check != null)
            check.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    String.format("/moderate %s", player.getName())));
        this.check = check;
    }

    public TextComponent getCheck() {
        return check;
    }
}