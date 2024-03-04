package me.steinsut.inventorymenuchanger.network;

import me.steinsut.inventorymenuchanger.api.ApiConstants;
import me.steinsut.inventorymenuchanger.network.client.CPacketOpenEntry;
import me.steinsut.inventorymenuchanger.network.server.SPacketCarryItem;
import me.steinsut.inventorymenuchanger.network.server.SPacketOpenInventory;
import me.steinsut.inventorymenuchanger.network.server.SPacketOpenScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    private static NetworkHandler INSTANCE;

    private final SimpleChannel channel;
    private int packetId = 0;

    private NetworkHandler() {
        this.channel = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ApiConstants.MOD_ID, "channel"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        registerPacket(CPacketOpenEntry.class, CPacketOpenEntry::encode, CPacketOpenEntry::decode, CPacketOpenEntry::handle);
        registerPacket(SPacketCarryItem.class, SPacketCarryItem::encode, SPacketCarryItem::decode, SPacketCarryItem::handleOnClient);
        registerPacket(SPacketOpenInventory.class, SPacketOpenInventory::encode, SPacketOpenInventory::decode, SPacketOpenInventory::handleOnClient);
        registerPacket(SPacketOpenScreen.class, SPacketOpenScreen::encode, SPacketOpenScreen::decode, SPacketOpenScreen::handleOnClient);
    }

    public static NetworkHandler getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NetworkHandler();
        }

        return INSTANCE;
    }

    private <P> void registerPacket(Class<P> packetType,
                                    BiConsumer<P, FriendlyByteBuf> packetEncoder,
                                    Function<FriendlyByteBuf, P> packetDecoder,
                                    BiConsumer<P, Supplier<NetworkEvent.Context>> packetHandler) {
        this.channel.registerMessage(this.packetId++, packetType, packetEncoder, packetDecoder, packetHandler);
    }

    public void sendToServer(Object packet) {
        this.channel.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public void sendToClientWithPlayer(Object packet, ServerPlayer player) {
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
