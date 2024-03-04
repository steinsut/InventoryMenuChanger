package me.steinsut.inventorymenuchanger.event;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.Event;

public abstract class MenuEvent extends Event {

    private final AbstractContainerMenu menu;
    private final MenuType<?> menuId;
    private final int containerId;

    protected MenuEvent(AbstractContainerMenu menu, MenuType<?> menuId, int containerId) {
        this.menu = menu;
        this.menuId = menuId;
        this.containerId = containerId;
    }

    public AbstractContainerMenu getMenu() {
        return this.menu;
    }

    public MenuType<?> getMenuType() {
        return this.menuId;
    }

    public int getContainerId() {
        return this.containerId;
    }

    public static class PreDisplay extends MenuEvent {
        public PreDisplay(AbstractContainerMenu menu, MenuType<?> menuId, int containerId) {
            super(menu, menuId, containerId);
        }
    }

}
