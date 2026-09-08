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

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.network.C2SPacket;
import org.patryk3211.powergrid.network.S2CPacket;
import org.patryk3211.powergrid.utility.ClientSideAccess;

public class UpdateComponentBiPacket implements S2CPacket, C2SPacket {
    @Nullable
    private final BlockPos pos;
    private final int componentId;
    private final Identifier propertyId;
    private final CompoundTag propertyValue;

    public UpdateComponentBiPacket(CircuitBoardBlockEntity be, PlacedComponent component, ComponentProperty<?> property) {
        pos = be.getBlockPos();
        componentId = be.getSchematic().getId(component);
        assert componentId >= 0;
        propertyId = property.id();
        propertyValue = new CompoundTag();
        component.getEntry(property).write(be.getLevel().registryAccess(), propertyValue);
    }

    public UpdateComponentBiPacket(CircuitBoardBlockEntity be, PlacedComponent component, Identifier propertyId) {
        pos = be.getBlockPos();
        componentId = be.getSchematic().getId(component);
        assert componentId >= 0;
        this.propertyId = propertyId;
        propertyValue = new CompoundTag();
        component.getEntry(propertyId).write(be.getLevel().registryAccess(), propertyValue);
    }

    public UpdateComponentBiPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        componentId = buf.readInt();
        propertyId = buf.readIdentifier();
        var tag = buf.readNbt();
        propertyValue = tag == null ? new CompoundTag() : tag;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(componentId);
        buf.writeIdentifier(propertyId);
        buf.writeNbt(propertyValue);
    }

    public void handle(Level world) {
        var be = world.getBlockEntity(pos, ModdedBlockEntities.CIRCUIT_BOARD.get());
        be.ifPresent(circuit -> {
            var components = circuit.getSchematic().components();
            if(componentId < 0 || componentId >= components.size())
                return;
            var placed = components.get(componentId);
            var entry = placed.findEntry(propertyId);
            if(entry == null)
                return;
            entry.read(world.registryAccess(), propertyValue);
            placed.stateUpdated();
            if(!world.isClientSide()) {
                // Server must broadcast this update to all clients
                placed.notifyClients(propertyId);
            }
        });
    }

    // Handle Server
    @Override
    public void handle(ServerPlayer player) {
        var world = player.level();
        if(!C2SPacket.canInteract(player, pos))
            return;
        handle(world);
    }

    // Handle Client
    @Override
    public void handle(Minecraft mc) {
        var world = ClientSideAccess.world();
        if(world == null)
            return;
        handle(world);
    }
}
