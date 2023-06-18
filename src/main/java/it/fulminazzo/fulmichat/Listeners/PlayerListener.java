package it.fulminazzo.fulmichat.Listeners;

import it.angrybear.Objects.ReflObject;
import it.angrybear.Utils.HexUtils;
import it.angrybear.Utils.StringUtils;
import it.angrybear.Utils.TextComponentUtils;
import it.angrybear.Utils.VersionsUtils;
import it.fulminazzo.fulmichat.API.FulmiChatPlayerEvent;
import it.fulminazzo.fulmichat.Enums.ConfigOption;
import it.fulminazzo.fulmichat.Enums.LoggingMessage;
import it.fulminazzo.fulmichat.Enums.Message;
import it.fulminazzo.fulmichat.Enums.Permission;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.fulmichat.Objects.ChatMessage;
import it.fulminazzo.fulmichat.Objects.EmojiGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PlayerListener implements Listener {
    private final FulmiChat plugin;

    public PlayerListener(FulmiChat plugin) {
        this.plugin = plugin;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (event.isCancelled()) return;

        // Emojis
        List<EmojiGroup> emojisGroups = plugin.getEmojiGroupsManager().getEmojiGroups();
        for (EmojiGroup emojiGroup : emojisGroups)
            if (emojiGroup.isPlayerEligible(player))
                message = emojiGroup.parseMessage(message);

        event.setMessage(message);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        ComponentBuilder finalMessage;

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
        if (player.hasPermission(Permission.MENTION_PERMISSION.getPermission())) message = parseMention(message, player);
        if (player.hasPermission(Permission.COLORED_CHAT.getPermission())) message = StringUtils.parseMessage(message).trim();
        if (ChatColor.stripColor(message).equalsIgnoreCase("")) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(message);

        // Item
        finalMessage = parsePlaceholders(event);

        // Moderation
        event.setCancelled(true);
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

    private ComponentBuilder formatMessageToTextComponent(AsyncPlayerChatEvent event, ComponentBuilder textComponent) {
        Player player = event.getPlayer();
        String[] format = HexUtils.unParseHexColor(event.getFormat()).replace("%1$s", player.getDisplayName()).split("%2\\$s");
        ComponentBuilder finalMessage = new ComponentBuilder("");
        for (String f : format) {
            finalMessage.append(TextComponentUtils.stringToComponentBuilder(f, true).create());
            if (!f.equals(format[format.length - 1])) finalMessage.append(textComponent.create(), ComponentBuilder.FormatRetention.NONE);
        }
        if (event.getFormat().endsWith("%2$s")) finalMessage.append(textComponent.create(), ComponentBuilder.FormatRetention.NONE);
        return finalMessage;
    }

    private String parseMention(String message, Player player) {
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
        return message;
    }

    private ComponentBuilder parsePlaceholders(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

        ComponentBuilder finalMessage = new ComponentBuilder("");
        boolean openBrackets = false;
        List<String> colorCodes = StringUtils.getCleanChatCodesInString(event.getFormat(), VersionsUtils.is1_16());
        String tmp = String.join("", colorCodes);
        for (String c : message.split("")) {
            if (openBrackets)
                if (c.equals("]")) {
                    openBrackets = false;
                    if (!tmp.equals("") && player.hasPermission(Permission.ITEM_PERMISSION.getPermission()) && itemStack != null)
                        tmp = parseItem(tmp, itemStack, finalMessage, message);

                    if (!tmp.equals("") && player.hasPermission(Permission.INVENTORY_PERMISSION.getPermission()))
                        tmp = parseInventory(tmp, player, finalMessage, message);

                    if (!tmp.equals("") && player.hasPermission(Permission.ENDER_PERMISSION.getPermission()))
                        tmp = parseEnderChest(tmp, player, finalMessage, message);

                    if (!tmp.equals("") && player.hasPermission(Permission.PING_PERMISSION.getPermission()))
                        tmp = parsePing(tmp, player, finalMessage, message);

                    if (!tmp.equals(""))
                        finalMessage.append(String.format("%s[%s]", String.join("", colorCodes), tmp), ComponentBuilder.FormatRetention.NONE);
                    tmp = String.join("", colorCodes);
                } else tmp += c;
            else
                if (c.equals("[")) {
                    openBrackets = true;
                    finalMessage.append(tmp, ComponentBuilder.FormatRetention.NONE);
                    colorCodes = StringUtils.getCleanChatCodesInString(TextComponentUtils.componentBuilderToString(finalMessage),
                            VersionsUtils.is1_16());
                    tmp = "";
                } else tmp += c;
        }
        if (!tmp.equals("")) finalMessage.append(tmp, ComponentBuilder.FormatRetention.NONE);
        return formatMessageToTextComponent(event, finalMessage);
    }

    private String parseItem(String tmp, ItemStack itemStack, ComponentBuilder finalMessage, String message) {
        for (String placeholder : ConfigOption.ITEM_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    TextComponent itemComponent = TextComponentUtils.getItemComponent(itemStack);
                    itemComponent.setText(ConfigOption.ITEM_PLACEHOLDER_PARSED.getMessage()
                            .replace("%item%", itemComponent.getText()));
                    finalMessage.append(itemComponent, ComponentBuilder.FormatRetention.NONE);
                    tmp = "";
                } catch (Exception e) {
                    FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                            "%task%", String.format("parsing [%s] in %s", tmp, message),
                            "%error%", e.getMessage());
                }
                break;
            }
        return tmp;
    }

    private String parseInventory(String tmp, Player player, ComponentBuilder finalMessage, String message) {
        for (String placeholder : ConfigOption.INVENTORY_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    TextComponent inventoryComponent = new TextComponent(ConfigOption.INVENTORY_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName()));
                    if (VersionsUtils.is1_9())
                        new ReflObject<>(inventoryComponent).callMethod("setHoverEvent",
                                TextComponentUtils.getTextHoverEvent(Message.SHOW_INVENTORY.getMessage(false)
                                        .replace("%player%", player.getDisplayName())));
                    inventoryComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/showinventory %s", plugin.getGuiManager().addInventory(player))));
                    finalMessage.append(inventoryComponent, ComponentBuilder.FormatRetention.NONE);
                    tmp = "";
                } catch (Exception e) {
                    FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                            "%task%", String.format("parsing [%s] in %s", tmp, message),
                            "%error%", e.getMessage());
                }
                break;
            }
        return tmp;
    }

    private String parseEnderChest(String tmp, Player player, ComponentBuilder finalMessage, String message) {
        for (String placeholder : ConfigOption.ENDER_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    TextComponent enderComponent = new TextComponent(ConfigOption.ENDER_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName()));
                    if (VersionsUtils.is1_9())
                        new ReflObject<>(enderComponent).callMethod("setHoverEvent",
                                TextComponentUtils.getTextHoverEvent(Message.SHOW_ENDER.getMessage(false)
                                        .replace("%player%", player.getDisplayName())));
                    enderComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/showenderchest %s", plugin.getGuiManager().addEnderChest(player))));
                    finalMessage.append(enderComponent, ComponentBuilder.FormatRetention.NONE);
                    tmp = "";
                } catch (Exception e) {
                    FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                            "%task%", String.format("parsing [%s] in %s", tmp, message),
                            "%error%", e.getMessage());
                }
                break;
            }
        return tmp;
    }

    private String parsePing(String tmp, Player player, ComponentBuilder finalMessage, String message) {
        for (String placeholder : ConfigOption.PING_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    TextComponent pingComponent = new TextComponent(ConfigOption.PING_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName())
                            .replace("%ping%", String.valueOf(player.getPing())));
                    finalMessage.append(pingComponent, ComponentBuilder.FormatRetention.NONE);
                    tmp = "";
                } catch (Exception e) {
                    FulmiChat.logWarning(LoggingMessage.GENERAL_ERROR_OCCURRED,
                            "%task%", String.format("parsing [%s] in %s", tmp, message),
                            "%error%", e.getMessage());
                }
                break;
            }
        return tmp;
    }
}