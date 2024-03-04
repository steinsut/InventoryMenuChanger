package me.steinsut.inventorymenuchanger.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SPacketCarryItem {

    private final ItemStack itemStack;

    public SPacketCarryItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static void encode(SPacketCarryItem packet, FriendlyByteBuf buf) { buf.writeItem(packet.itemStack); }

    public static SPacketCarryItem decode(FriendlyByteBuf buf) { return new SPacketCarryItem(buf.readItem()); }

    public static void handleOnClient(SPacketCarryItem packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            handle(packet, ctxSupplier);
        });
    }

    private static void handle(SPacketCarryItem packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if(player != null) {
                player.containerMenu.setCarried(packet.itemStack);
            }
        });
        ctx.setPacketHandled(true);
    }
}
