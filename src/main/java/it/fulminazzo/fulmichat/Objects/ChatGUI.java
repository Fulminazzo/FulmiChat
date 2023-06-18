package it.fulminazzo.fulmichat.Objects;

import it.fulminazzo.graphics.Objects.GUI;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class ChatGUI {
    private final GUI gui;
    private final UUID uuid;

    public ChatGUI(Inventory inventory, UUID uuid) {
        this.gui = new GUI(inventory);
        this.uuid = uuid;
    }

    public GUI getGui() {
        return gui;
    }

    public UUID getUuid() {
        return uuid;
    }
}