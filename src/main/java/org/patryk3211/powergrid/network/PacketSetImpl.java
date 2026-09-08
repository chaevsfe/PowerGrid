package org.patryk3211.powergrid.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Function;

public class PacketSetImpl extends PacketSet {
    protected PacketSetImpl(String id, int version,
                            List<Function<FriendlyByteBuf, S2CPacket>> s2cPackets,
                            Object2IntMap<Class<? extends S2CPacket>> s2cTypes,
                            List<Function<FriendlyByteBuf, C2SPacket>> c2sPackets,
                            Object2IntMap<Class<? extends C2SPacket>> c2sTypes) {
        super(id, version, s2cPackets, s2cTypes, c2sPackets, c2sTypes);
        PayloadTypeRegistry.clientboundPlay().register(payloadType(s2cPacket), PowerGridPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(payloadType(c2sPacket), PowerGridPayload.STREAM_CODEC);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void registerS2CListener() {
        ClientPlayNetworking.registerGlobalReceiver(payloadType(s2cPacket), (payload, context) ->
                handleS2CPacket(context.client(), payload.toBuffer()));
    }

    @Override
    public void registerC2SListener() {
        ServerPlayNetworking.registerGlobalReceiver(payloadType(c2sPacket), (payload, context) ->
                handleC2SPacket(context.player(), payload.toBuffer()));
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void doSendC2S(FriendlyByteBuf buf) {
        ClientPlayNetworking.send(new PowerGridPayload(payloadType(c2sPacket), copyBytes(buf)));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void send(Object packet) {
        Minecraft.getInstance().getConnection().send((Packet<?>) packet);
    }

    @Override
    public void sendTo(ServerPlayer player, Object packet) {
        player.connection.send((Packet<?>) packet);
    }

    @Override
    public void sendTo(PlayerSelection selection, Object packet) {
        if (selection instanceof PlayerSelectionImpl impl) {
            for (ServerPlayer player : impl.powergrid$getPlayers())
                player.connection.send((Packet<?>) packet);
        }
    }

    static CustomPacketPayload.Type<PowerGridPayload> payloadType(Identifier id) {
        return new CustomPacketPayload.Type<>(id);
    }

    private static byte[] copyBytes(FriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        return data;
    }

    public record PowerGridPayload(CustomPacketPayload.Type<PowerGridPayload> type, byte[] data) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, PowerGridPayload> STREAM_CODEC = StreamCodec.ofMember(
                PowerGridPayload::write,
                PowerGridPayload::new
        );

        private PowerGridPayload(RegistryFriendlyByteBuf buf) {
            this(payloadType(buf.readIdentifier()), buf.readByteArray());
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeIdentifier(type.id());
            buf.writeByteArray(data);
        }

        FriendlyByteBuf toBuffer() {
            return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        }
    }

    @ApiStatus.Internal
    public static PacketSet create(String id, int version,
                                   List<Function<FriendlyByteBuf, S2CPacket>> s2cPackets,
                                   Object2IntMap<Class<? extends S2CPacket>> s2cTypes,
                                   List<Function<FriendlyByteBuf, C2SPacket>> c2sPackets,
                                   Object2IntMap<Class<? extends C2SPacket>> c2sTypes) {
        return new PacketSetImpl(id, version, s2cPackets, s2cTypes, c2sPackets, c2sTypes);
    }
}
