package me.steinsut.inventorymenuchanger.registry;

import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.entry.EntryType;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.*;

public class EntryRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, IChangerEntry> entries;
    private final Map<ResourceLocation, Constructor<?>> entryConstructors;
    private final List<Class<?>> entryClasses;
    private boolean frozen;

    public EntryRegistry() {
        this.entries = new HashMap<>();
        this.entryConstructors = new LinkedHashMap<>();
        this.entryClasses = new ArrayList<>();
        this.frozen = false;
    }

    public void addEntry(IChangerEntry entry) {
        if(this.frozen) return;

        if(entry.getType() == EntryType.MENU) {
            addMenuEntry(entry);
        }
        else {
            addScreenEntry(entry);
        }
    }

    private void addMenuEntry(IChangerEntry entry) {
        Class<?> entryClass = entry.getGuiClass();
        if(!AbstractContainerMenu.class.isAssignableFrom(entryClass)) {
            LOGGER.error("Gui class is not a subclass of AbstractContainerMenu.");
            return;
        }

        Constructor<?> ctor = null;
        Class<?>[][] parameterLists = {
                {Integer.TYPE, Inventory.class},
                {Integer.TYPE, Inventory.class, Player.class},
        };
        int i = 1;
        for (Class<?>[] parameterList : parameterLists) {
            try {
                ctor = entryClass.getConstructor(parameterList);
                break;
            } catch (NoSuchMethodException ignored) {
                LOGGER.error("Constructor " + i + " not found.");
            }
            i++;
        }
        if(ctor == null) {
            LOGGER.error("No suitable constructor found for menu. Searched for (int, Inventory), (int, Inventory, Player)");
        }
        else {
            this.entries.put(entry.getEntryLocation(), entry);
            this.entryConstructors.put(entry.getEntryLocation(), ctor);
            this.entryClasses.add(entry.getGuiClass());
        }
    }

    private void addScreenEntry(IChangerEntry entry) {
        Class<?> entryClass = entry.getGuiClass();
        if(!Screen.class.isAssignableFrom(entryClass)) {
            LOGGER.error("Gui class is not a subclass of Screen.");
            return;
        }

        Constructor<?> ctor = null;
        Class<?>[][] parameterLists = {
                {Player.class},
        };
        int i = 1;
        for (Class<?>[] parameterList : parameterLists) {
            try {
                ctor = entryClass.getConstructor(parameterList);
                break;
            } catch (NoSuchMethodException ignored) {
                LOGGER.error("Constructor " + i + " not found.");
            }
            i++;
        }
        if(ctor == null) {
            LOGGER.error("No suitable constructor found for screen. Searched for (Player)");
        }
        else {
            this.entries.put(entry.getEntryLocation(), entry);
            this.entryConstructors.put(entry.getEntryLocation(), ctor);
            this.entryClasses.add(entry.getGuiClass());
        }
    }

    public boolean hasEntry(IChangerEntry entry) {
        return this.entries.containsKey(entry.getEntryLocation());
    }

    public Optional<IChangerEntry> getEntry(ResourceLocation entryLocation) {
        return Optional.ofNullable(this.entries.get(entryLocation));
    }

    public List<IChangerEntry> getAllEntries() {
        return this.entries.values().stream().toList();
    }

    public Optional<Constructor<?>> getConstructor(IChangerEntry entry) {
        return Optional.ofNullable(this.entryConstructors.get(entry.getEntryLocation()));
    }

    public Optional<Constructor<?>> getConstructor(ResourceLocation entryLocation) {
        return Optional.ofNullable(this.entryConstructors.get(entryLocation));
    }

    public List<Class<?>> getAllClasses() {
        return Collections.unmodifiableList(this.entryClasses);
    }

    public void freeze() {
        this.frozen = true;
    }
    public void unfreeze() {
        this.frozen = false; }
    public boolean isFrozen() { return this.frozen; }
}
