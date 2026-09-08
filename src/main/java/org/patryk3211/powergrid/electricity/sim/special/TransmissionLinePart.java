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
package org.patryk3211.powergrid.electricity.sim.special;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.electricity.WorldNetworks;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;
import org.patryk3211.powergrid.electricity.sim.ElectricalNetwork;
import org.patryk3211.powergrid.electricity.sim.node.IElectricNode;
import org.patryk3211.powergrid.electricity.sim.node.OwnedFloatingNode;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;
import org.patryk3211.powergrid.electricity.wire.IWireEndpoint;
import org.patryk3211.powergrid.electricity.wire.WireEndpointType;

import java.util.Objects;

public class TransmissionLinePart extends ElectricWire {
    @Nullable
    private TransmissionLine line;
    @NotNull
    private final WorldNetworks global;

    @Nullable
    public BaseWireEntity owner;
    public final WorldNetworks.PartId persistentOwnerId;
    public ChunkPos lastKnownChunk;

    private TransmissionLinePart(double resistance, @NotNull IWireEndpoint endpoint1, @NotNull IWireEndpoint endpoint2, WorldNetworks.PartId ownerId, ChunkPos lastKnownChunk, @NotNull WorldNetworks global) {
        super(resistance, global.holderOrPlaceholderNode(endpoint1), global.holderOrPlaceholderNode(endpoint2));
        this.global = global;
        this.persistentOwnerId = ownerId;
        this.lastKnownChunk = lastKnownChunk;
        global.registerPart(persistentOwnerId, this);
    }

    private TransmissionLinePart(double resistance, @NotNull IWireEndpoint endpoint1, @NotNull IWireEndpoint endpoint2, @NotNull BaseWireEntity owner, @NotNull WorldNetworks global, WorldNetworks.PartId id) {
        super(resistance, global.holderOrPlaceholderNode(endpoint1), global.holderOrPlaceholderNode(endpoint2));
        this.global = global;
        this.owner = owner;
        this.persistentOwnerId = id;
        this.lastKnownChunk = ChunkPos.containing(owner.blockPosition());
        global.registerPart(persistentOwnerId, this);
    }

    @Nullable
    public static TransmissionLinePart uniquePart(CompoundTag tag, WorldNetworks global) {
        var owner = tag.read("Owner", UUIDUtil.CODEC).orElse(null);
        if(owner == null) {
            PowerGrid.LOGGER.error("Skipping a transmission line part with no owner id");
            return null;
        }
        WorldNetworks.PartId ownerId;
        if(tag.contains("ComplexOwner")) {
            ownerId = new WorldNetworks.ComplexId(owner, tag.getIntOr("ComplexOwner", 0));
        } else {
            ownerId = new WorldNetworks.SimpleId(owner);
        }
        var resistance = tag.getDoubleOr("Resistance", 0);
        var endpoint1 = WireEndpointType.deserialize(tag.getCompoundOrEmpty("Node1"));
        var endpoint2 = WireEndpointType.deserialize(tag.getCompoundOrEmpty("Node2"));
        if(endpoint1 == null || endpoint2 == null) {
            PowerGrid.LOGGER.error("Skipping transmission line part {} with an unreadable endpoint", owner);
            return null;
        }
        var lastKnownChunk = new ChunkPos(tag.getIntOr("X", 0), tag.getIntOr("Z", 0));
        var part = global.getPart(ownerId);
        if(part == null)
            return new TransmissionLinePart(resistance, endpoint1, endpoint2, ownerId, lastKnownChunk, global);
        return part;
    }

    public static TransmissionLinePart uniquePart(double resistance, @NotNull IWireEndpoint endpoint1, @NotNull IWireEndpoint endpoint2, BaseWireEntity owner, @NotNull WorldNetworks global, WorldNetworks.PartId id) {
        var part = global.getPart(id);
        if(part == null)
            return new TransmissionLinePart(resistance, endpoint1, endpoint2, owner, global, id);
        return part;
    }

