package me.steinsut.inventorymenuchanger.network.server;

import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.entry.EntryType;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import me.steinsut.inventorymenuchanger.registry.Registries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;

public class SPacketOpenScreen {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ResourceLocation entryLocation;

    public SPacketOpenScreen(ResourceLocation entryLocation) {
        this.entryLocation = entryLocation;
    }

    public static void encode(SPacketOpenScreen packet, FriendlyByteBuf buf) { buf.writeResourceLocation(packet.entryLocation); }

    public static SPacketOpenScreen decode(FriendlyByteBuf buf) { return new SPacketOpenScreen(buf.readResourceLocation()); }

    public static void handleOnClient(SPacketOpenScreen packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            handle(packet, ctxSupplier);
        });
    }

    private static void handle(SPacketOpenScreen packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if(player == null) {
                LOGGER.error("Player is null.");
                return;
            }

            Optional<IChangerEntry> entryOptional = Registries.ENTRIES.getEntry(packet.entryLocation);
            if(entryOptional.isEmpty()) {
                LOGGER.error("No entry found for location.");
                return;
            }

            IChangerEntry entry = entryOptional.get();
            if(entry.getType() != EntryType.SCREEN) {
                LOGGER.error("Entry is not a screen entry.");
                return;
            }

            Optional<Constructor<?>> ctorOptional = Registries.ENTRIES.getConstructor(entryOptional.get());
            Constructor<?> ctor = ctorOptional.get();
            Class<?>[] types = ctor.getParameterTypes();
            try {
                Screen screen = (Screen) ctor.newInstance(player);
                Minecraft.getInstance().setScreen(screen);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                LOGGER.error(e.getMessage());
            }
        });
        ctx.setPacketHandled(true);
    }
}
