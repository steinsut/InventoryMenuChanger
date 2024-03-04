package me.steinsut.inventorymenuchanger.api.impl.entry;

import me.steinsut.inventorymenuchanger.api.entry.EntryType;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import net.minecraft.resources.ResourceLocation;

public class SimpleChangerEntry implements IChangerEntry {
    private final EntryType type;
    private final Class<?> guiClass;
    private final ResourceLocation location;
    private final String translationKey;
    private final ResourceLocation iconLocation;

    public SimpleChangerEntry(EntryType type, Class<?> guiClass, ResourceLocation location, String translationKey, ResourceLocation iconLocation) {
        this.type = type;
        this.guiClass = guiClass;
        this.location = location;
        this.translationKey = translationKey;
        this.iconLocation = iconLocation;
    }

    @Override
    public EntryType getType() {
        return this.type;
    }

    @Override
    public Class<?> getGuiClass() {
        return this.guiClass;
    }

    @Override
    public ResourceLocation getEntryLocation() {
        return this.location;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public ResourceLocation getIconLocation() {
        return this.iconLocation;
    }

    @Override
    public boolean isEntryVisible() {
        return true;
    }

    @Override
    public void onClick() {

    }
}
