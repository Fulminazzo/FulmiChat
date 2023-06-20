package it.fulminazzo.fulmichat.Managers;

import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.Enums.Message;
import it.fulminazzo.fulmichat.Objects.ChatGUI;
import it.fulminazzo.graphics.Objects.GUI;
import it.fulminazzo.graphics.Objects.Items.Item;
import it.fulminazzo.graphics.Utils.GUIUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GUIManager {
    private final HashMap<ChatGUIType, List<ChatGUI>> guis;

    public GUIManager() {
        this.guis = new HashMap<>();
    }

    public GUI getGUI(ChatGUIType id, String uuid) {
        try {
            return getGUI(id, UUID.fromString(uuid));
        } catch (Exception e) {
            return null;
        }
    }

    public GUI getGUI(ChatGUIType id, UUID uuid) {
        if (uuid == null) return null;
        return getGUIs(id).stream().filter(c -> c.getUuid().equals(uuid)).map(ChatGUI::getGui).findAny().orElse(null);
    }

    public List<ChatGUI> getGUIs(ChatGUIType id) {
        return guis.getOrDefault(id, new ArrayList<>());
    }

    public UUID addInventory(Player player) {
        GUI gui = GUIUtils.createPlayerInventoryGUI(player);
        gui.setTitle(Message.INVENTORY_TITLE.getMessage(false).replace("%player%", player.getDisplayName()));
        Item expItem = new Item(Material.PAPER, Message.EXPERIENCE_ITEM.getMessage(false)
                .replace("%player%", player.getDisplayName())
                .replace("%level%", String.valueOf(player.getLevel())));
        gui.setItem(6, expItem);
        return addGeneral(ChatGUIType.INVENTORY, gui);
    }

    public UUID addEnderChest(Player player) {
        GUI gui = new GUI(player.getEnderChest());
        gui.setTitle(Message.ENDER_TITLE.getMessage(false).replace("%player%", player.getDisplayName()));
        return addGeneral(ChatGUIType.ENDER_CHEST, gui);
    }

    public UUID addGeneral(ChatGUIType id, GUI gui) {
        List<ChatGUI> list = getGUIs(id);
        UUID uuid = UUID.randomUUID();
        list.add(new ChatGUI(gui, uuid));
        guis.put(id, list);
        return uuid;
    }
}