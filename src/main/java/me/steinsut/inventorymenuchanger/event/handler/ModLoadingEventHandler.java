package me.steinsut.inventorymenuchanger.event.handler;

import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.InventoryMenuChanger;
import me.steinsut.inventorymenuchanger.api.ApiConstants;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import me.steinsut.inventorymenuchanger.registry.EntryRegistry;
import me.steinsut.inventorymenuchanger.registry.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.slf4j.Logger;

import java.util.stream.Stream;

public class ModLoadingEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final IEventBus modBus;

    private final InventoryMenuChanger modInstance;

    public ModLoadingEventHandler(InventoryMenuChanger modInstance, IEventBus modBus) {
        this.modInstance = modInstance;
        this.modBus = modBus;
    }

    public void registerEvents() {
        this.modBus.addListener(this::onIMCProcess);
    }

    private void onIMCProcess(InterModProcessEvent event) {
        EntryRegistry registry = Registries.ENTRIES;

        Stream<InterModComms.IMCMessage> messages = InterModComms.getMessages(ApiConstants.MOD_ID);
        messages.forEachOrdered((message) -> {
            if (message.method().equals(ApiConstants.IMC_ADD_ENTRY)) {
                if (!(message.messageSupplier().get() instanceof IChangerEntry entry)) {
                    LOGGER.error("Message object does not implement IScreenEntry");
                    return;
                }
                LOGGER.debug("Adding entry for gui class: " + entry.getGuiClass().getName());
                registry.addEntry(entry);
            }
        });
        registry.freeze();
    }
}
