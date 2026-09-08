/*
 * Copyright 2025 patryk3211
 * Modified 2026 by chaevsfe for the unofficial Fabric / Create Fly 26.2 port.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.patryk3211.powergrid.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.electricity.sim.node.OwnedFloatingNode;
import org.patryk3211.powergrid.electricity.wire.BlockWireEndpoint;
import org.patryk3211.powergrid.electricity.wire.CircuitBoardEndpoint;
import org.patryk3211.powergrid.electricity.wire.IWireEndpoint;
import org.patryk3211.powergrid.electricity.wire.WireEndpointType;
import org.patryk3211.powergrid.network.C2SPacket;

public class EndpointTrackingC2SPacket implements C2SPacket {
    private final IWireEndpoint endpoint;
    private final boolean end;

    public EndpointTrackingC2SPacket(OwnedFloatingNode node, boolean end) {
        endpoint = node.endpoint;
        this.end = end;
    }

    public EndpointTrackingC2SPacket(IWireEndpoint endpoint, boolean end) {
        this.endpoint = endpoint;
        this.end = end;
    }

    public EndpointTrackingC2SPacket(FriendlyByteBuf buf) {
        endpoint = WireEndpointType.deserialize(buf.readNbt());
        end = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(endpoint.serialize());
        buf.writeBoolean(end);
    }

    @Override
    public void handle(ServerPlayer player) {
        if(endpoint == null)
            return;
        var level = player.level();
        if(!end && !withinTrackingRange(player, level))
            return;
        var global = GlobalElectricNetworks.getWorldNetworks((LevelAccessor) level);
        if(global != null)
            global.tracking(player, endpoint, end);
    }

    private boolean withinTrackingRange(ServerPlayer player, ServerLevel level) {
        if(endpoint instanceof BlockWireEndpoint bwe && !level.isLoaded(bwe.getPos()))
            return false;
        if(endpoint instanceof CircuitBoardEndpoint cbe && !level.isLoaded(cbe.getPos()))
            return false;
        var pos = endpoint.getExactPosition(level);
        if(pos == null)
            return false;
        var range = (level.getServer().getPlayerList().getViewDistance() + 1) * 16.0;
        return player.distanceToSqr(pos) <= range * range;
    }
}
