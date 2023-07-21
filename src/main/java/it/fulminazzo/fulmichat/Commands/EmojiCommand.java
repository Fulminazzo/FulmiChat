package it.fulminazzo.fulmichat.Commands;

import it.angrybear.Bukkit.Commands.BearCommandExecutor;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.Enums.Message;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.fulmichat.Objects.Emoji;
import it.fulminazzo.fulmichat.Objects.EmojiGroup;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EmojiCommand extends BearCommandExecutor<FulmiChat> {

    public EmojiCommand(FulmiChat plugin) {
        super(plugin, "emoji", ChatPermission.EMOJI);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) sender.sendMessage(Message.CONSOLE_CANNOT_EXECUTE.getMessage(true));
        else {
            List<EmojiGroup> emojisGroups = plugin.getEmojiGroupsManager().getEmojiGroups();
            for (EmojiGroup emojiGroup : emojisGroups)
                for (Emoji emoji : emojiGroup.getEmojis())
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            String.format("&8- [&f%s&8] &7=> &8[&f%s&8]",
                                    String.join("&8, &f", emoji.getEmoticons()),
                                    String.join("&8, &f", emoji.getEmojis()))));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}