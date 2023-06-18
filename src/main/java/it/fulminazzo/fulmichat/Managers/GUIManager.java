package it.fulminazzo.fulmichat.Managers;

import it.fulminazzo.fulmichat.Enums.ChatGUIType;
import it.fulminazzo.fulmichat.Objects.ChatGUI;
import it.fulminazzo.graphics.Objects.GUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

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
        return addGeneral(ChatGUIType.INVENTORY, player.getInventory());
    }

    public UUID addEnderChest(Player player) {
        return addGeneral(ChatGUIType.ENDER_CHEST, player.getEnderChest());
    }

    public UUID addGeneral(ChatGUIType id, Inventory inventory) {
        List<ChatGUI> list = getGUIs(id);
        UUID uuid = UUID.randomUUID();
        list.add(new ChatGUI(inventory, uuid));
        guis.put(id, list);
        return uuid;
    }
}