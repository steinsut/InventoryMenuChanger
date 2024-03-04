package me.steinsut.inventorymenuchanger.api.entry;

import net.minecraft.resources.ResourceLocation;

public interface IChangerEntry {
    EntryType getType();
    Class<?> getGuiClass();
    ResourceLocation getEntryLocation();
    String getTranslationKey();
    ResourceLocation getIconLocation();
    boolean isEntryVisible();
    void onClick();
}
