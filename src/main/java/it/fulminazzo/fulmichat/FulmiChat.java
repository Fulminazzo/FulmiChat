package it.fulminazzo.fulmichat;

import it.angrybear.Objects.ReflObject;
import it.angrybear.Objects.YamlElements.ItemStackYamlObject;
import it.angrybear.SimpleBearPlugin;
import it.angrybear.Utils.NumberUtils;
import it.angrybear.Utils.PluginsUtil;
import it.angrybear.Utils.StringUtils;
import it.fulminazzo.fulmichat.Commands.EmojiCommand;
import it.fulminazzo.fulmichat.Commands.ModCommand;
import it.fulminazzo.fulmichat.Listeners.PlayerListener;
import it.fulminazzo.fulmichat.Managers.EmojiGroupsManager;
import it.fulminazzo.graphics.Exceptions.InvalidSizeException;
import it.fulminazzo.graphics.Objects.GUI;
import it.fulminazzo.graphics.Objects.Items.AbstractClasses.GUIElement;
import it.fulminazzo.graphics.Objects.Items.CommandItem;
import it.fulminazzo.graphics.Objects.Items.Item;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FulmiChat extends SimpleBearPlugin {
    private static FulmiChat plugin;
    private EmojiGroupsManager emojiGroupsManager;

    @Override
    public void onEnable() {
        plugin = this;
        super.onEnable();
        getCommand("moderate").setExecutor(new ModCommand(this));
        getCommand("emoji").setExecutor(new EmojiCommand(this));
    }

    @Override
    public void loadAll() throws Exception {
        super.loadAll();
        this.emojiGroupsManager = new EmojiGroupsManager(getConfig().getConfigurationSection("emojis"));
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void loadConfig() throws Exception {
        loadGeneral("config.yml", false);
        reloadConfig();
    }

    public GUI getModerationGUI(Player issuer, Player target) {
        ConfigurationSection moderationGUISection = getConfig().getConfigurationSection("moderation-gui");
        if (moderationGUISection == null) return null;
        String title = formatString(moderationGUISection.getString("title"), issuer, target);

        List<GUIElement> contents = new ArrayList<>();
        ConfigurationSection itemsSection = moderationGUISection.getConfigurationSection("items");
        if (itemsSection == null) return null;
        for (String key : itemsSection.getKeys(false)) {
            if (!NumberUtils.isNatural(key)) continue;
            int slot = Integer.parseInt(key);
            while (contents.size() <= slot) contents.add(null);
            try {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;
                Item item = new Item(new ItemStackYamlObject().load(itemsSection, key));
                item.setDisplayName(formatString(item.getDisplayName(), issuer, target));
                List<String> lore = item.getLore();
                if (lore != null) lore = lore.stream()
                            .map(s -> formatString(s, issuer, target))
                            .collect(Collectors.toList());
                item.setLore(lore);
                String action = itemSection.getString("action");
                if (action != null && action.equalsIgnoreCase("close"))
                    item.setClickAction((p, g) -> p.closeInventory());
                String command = itemSection.getString("command");
                if (command != null)
                    contents.set(slot, new CommandItem(item, formatString(command, issuer, target)));
                else contents.set(slot, item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            GUI moderationGUI = new GUI((Math.max(contents.size() / 9, 1)) * 9, title);
            moderationGUI.fill(contents);
            return moderationGUI;
        } catch (InvalidSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getModerationCheck() {
        String check = getConfig().getString("moderation-gui.check");
        if (check == null) return "";
        return StringUtils.parseMessage(check);
    }

    private String formatString(String string, Player issuer, Player target) {
        if (string == null) string = "";
        return string
                .replace("%issuer%", issuer == null ? "null" : issuer.getName())
                .replace("%target%", target == null ? "null" : target.getName());
    }

    public EmojiGroupsManager getEmojiGroupsManager() {
        return emojiGroupsManager;
    }

    public static String formatPlaceholders(String string, Player player) {
        if (PluginsUtil.isPluginEnabled("PlaceholderAPI")) {
            ReflObject<?> placeholderAPI = new ReflObject<>("me.clip.placeholderapi.PlaceholderAPI", false);
            string = placeholderAPI.getMethodObject("setPlaceholders", player, string);
            string = placeholderAPI.getMethodObject("setBracketPlaceholders", player, string);
        }
        return string;
    }

    public static FulmiChat getPlugin() {
        return plugin;
    }
}