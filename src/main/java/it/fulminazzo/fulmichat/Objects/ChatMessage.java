package it.fulminazzo.fulmichat.Objects;

import it.fulminazzo.fulmichat.FulmiChat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ChatMessage {
    private BaseComponent[] message;
    private TextComponent check;

    public ChatMessage(String message, Player player) {
        this(new TextComponent(message), player);
    }

    public ChatMessage(TextComponent message, Player player) {
        setMessage(message);
        TextComponent check = null;
        String moderationCheck = FulmiChat.getPlugin().getModerationCheck();
        if (moderationCheck != null) check = new TextComponent(moderationCheck);
        setCheck(check, player);
    }

    public TextComponent getUserMessage() {
        return message == null ? new TextComponent() : new TextComponent(message);
    }

    public TextComponent getModMessage() {
        TextComponent modComponent = getUserMessage();
        if (check != null) {
            modComponent.addExtra(" ");
            modComponent.addExtra(check);
        }
        return modComponent;
    }

    public void setMessage(String message) {
        this.message = TextComponent.fromLegacyText(message);
    }

    public void setMessage(TextComponent message) {
        this.message = new BaseComponent[]{new TextComponent(message)};
    }

    public BaseComponent[] getMessage() {
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