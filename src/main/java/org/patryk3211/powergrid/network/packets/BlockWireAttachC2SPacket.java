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

import org.patryk3211.powergrid.compat.sable.SableCompanion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedDataComponents;
import org.patryk3211.powergrid.electricity.wire.*;
import org.patryk3211.powergrid.network.C2SPacket;

public class BlockWireAttachC2SPacket implements C2SPacket {
    public final int entityId;
    public final int index;
    public final int gridPoint;

    public BlockWireAttachC2SPacket(BlockWireEntity entity, int index, int gridPoint) {
        this.entityId = entity.getId();
        this.index = index;
        this.gridPoint = gridPoint;
    }

    public BlockWireAttachC2SPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        index = buf.readInt();
        gridPoint = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(index);
        buf.writeInt(gridPoint);
    }

    @Override
    public void handle(ServerPlayer player) {
        var entity = player.level().getEntity(entityId);
        if(!(entity instanceof BlockWireEntity wire)) {
            PowerGrid.LOGGER.debug("Received block wire attach packet with invalid entity from {}", player.getName().getString());
            return;
        }
        if(wire.getBoundingBox().distanceToSqr(player.position()) > C2SPacket.MAX_INTERACTION_DISTANCE_SQUARED) {
            PowerGrid.LOGGER.debug("Received wire attach packet for an entity out of reach from {}", player.getName().getString());
            return;
        }
        if(!C2SPacket.mayEdit(player, wire.blockPosition())) {
            PowerGrid.LOGGER.debug("Received wire attach packet from a player who may not edit there {}", player.getName().getString());
            return;
        }
        var stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(!IWire.isWire(player.level(), stack.getItem())) {
            PowerGrid.LOGGER.debug("Received wire attach packet for player whose not holding a wire from {}", player.getName().getString());
            return;
        }
        if(index < 0 || index >= wire.segments.size()) {
            PowerGrid.LOGGER.debug("Received wire segment index out of bounds from {}", player.getName().getString());
            return;
        }
        var segment = wire.segments.get(index);
        // Align to grid.
        var gridLength = segment.gridLength;
        if(gridPoint < 0 || gridPoint > gridLength) {
            PowerGrid.LOGGER.debug("Received wire segment length out of bounds from {}", player.getName().getString());
            return;
        }

        var existingEndpoint = stack.getOrDefault(ModdedDataComponents.CONNECTION_DATA, WireConnection.EMPTY).endpoint();
        if(existingEndpoint != null && existingEndpoint.getSubLevel(player.level()) != SableCompanion.INSTANCE.getContaining(entity))
            return;

        IWireEndpoint endpoint;
        if(gridPoint <= 1 && index == 0) {
            // Extend wire at start.
            if(wire.getEndpoint1() == null) {
                wire = wire.flip();
                endpoint = new BlockWireEntityEndpoint(wire, true);
            } else {
                // Possibly a junction.
                endpoint = wire.getEndpoint1();
            }
        } else if(gridPoint >= segment.gridLength - 1 && index == wire.segments.size() - 1) {
            // Extend wire at end.
            if(wire.getEndpoint2() == null) {
                endpoint = new BlockWireEntityEndpoint(wire, true);
            } else {
                // Possibly a junction.
                endpoint = wire.getEndpoint2();
            }
        } else {
            // Junction.
            endpoint = new DeferredJunctionWireEndpoint(wire, index, gridPoint);
        }
        if(endpoint != null && existingEndpoint == null) {
            stack.set(ModdedDataComponents.CONNECTION_DATA, WireConnection.of(endpoint));
        } else if(endpoint != null) {
            var result = WireItem.connect(player.level(), stack, player, existingEndpoint, endpoint);
            if(result.getResult().consumesAction()) {
                stack.remove(ModdedDataComponents.CONNECTION_DATA);
            }
        }
    }
}
