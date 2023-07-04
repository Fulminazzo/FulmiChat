package it.fulminazzo.fulmichat.Listeners;

import it.angrybear.Bukkit.Utils.BukkitTextComponentUtils;
import it.angrybear.Exceptions.ExpectedPlayerException;
import it.angrybear.Objects.ReflObject;
import it.angrybear.Objects.UtilPlayer;
import it.angrybear.Utils.HexUtils;
import it.angrybear.Utils.StringUtils;
import it.angrybear.Utils.TextComponentUtils;
import it.angrybear.Utils.VersionsUtils;
import it.fulminazzo.fulmichat.API.FulmiChatPlayerEvent;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.Enums.ConfigOption;
import it.fulminazzo.fulmichat.Enums.LoggingMessage;
import it.fulminazzo.fulmichat.Enums.Message;
import it.fulminazzo.fulmichat.FulmiChat;
import it.fulminazzo.fulmichat.Objects.ChatMessage;
import it.fulminazzo.fulmichat.Objects.EmojiGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {
    protected final FulmiChat plugin;

    public PlayerListener(FulmiChat plugin) {
        this.plugin = plugin;
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
        TextComponent finalMessage;

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
        if (player.hasPermission(ChatPermission.MENTION.getPermission())) message = parseMention(message, player);
        if (player.hasPermission(ChatPermission.COLORED_CHAT.getPermission())) message = StringUtils.parseMessage(message).trim();
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
            if (p.hasPermission(ChatPermission.MOD.getPermission())) p.spigot().sendMessage(chatMessage.getModMessage());
            else p.spigot().sendMessage(chatMessage.getUserMessage());
        Bukkit.getConsoleSender().sendMessage(TextComponent.toLegacyText(chatMessage.getMessage()));
    }

    private TextComponent formatMessageToTextComponent(AsyncPlayerChatEvent event, TextComponent textComponent) {
        Player player = event.getPlayer();
        String[] format = StringUtils.parseMessage(event.getFormat()).replace("%1$s", player.getDisplayName()).split("%2\\$s");
        TextComponent finalMessage = new TextComponent("");
        for (String f : format) {
            Arrays.stream(TextComponent.fromLegacyText(f)).forEach(finalMessage::addExtra);
            if (!f.equals(format[format.length - 1])) finalMessage.addExtra(textComponent);
        }
        if (event.getFormat().endsWith("%2$s")) finalMessage.addExtra(textComponent);
        return finalMessage;
    }

    private String parseMention(String message, Player player) {
        for (Player p : Bukkit.getOnlinePlayers())
            if (!p.equals(player) && player.canSee(p)) {
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

    private TextComponent parsePlaceholders(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

        TextComponent finalMessage = new TextComponent("");
        boolean openBrackets = false;
        List<String> colorCodes = StringUtils.getCleanChatCodesInString(event.getFormat(), VersionsUtils.is1_16());
        String tmp = String.join("", colorCodes);
        for (String c : message.split("")) {
            if (openBrackets)
                if (c.equals("]")) {
                    openBrackets = false;
                    if (!tmp.equals("") && player.hasPermission(ChatPermission.ITEM.getPermission()) && itemStack != null)
                        tmp = parseItem(tmp, player, finalMessage, message);

                    if (!tmp.equals("") && player.hasPermission(ChatPermission.INVENTORY.getPermission()))
                        tmp = parseInventory(tmp, player, finalMessage, message);

                    if (!tmp.equals("") && player.hasPermission(ChatPermission.ENDER.getPermission()))
                        tmp = parseEnderChest(tmp, player, finalMessage, message);

                    if (!tmp.equals("") && player.hasPermission(ChatPermission.CHEST.getPermission())) {
                        String finalTmp = tmp;
                        try {
                            tmp = Bukkit.getScheduler().callSyncMethod(plugin, () -> parseChest(finalTmp, player, finalMessage, message)).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!tmp.equals("") && player.hasPermission(ChatPermission.PING.getPermission()))
                        tmp = parsePing(tmp, player, finalMessage, message);

                    if (!tmp.equals("")) {
                        tmp = String.format("%s[%s]", String.join("", colorCodes), tmp);
                        Arrays.stream(TextComponent.fromLegacyText(tmp)).forEach(finalMessage::addExtra);
                    }
                    tmp = String.join("", colorCodes);
                } else tmp += c;
            else
                if (c.equals("[")) {
                    openBrackets = true;
                    Arrays.stream(TextComponent.fromLegacyText(tmp)).forEach(finalMessage::addExtra);
                    if (player.hasPermission(ChatPermission.COLORED_CHAT.getPermission()))
                        colorCodes = StringUtils.getCleanChatCodesInString(HexUtils.unParseHexColor(finalMessage.toLegacyText()), VersionsUtils.is1_16())
                                .stream().map(StringUtils::parseMessage).collect(Collectors.toList());
                    else colorCodes = StringUtils.getCleanChatCodesInString(finalMessage.toLegacyText(), VersionsUtils.is1_16());
                    tmp = "";
                } else tmp += c;
        }
        if (!tmp.equals("")) Arrays.stream(TextComponent.fromLegacyText(tmp)).forEach(finalMessage::addExtra);
        return formatMessageToTextComponent(event, finalMessage);
    }

    private String parseItem(String tmp, Player player, TextComponent finalMessage, String message) {
        ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (itemStack == null) return tmp;
        for (String placeholder : ConfigOption.ITEM_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    TextComponent itemComponent = BukkitTextComponentUtils.getItemComponent(itemStack);
                    itemComponent.setText(ConfigOption.ITEM_PLACEHOLDER_PARSED.getMessage()
                            .replace("%item%", itemComponent.getText()));
                    UUID uuid = plugin.getGuiManager().addItem(player);
                    itemComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/showitem %s", uuid)));
                    finalMessage.addExtra(itemComponent);
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

    private String parseChest(String tmp, Player player, TextComponent finalMessage, String message) {
        Block block = player.getTargetBlock((Set<Material>) null, Bukkit.getServer().getViewDistance() * 16);
        if (!(block.getState() instanceof InventoryHolder)) return tmp;
        for (String placeholder : ConfigOption.CHEST_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    BaseComponent[] chestComponent = TextComponent.fromLegacyText(ConfigOption.CHEST_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName())
                            .replace("%container%", StringUtils.capitalize(block.getType().name())));
                    if (VersionsUtils.is1_9())
                        Arrays.stream(chestComponent).forEach(e ->
                                new ReflObject<>(e).callMethod("setHoverEvent",
                                        TextComponentUtils.getTextHoverEvent(Message.SHOW_CHEST.getMessage(false)
                                                .replace("%player%", player.getDisplayName())
                                                .replace("%container%", StringUtils.capitalize(block.getType().name())))));
                    UUID uuid = plugin.getGuiManager().addChest(player);
                    Arrays.stream(chestComponent).peek(e -> e.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/showchest %s", uuid))))
                            .forEach(finalMessage::addExtra);
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

    private String parseInventory(String tmp, Player player, TextComponent finalMessage, String message) {
        for (String placeholder : ConfigOption.INVENTORY_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    BaseComponent[] inventoryComponent = TextComponent.fromLegacyText(ConfigOption.INVENTORY_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName()));
                    if (VersionsUtils.is1_9())
                        Arrays.stream(inventoryComponent).forEach(e ->
                                new ReflObject<>(e).callMethod("setHoverEvent",
                                        TextComponentUtils.getTextHoverEvent(Message.SHOW_INVENTORY.getMessage(false)
                                                .replace("%player%", player.getDisplayName()))));
                    UUID uuid = plugin.getGuiManager().addInventory(player);
                    Arrays.stream(inventoryComponent).peek(e -> e.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/showinventory %s", uuid))))
                            .forEach(finalMessage::addExtra);
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

    private String parseEnderChest(String tmp, Player player, TextComponent finalMessage, String message) {
        for (String placeholder : ConfigOption.ENDER_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    BaseComponent[] enderComponent = TextComponent.fromLegacyText(ConfigOption.ENDER_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName()));
                    if (VersionsUtils.is1_9())
                        Arrays.stream(enderComponent).forEach(e ->
                                new ReflObject<>(e).callMethod("setHoverEvent",
                                        TextComponentUtils.getTextHoverEvent(Message.SHOW_ENDER.getMessage(false)
                                                .replace("%player%", player.getDisplayName()))));
                    UUID uuid = plugin.getGuiManager().addEnderChest(player);
                    Arrays.stream(enderComponent).peek(e -> e.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/showenderchest %s", uuid))))
                            .forEach(finalMessage::addExtra);
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

    private String parsePing(String tmp, Player player, TextComponent finalMessage, String message) {
        int playerPing = 0;
        try {
            playerPing = new UtilPlayer(player).getPing();
        } catch (ExpectedPlayerException e) {
            e.printStackTrace();
        }
        for (String placeholder : ConfigOption.PING_PLACEHOLDER.getStringList())
            if (String.format("[%s]", tmp).equalsIgnoreCase(placeholder)) {
                try {
                    Arrays.stream(TextComponent.fromLegacyText(ConfigOption.PING_PLACEHOLDER_PARSED.getMessage()
                            .replace("%player%", player.getDisplayName())
                            .replace("%ping%", String.valueOf(playerPing))))
                            .forEach(finalMessage::addExtra);
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