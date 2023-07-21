package it.fulminazzo.fulmichat.Commands;

import it.angrybear.Bukkit.Commands.BearCommandExecutor;
import it.angrybear.Enums.BearPermission;
import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.Enums.Message;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.graphics.Objects.GUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class ShowChatGUI extends BearCommandExecutor<FulmiChat> {
    private final ChatGUIType chatGUIType;

    public ShowChatGUI(FulmiChat plugin, String name, BearPermission permission, ChatGUIType chatGUIType) {
        super(plugin, name, permission);
        this.chatGUIType = chatGUIType;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player)
            if (args.length == 0) sender.sendMessage(Message.NOT_ENOUGH_ARGUMENTS.getMessage(true));
            else {
                GUI chatGUI = plugin.getGuiManager().getGUI(chatGUIType, args[0]);
                if (chatGUI == null) sender.sendMessage(Message.GUI_ERROR.getMessage(true));
                else chatGUI.openGUI((Player) sender);
            }
        else sender.sendMessage(Message.CONSOLE_CANNOT_EXECUTE.getMessage(true));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>();
    }
}