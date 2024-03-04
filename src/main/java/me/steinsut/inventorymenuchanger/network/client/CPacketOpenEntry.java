package me.steinsut.inventorymenuchanger.network.client;

import com.mojang.logging.LogUtils;
import me.steinsut.inventorymenuchanger.api.entry.IChangerEntry;
import me.steinsut.inventorymenuchanger.network.NetworkHandler;
import me.steinsut.inventorymenuchanger.network.server.SPacketCarryItem;
import me.steinsut.inventorymenuchanger.network.server.SPacketOpenScreen;
import me.steinsut.inventorymenuchanger.registry.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;

public class CPacketOpenEntry {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ItemStack carried;
    private final ResourceLocation entryLocation;

    public CPacketOpenEntry(ItemStack carried, ResourceLocation entryLocation) {
        this.carried = carried;
        this.entryLocation = entryLocation;
    }

    public static void encode(CPacketOpenEntry packet, FriendlyByteBuf buf) {
        buf.writeItem(packet.carried);
        buf.writeResourceLocation(packet.entryLocation);
    }

    public static CPacketOpenEntry decode(FriendlyByteBuf buf) {
        return new CPacketOpenEntry(buf.readItem(), buf.readResourceLocation());
    }

    public static void handle(CPacketOpenEntry packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Optional<IChangerEntry> entryOptional = Registries.ENTRIES.getEntry(packet.entryLocation);
            if(entryOptional.isEmpty()) {
                LOGGER.error("No entry found for location.");
                return;
            }

            ServerPlayer player = ctx.getSender();
            if(player == null) {
                LOGGER.error("Player is null.");
                return;
            }

            ItemStack carried = packet.carried;
            IChangerEntry entry = entryOptional.get();
            player.containerMenu.setCarried(ItemStack.EMPTY);

            Optional<Constructor<?>> ctorOptional = Registries.ENTRIES.getConstructor(entryOptional.get());

            switch(entry.getType()) {
                case MENU -> {
                    NetworkHooks.openScreen(player, new SimpleMenuProvider((int i, Inventory iv, Player p) -> {
                        Constructor<?> ctor = ctorOptional.get();
                        Class<?>[] types = ctor.getParameterTypes();
                        try {
                            if (types.length == 2) {
                                return (AbstractContainerMenu) ctor.newInstance(i, iv);
                            } else {
                                return (AbstractContainerMenu) ctor.newInstance(i, iv, p);
                            }
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            LOGGER.error(e.getMessage());
                            return null;
                        }
                    }, Component.translatable(entryOptional.get().getTranslationKey())));
                }
                case SCREEN -> {
                    NetworkHandler.getInstance().sendToClientWithPlayer(new SPacketOpenScreen(entry.getEntryLocation()), player);
                    return;
                }
                default -> {
                    return;
                }
            }

            if(!carried.isEmpty()) {
                player.containerMenu.setCarried(carried);
                NetworkHandler.getInstance().sendToClientWithPlayer(new SPacketCarryItem(carried), player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
