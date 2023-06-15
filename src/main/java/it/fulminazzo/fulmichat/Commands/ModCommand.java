package it.fulminazzo.fulmichat.Commands;

import it.fulminazzo.fulmichat.Enums.Message;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.graphics.Objects.GUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModCommand implements TabExecutor {
    private final FulmiChat plugin;

    public ModCommand(FulmiChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) sender.sendMessage(Message.CONSOLE_CANNOT_EXECUTE.getMessage(true));
        else if (args.length == 0) sender.sendMessage(Message.NOT_ENOUGH_ARGUMENTS.getMessage(true));
        else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null)
                sender.sendMessage(Message.PLAYER_NOT_FOUND.getMessage(true).replace("%player%", args[0]));
            else {
                GUI gui = plugin.getModerationGUI((Player) sender, target);
                if (gui == null)
                    sender.sendMessage(Message.GUI_ERROR.getMessage(true));
                else gui.openGUI((Player) sender);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1 && sender instanceof Player)
            list.addAll(Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(sender))
                    .map(HumanEntity::getName)
                    .collect(Collectors.toList()));
        return list;
    }
}
