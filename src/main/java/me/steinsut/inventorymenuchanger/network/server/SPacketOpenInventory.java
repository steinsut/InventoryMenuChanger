package me.steinsut.inventorymenuchanger.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SPacketOpenInventory {
    public SPacketOpenInventory() {}

    public static void encode(SPacketOpenInventory packet, FriendlyByteBuf buf) {}

    public static SPacketOpenInventory decode(FriendlyByteBuf buf) { return new SPacketOpenInventory(); }

    public static void handleOnClient(SPacketOpenInventory packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            handle(packet, ctxSupplier);
        });
    }

    private static void handle(SPacketOpenInventory packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if(player != null) {
                ItemStack carried = player.containerMenu.getCarried();
                InventoryScreen inventory = new InventoryScreen(player);
                minecraft.setScreen(inventory);
                player.containerMenu.setCarried(carried);
            }
        });
        ctx.setPacketHandled(true);
    }
}
