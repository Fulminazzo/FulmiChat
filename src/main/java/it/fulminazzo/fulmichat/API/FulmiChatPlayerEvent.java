package it.fulminazzo.fulmichat.API;

import it.fulminazzo.fulmichat.Objects.ChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class FulmiChatPlayerEvent extends Event {
    private final static HandlerList handlerList = new HandlerList();
    private final Player player;
    private final Set<Player> players;
    private ChatMessage chatMessage;

    public FulmiChatPlayerEvent(Player player, Set<Player> players, ChatMessage chatMessage) {
        super(true);
        this.player = player;
        this.players = players;
        this.chatMessage = chatMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
