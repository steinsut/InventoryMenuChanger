package me.steinsut.inventorymenuchanger.event.handler;

import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.InventoryMenuChanger;
import me.steinsut.inventorymenuchanger.event.MenuEvent;
import me.steinsut.inventorymenuchanger.gui.EntryScrollPanelNew;
import me.steinsut.inventorymenuchanger.registry.Registries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

public class ClientEventHandlerTest {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final IEventBus minecraftBus;
    private final InventoryMenuChanger modInstance;
    private EntryScrollPanelNew panel;
    private Class<?> guiClass;

    public ClientEventHandlerTest(InventoryMenuChanger modInstance, IEventBus minecraftBus) {
        this.modInstance = modInstance;
        this.minecraftBus = minecraftBus;
    }

    public void registerEvents() {
        this.minecraftBus.addListener(this::onPostScreenInit);
        this.minecraftBus.addListener(this::onMouseDragged);
        this.minecraftBus.addListener(this::onScreenClose);
        this.minecraftBus.addListener(this::onMenuDisplay);
    }

    private void onMenuDisplay(MenuEvent.PreDisplay event) {
        this.guiClass = event.getMenu().getClass();
    }

    private void onPostScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if(this.guiClass == null) {
            this.guiClass = screen.getClass();
        }

        if (Registries.ENTRIES.getAllClasses().contains(this.guiClass)
            || this.guiClass == CreativeModeInventoryScreen.class) {
            LOGGER.debug("this is a certified inventory screen");

            int entryCount = Registries.ENTRIES.getAllEntries().size();
            int height = 146;
            if(entryCount < 7) {
                height = 20 * entryCount + 2 * Math.max(entryCount - 1, 0) + 4;
            }
            height = 146;

            this.panel = new EntryScrollPanelNew((screen.height / 2) - height / 2, 0);
            Registries.ENTRIES.getAllEntries().forEach((entry) -> {
                if(entry.isEntryVisible()) {
                    this.panel.addEntry(entry, entry.getGuiClass() == this.guiClass);
                }
            });
            event.addListener(this.panel);
        }
    }

    private void onScreenClose(ScreenEvent.Closing event) {
        this.panel = null;
        this.guiClass = null;
    }

    private void onMouseDragged(ScreenEvent.MouseDragged event) {
        if(this.panel != null) {
            this.panel.mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY());
        }
    }
}