    @Override
    public void setNode1(IElectricNode node1) {
        assert node1 instanceof OwnedFloatingNode;
        super.setNode1(Objects.requireNonNull(node1));
    }

    @Override
    public void setNode2(IElectricNode node2) {
        assert node2 instanceof OwnedFloatingNode;
        super.setNode2(Objects.requireNonNull(node2));
    }

    @Override
    public OwnedFloatingNode getNode1() {
        return (OwnedFloatingNode) node1;
    }

    @Override
    public OwnedFloatingNode getNode2() {
        return (OwnedFloatingNode) node2;
    }

    @NotNull
    public IWireEndpoint getEndpoint1() {
        return getNode1().endpoint;
    }

    @NotNull
    public IWireEndpoint getEndpoint2() {
        return getNode2().endpoint;
    }

    @Nullable
    public TransmissionLine getLine() {
        return line;
    }

    public void setLine(@Nullable TransmissionLine line) {
        this.line = line;
    }

    public void unload() {
        assert owner != null : "Node already unloaded";
        if(ModdedConfigs.logsEnabled())
            PowerGrid.LOGGER.debug("{}: Unloading part, UUID={}, chunk={}", line, persistentOwnerId, lastKnownChunk);
        lastKnownChunk = ChunkPos.containing(owner.blockPosition());
        global.bounty(persistentOwnerId, lastKnownChunk);
        owner = null;
    }

    public void grab(BaseWireEntity forEntity, WorldNetworks.PartId id) {
        if(persistentOwnerId.equals(id)) {
            owner = forEntity;
            if (line != null)
                line.grabPart(forEntity, this);
        } else {
            PowerGrid.LOGGER.warn("Entity tried to grab a part which it does not own, part: {}, entity: {}", this, forEntity);
        }
    }

    // Transmission line part can NEVER be directly in a network.
    @Override
    public void setNetwork(ElectricalNetwork network) {
        throw new IllegalCallerException();
    }

    @Override
    public void remove() {
        if(ModdedConfigs.logsEnabled())
            PowerGrid.LOGGER.debug("Removing {}", this);
        if(line != null)
            line.remove(this);
        global.unregisterPart(persistentOwnerId, this);
    }

    @Override
    public double potentialDifference() {
        if(line == null)
            return 0;
        return line.current() * getResistance();
    }

    @Override
    public double current() {
        if(line == null)
            return 0;
        return line.current();
    }

    @Override
    public boolean isConverged() {
        if(line == null)
            return false;
        return line.isConverged();
    }

    @Override
    public String toString() {
        return String.format("LinePart[id=%s, %s, %s]", persistentOwnerId, getEndpoint1(), getEndpoint2());
    }

    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("Node1", getEndpoint1().serialize());
        tag.put("Node2", getEndpoint2().serialize());
        if(persistentOwnerId instanceof WorldNetworks.SimpleId id) {
            tag.store("Owner", UUIDUtil.CODEC, id.id());
        } else if(persistentOwnerId instanceof WorldNetworks.ComplexId id) {
            tag.store("Owner", UUIDUtil.CODEC, id.id());
            tag.putInt("ComplexOwner", id.sub());
        }
        if(owner != null)
            lastKnownChunk = ChunkPos.containing(owner.blockPosition());
        tag.putInt("X", lastKnownChunk.x());
        tag.putInt("Z", lastKnownChunk.z());
        tag.putDouble("Resistance", resistance);
        return tag;
    }

    public void refreshEndpointNodes() {
        var node1 = getEndpoint1().getNode(global.world);
        if(this.node1 != node1 && node1 != null) {
            global.addAndMigrateNode(getNode1().endpoint, node1);
            setNode1(node1);
        }
        var node2 = getEndpoint2().getNode(global.world);
        if(this.node2 != node2 && node2 != null) {
            global.addAndMigrateNode(getNode2().endpoint, node2);
            setNode2(node2);
        }
    }
}
