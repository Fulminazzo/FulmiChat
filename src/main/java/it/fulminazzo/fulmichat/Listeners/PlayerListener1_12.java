package it.fulminazzo.fulmichat.Listeners;

import it.angrybear.Utils.StringUtils;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.fulmichat.Objects.EmojiGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.BroadcastMessageEvent;

import java.util.List;

public class PlayerListener1_12 extends PlayerListener {

    public PlayerListener1_12(FulmiChat plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBroadcast(BroadcastMessageEvent event) {
        String message = event.getMessage();
        if (event.isCancelled()) return;

        // Emojis
        List<EmojiGroup> emojisGroups = plugin.getEmojiGroupsManager().getEmojiGroups();
        for (EmojiGroup emojiGroup : emojisGroups) message = emojiGroup.parseMessage(message);

        event.setMessage(StringUtils.parseMessage(message));
    }
}