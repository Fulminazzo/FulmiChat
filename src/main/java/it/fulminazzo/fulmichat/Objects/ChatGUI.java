package it.fulminazzo.fulmichat.Objects;

import it.fulminazzo.graphics.Objects.GUI;
import it.fulminazzo.graphics.Objects.Items.AbstractClasses.GUIElement;
import it.fulminazzo.graphics.Objects.Items.Item;
import it.fulminazzo.graphics.Utils.GUIUtils;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class ChatGUI {
    private final GUI gui;
    private final UUID uuid;

    public ChatGUI(Inventory inventory, UUID uuid) {
        this(new GUI(inventory), uuid);
    }

    public ChatGUI(GUI gui, UUID uuid) {
        this.gui = gui;
        this.uuid = uuid;
        if (gui == null) return;
        for (int i = 0; i < gui.getSize(); i++) {
            GUIElement item = gui.getItem(i);
            if (!(item instanceof Item)) continue;
            GUI shulkerGUI = GUIUtils.createShulkerBoxGUI((Item) item);
            if (shulkerGUI != null) ((Item) item).setClickAction((p, g) -> shulkerGUI.openGUI(p));
        }
    }

    public GUI getGui() {
        return gui;
    }

    public UUID getUuid() {
        return uuid;
    }
}