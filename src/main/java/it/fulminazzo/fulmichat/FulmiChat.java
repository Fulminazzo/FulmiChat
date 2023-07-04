package it.fulminazzo.fulmichat;

import it.angrybear.Bukkit.Objects.YamlElements.ItemStackYamlObject;
import it.angrybear.Bukkit.SimpleBearPlugin;
import it.angrybear.Bukkit.Utils.BukkitUtils;
import it.angrybear.Objects.Configurations.Configuration;
import it.angrybear.Objects.ReflObject;
import it.angrybear.Utils.NumberUtils;
import it.angrybear.Utils.StringUtils;
import it.angrybear.Utils.VersionsUtils;
import it.fulminazzo.fulmichat.Commands.*;
import it.fulminazzo.fulmichat.Enums.ChatPermission;
import it.fulminazzo.fulmichat.Listeners.PlayerListener;
import it.fulminazzo.fulmichat.Listeners.PlayerListener1_12;
import it.fulminazzo.fulmichat.Managers.EmojiGroupsManager;
import it.fulminazzo.fulmichat.Managers.GUIManager;
import it.fulminazzo.graphics.Exceptions.InvalidSizeException;
import it.fulminazzo.graphics.Objects.GUI;
import it.fulminazzo.graphics.Objects.Items.AbstractClasses.GUIElement;
import it.fulminazzo.graphics.Objects.Items.CommandItem;
import it.fulminazzo.graphics.Objects.Items.Item;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class FulmiChat extends SimpleBearPlugin {
    private static FulmiChat plugin;
    private EmojiGroupsManager emojiGroupsManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        plugin = this;
        removeReloadSupport();
        setPermissionsClass(ChatPermission.class);
        super.onEnable();
        if (isEnabled()) {
            setExecutor("moderate", new ModCommand(this));
            setExecutor("emoji", new EmojiCommand(this));
            setExecutor("showitem", new ShowItem(this));
            setExecutor("showinventory", new ShowInventory(this));
            setExecutor("showenderchest", new ShowEnderChest(this));
            setExecutor("showchest", new ShowChest(this));
        }
    }

    private void setExecutor(String commandName, TabExecutor tabExecutor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) return;
        Arrays.stream(ChatPermission.values())
                .filter(p -> command.getName().replace("show", "").toLowerCase().contains(p.name().toLowerCase()))
                .map(ChatPermission::getPermission)
                .findAny().ifPresent(command::setPermission);
        command.setExecutor(tabExecutor);
    }

    @Override
    public void loadManagers() {
        super.loadManagers();
        this.emojiGroupsManager = new EmojiGroupsManager(getConfig().getConfigurationSection("emojis"));
        this.guiManager = new GUIManager();
    }

    @Override
    public void loadListeners() {
        super.loadListeners();
        if (VersionsUtils.is1_12()) Bukkit.getPluginManager().registerEvents(new PlayerListener1_12(this), this);
        else Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void loadConfig() throws Exception {
        loadGeneral("config.yml", false);
        reloadConfig();
    }

    @Override
    public void unloadAll() throws Exception {
        super.unloadAll();
        unloadPermissions(this);
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
                Item item = new Item(new ItemStackYamlObject().load(new Configuration(itemsSection), key));
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

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public static String formatPlaceholders(String string, Player player) {
        if (BukkitUtils.isPluginEnabled("PlaceholderAPI")) {
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
