package it.fulminazzo.fulmichat.Listeners;

import it.angrybear.Utils.HexUtils;
import it.angrybear.Utils.StringUtils;
import it.angrybear.Utils.TextComponentUtils;
import it.fulminazzo.fulmichat.API.FulmiChatPlayerEvent;
import it.fulminazzo.fulmichat.Enums.ConfigOption;
import it.fulminazzo.fulmichat.Enums.LoggingMessage;
import it.fulminazzo.fulmichat.Enums.Permission;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.fulmichat.Objects.ChatMessage;
import it.fulminazzo.fulmichat.Objects.EmojiGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerListener implements Listener {
    private final FulmiChat plugin;

    public PlayerListener(FulmiChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        ComponentBuilder finalMessage = null;

        if (event.isCancelled()) return;

        // Placeholders
        event.setFormat(FulmiChat.formatPlaceholders(event.getFormat(), player));
        message = String.join("", StringUtils.getCleanChatCodesInString(event.getFormat(), true)) + message;

        // Emojis
        List<EmojiGroup> emojisGroups = plugin.getEmojiGroupsManager().getEmojiGroups();
        for (EmojiGroup emojiGroup : emojisGroups)
            if (emojiGroup.isPlayerEligible(player)) {
                message = emojiGroup.parseMessage(message);
                event.setFormat(emojiGroup.parseMessage(event.getFormat()));
            }

        // Mention
        if (player.hasPermission(Permission.MENTION_PERMISSION.getPermission()))
            for (Player p : Bukkit.getOnlinePlayers())
                if (!p.equals(player)) {
                    String soundName = ConfigOption.PLAYER_MENTION_SOUND.getString();
                    if (soundName != null && message.contains(p.getName()))
                        Arrays.stream(Sound.values())
                                .filter(s -> s.name().equalsIgnoreCase(soundName))
                                .findAny().ifPresent(sound ->
                                        p.playSound(p.getLocation(), sound, 10, 1));
                    message = StringUtils.replaceChatColors(message,
                            ConfigOption.PLAYER_MENTION.getString().replace("%player-name%", p.getName()),
                            ConfigOption.PLAYER_MENTION_PARSED.getMessage().replace("%player-name%", p.getName()));
                }

        if (player.hasPermission(Permission.COLORED_CHAT.getPermission())) message = StringUtils.parseMessage(message).trim();
        if (ChatColor.stripColor(message).equalsIgnoreCase("")) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(message);

        // Item
        List<String> itemPlaceholders = ConfigOption.ITEM_PLACEHOLDER.getStringList();
        for (String itemPlaceholder : itemPlaceholders) {
            ComponentBuilder tmp = parseItemComponent(event, itemPlaceholder);
            if (tmp != null) finalMessage = tmp;
        }

        // Moderation
        event.setCancelled(true);
        if (finalMessage == null) finalMessage = formatMessageToTextComponent(event);
        ChatMessage chatMessage = new ChatMessage(finalMessage, event.getPlayer());
        FulmiChatPlayerEvent fulmiChatPlayerEvent = new FulmiChatPlayerEvent(player, event.getRecipients(), chatMessage);
        Bukkit.getPluginManager().callEvent(fulmiChatPlayerEvent);
        chatMessage = fulmiChatPlayerEvent.getChatMessage();

        if (chatMessage == null) {
            FulmiChat.logWarning(LoggingMessage.CHAT_MESSAGE_CANCELLED, "%player%", event.getPlayer().getName());
            return;
        }
        for (Player p : event.getRecipients())
            if (p.hasPermission(Permission.MOD.getPermission())) p.spigot().sendMessage(chatMessage.getModMessage());
            else p.spigot().sendMessage(chatMessage.getUserMessage());
        Bukkit.getConsoleSender().sendMessage(TextComponentUtils.componentBuilderToString(chatMessage.getMessage()));
    }

    private ComponentBuilder formatMessageToTextComponent(AsyncPlayerChatEvent event) {
        ComponentBuilder componentBuilder = TextComponentUtils.stringToComponentBuilder(event.getMessage(),
                event.getPlayer().hasPermission(Permission.COLORED_CHAT.getPermission()));
        return formatMessageToTextComponent(event, componentBuilder);
    }

    private ComponentBuilder formatMessageToTextComponent(AsyncPlayerChatEvent event, ComponentBuilder textComponent) {
        Player player = event.getPlayer();
        String[] format = HexUtils.unParseHexColor(event.getFormat()).replace("%1$s", player.getDisplayName()).split("%2\\$s");
        ComponentBuilder finalMessage = new ComponentBuilder("");
        for (String f : format) {
            finalMessage.append(TextComponentUtils.stringToComponentBuilder(f, true).create());
            if (!f.equals(format[format.length - 1])) finalMessage.append(textComponent.create());
        }
        if (event.getFormat().endsWith("%2$s")) finalMessage.append(textComponent.create());
        return finalMessage;
    }

    private ComponentBuilder parseItemComponent(AsyncPlayerChatEvent event, String itemPlaceholder) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (!player.hasPermission(Permission.ITEM_PERMISSION.getPermission())) return null;
        if (!message.contains(itemPlaceholder)) return null;
        ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return null;

        String[] tmp = message.split(itemPlaceholder
                .replace("[", "\\[")
                .replace("]", "\\]"));
        ComponentBuilder textComponent = new ComponentBuilder("");
        List<String> lastCodes = new ArrayList<>();
        for (String t : tmp) {
            ComponentBuilder tComponent = TextComponentUtils.stringToComponentBuilder(
                    String.join("", lastCodes) + t,
                    event.getPlayer().hasPermission(Permission.COLORED_CHAT.getPermission()));
            textComponent.append(tComponent.create());
            List<String> tmpCodes = StringUtils.getCleanChatCodesInString(t, true);
            if (!tmpCodes.isEmpty()) lastCodes = tmpCodes;

            if (!t.equals(tmp[tmp.length - 1]))
                if (parseItem(message, itemPlaceholder, itemStack, textComponent)) return null;
        }
        if (message.endsWith(itemPlaceholder))
            if (parseItem(message, itemPlaceholder, itemStack, textComponent)) return null;

        return formatMessageToTextComponent(event, textComponent);
    }

    private boolean parseItem(String message, String itemPlaceholder, ItemStack itemStack, ComponentBuilder textComponent) {
        try {
            TextComponent itemComponent = TextComponentUtils.getItemComponent(itemStack);
            itemComponent.setText(ConfigOption.ITEM_PLACEHOLDER_PARSED.getMessage()
                            .replace("%item%", itemComponent.getText()));
            textComponent.append(itemComponent);
        } catch (Exception e) {
            FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                    "%task%", String.format("parsing %s in %s", itemPlaceholder, message),
                    "%error%", e.getMessage());
            return true;
        }
        return false;
    }
}