package org.patryk3211.powergrid.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public interface C2SPacket {
    double MAX_INTERACTION_DISTANCE_SQUARED = 64 * 64;

    void write(FriendlyByteBuf buf);
    void handle(ServerPlayer player);

    static boolean canInteract(ServerPlayer player, BlockPos pos) {
        var level = player.level();
        if(!level.isLoaded(pos))
            return false;
        if(player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_INTERACTION_DISTANCE_SQUARED)
            return false;
        return player.mayInteract(level, pos);
    }

    static boolean canEdit(ServerPlayer player, BlockPos pos) {
        if(!canInteract(player, pos))
            return false;
        return mayEdit(player, pos);
    }

    static boolean mayEdit(ServerPlayer player, BlockPos pos) {
        if(player.isSpectator() || !player.mayBuild())
            return false;
        return player.level().mayInteract(player, pos);
    }

    static boolean mayEditHeldItem(ServerPlayer player) {
        return !player.isSpectator();
    }
}
