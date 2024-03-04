package me.steinsut.inventorymenuchanger;

import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.ApiConstants;
import me.steinsut.inventorymenuchanger.api.entry.EntryType;
import me.steinsut.inventorymenuchanger.api.impl.entry.SimpleChangerEntry;
import me.steinsut.inventorymenuchanger.event.handler.ClientEventHandler;
import me.steinsut.inventorymenuchanger.event.handler.ClientEventHandlerTest;
import me.steinsut.inventorymenuchanger.event.handler.ModLoadingEventHandler;
import me.steinsut.inventorymenuchanger.network.NetworkHandler;
import me.steinsut.inventorymenuchanger.registry.Registries;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static me.steinsut.inventorymenuchanger.api.ApiConstants.MOD_ID;

@Mod(ApiConstants.MOD_ID)
public class InventoryMenuChanger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private ClientEventHandlerTest clientEventHandler;
    private final ModLoadingEventHandler modLoadingEventHandler;

    public InventoryMenuChanger() {
        IEventBus minecraftBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.debug("Adding default inventory entry.");
        Registries.ENTRIES.addEntry(new SimpleChangerEntry(EntryType.SCREEN,
                InventoryScreen.class,
                new ResourceLocation(MOD_ID, "inventory"),
                "container.inventory",
                new ResourceLocation(MOD_ID, "textures/gui/inventory_button.png")));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            this.clientEventHandler = new ClientEventHandlerTest(this, minecraftBus);
            this.clientEventHandler.registerEvents();
        });

        this.modLoadingEventHandler = new ModLoadingEventHandler(this, modBus);
        this.modLoadingEventHandler.registerEvents();

        NetworkHandler.getInstance();
    }
}
