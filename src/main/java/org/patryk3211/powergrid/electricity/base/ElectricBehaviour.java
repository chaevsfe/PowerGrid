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
package org.patryk3211.powergrid.electricity.base;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.electricity.sim.AbstractElectricWire;
import org.patryk3211.powergrid.electricity.sim.ElectricalNetwork;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;
import org.patryk3211.powergrid.electricity.sim.node.*;
import org.patryk3211.powergrid.electricity.sim.special.TransmissionLine;
import org.patryk3211.powergrid.electricity.sim.special.TransmissionLinePart;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;
import org.patryk3211.powergrid.electricity.wire.BlockWireEndpoint;
import org.patryk3211.powergrid.electricity.wire.HangingWireEntity;
import org.patryk3211.powergrid.network.packets.StateS2CPacket;

import java.util.*;
import java.util.function.Function;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

public class ElectricBehaviour extends BlockEntityBehaviour implements ISynchronizedElement {
    public static final BehaviourType<ElectricBehaviour> TYPE = new BehaviourType<>();

    private final IElectricEntity element;

    // Order of these lists should be the same on server and client.
    private final List<INode> internalNodes = new ArrayList<>();
    private final List<OwnedFloatingNode> externalNodes = new ArrayList<>();
    private final List<AbstractElectricWire> internalWires = new ArrayList<>();

    private final Map<BlockWireEndpoint, Set<BaseWireEntity>> connections = new HashMap<>();
    private boolean destroying = false;
    private byte rebuildOnClient = 0;
    private boolean removed = false;
    private boolean paused = true;
    private boolean reducedSync = false;
    public int inhibitSyncCount = 0;

    private int syncCount = 0;
    private int lastSyncCount = 0;

    @Nullable
    private SyncAppender syncAppender;

    public <T extends SmartBlockEntity & IElectricEntity> ElectricBehaviour(T be) {
        this(be, true);
    }

    protected <T extends SmartBlockEntity & IElectricEntity> ElectricBehaviour(T be, boolean buildCircuit) {
        super(be);
        this.element = be;
        if(buildCircuit) {
            var builder = new IElectricEntity.CircuitBuilder(getPos(), externalNodes, internalNodes, internalWires);
            element.buildCircuit(builder);
        }
    }

    public void reducedSync() {
        reducedSync = true;
    }

    public void joinNetwork(@NotNull ElectricalNetwork network, int externalIndex) {
        if(externalIndex < 0 || externalIndex >= externalNodes.size())
            return;
        var node = externalNodes.get(externalIndex);
        tracedAdd(network, node);
    }

    public void tracedAdd(ElectricalNetwork network, IElectricNode node) {
        addOrMerge(node, network);
        var list = new LinkedList<INode>();
        list.add(node);
        tracedAdd(list);
    }

    public void rebuildCircuit(boolean rebuildExternal) {
        var builder = new IElectricEntity.CircuitBuilder(getPos(), externalNodes, internalNodes, internalWires);
        var networkList = externalNodes.stream().map(INode::getNetwork).toList();
        if(rebuildExternal) {
            for (var endpointConnections : connections.values()) {
                for (var entity : endpointConnections) {
                    entity.dropWire();
                }
            }
        }
        builder.rebuildExternal(rebuildExternal);
        if(paused)
            builder.paused();
        builder.clear();
        element.buildCircuit(builder);
        // Make sure external (and internal) nodes are in the correct networks.
        for(int i = 0; i < networkList.size(); ++i) {
            if(networkList.get(i) == null)
                continue;
            joinNetwork(networkList.get(i), i);
        }

        if(rebuildExternal) {
            // Break connections if external node was removed.
            var iter = connections.entrySet().iterator();
            while (iter.hasNext()) {
                var entry = iter.next();
                var endpoint = entry.getKey();
                if (endpoint.getTerminal() < externalNodes.size() && externalNodes.get(endpoint.getTerminal()) != null) {
                    // Rewire
                    for (var entity : entry.getValue())
                        entity.makeWire();
                    continue;
                }
                var connCopy = List.copyOf(entry.getValue());
                for (BaseWireEntity entity : connCopy) {
                    entity.endpointRemoved(endpoint);
                }
                iter.remove();
            }
            if(getLevel() != null)
                GlobalElectricNetworks.nodeHolderAdded(this);
        }

        var world = getLevel();
        if(world != null && !world.isClientSide())
            rebuildOnClient = rebuildExternal ? (byte) 2 : (byte) 1;
    }

    public List<INode> getInternalNodes() {
        return internalNodes;
    }

    public List<OwnedFloatingNode> getExternalNodes() {
        return externalNodes;
    }

    public void pause() {
        if(!paused) {
            paused = true;
            element.paused();
            internalWires.forEach(AbstractElectricWire::remove);
            // Remove nodes in reverse order.
            for(int i = internalNodes.size() - 1; i >= 0; --i) {
                var node = internalNodes.get(i);
                node.remove();
            }
        }
    }

    private static void addOrMerge(IElectricNode node, ElectricalNetwork network) {
        if(node.getNetwork() == network)
            return;
        if(node.getNetwork() == null) {
            network.addNode(node);
            return;
        }
        network.merge(node.getNetwork());
    }

    public void tracedAdd(List<INode> scanNodes) {
        if(paused)
            return;
        var handled = new HashSet<INetworkElement>();
        while(!scanNodes.isEmpty()) {
            var node = scanNodes.remove(0);
            var network = node.getNetwork();
            if(network == null)
                continue;
            handled.add(node);
            for(var wire : internalWires) {
                if(wire.coupledNodes().contains(node)) {
                    // Wire connects here
                    if(handled.add(wire)) {
                        wire.coupledNodes().forEach(other -> {
                            addOrMerge(other, network);
                            if(handled.add(other))
                                scanNodes.add(other);
                        });
                        network.addWire(wire);
                    }
                }
            }
            for(var inode : internalNodes) {
                if(!(inode instanceof ICouplingNode coupling))
                    continue;
                if(coupling.coupledNodes().contains(node)) {
                    if(handled.add(coupling)) {
                        coupling.coupledNodes().forEach(other -> {
                            addOrMerge(other, network);
                            if(handled.add(other))
                                scanNodes.add(other);
                        });
                        network.addNode(coupling);
                    }
                }
            }
        }
    }

    public void unpause() {
        if(paused) {
            paused = false;
            // External nodes weren't removed so they don't have to be added.
            if(hasInternals()) {
                GlobalElectricNetworks.prepareUnpaused(this);
                tracedAdd(new LinkedList<>(externalNodes));
                element.unpaused();
            }
        }
    }

    @Override
    public void unload() {
        if(!removed) {
            pause();
            // Unload doesn't remove external nodes since they might be utilized by transmission lines.
            // Wires are not dropped either since they could be forming an important transmission line junction.
            GlobalElectricNetworks.nodeHolderUnloaded(this);
        }
    }

    public void remove() {
        breakConnections();
        pause();
        GlobalElectricNetworks.nodeHolderRemoved(this);
        removed = true;
    }

    public void refreshConnectionEntities() {
        for(var entry : connections.entrySet()) {
            for(var entity : entry.getValue()) {
                if(entity instanceof HangingWireEntity wire)
                    wire.refreshTerminalPositions();
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        GlobalElectricNetworks.nodeHolderAdded(this);
        unpause();
    }

    public void addConnection(BlockWireEndpoint endpoint, BaseWireEntity wire) {
        var sourceConnections = connections.computeIfAbsent(endpoint, key -> new HashSet<>());
        // Check for stale wires here
        sourceConnections.removeIf(Entity::isRemoved);
        sourceConnections.add(wire);
    }

    public void removeConnection(BlockWireEndpoint endpoint, BaseWireEntity wire) {
        if(!destroying && connections.containsKey(endpoint)) {
            var list = connections.get(endpoint);
            list.remove(wire);
            if(list.isEmpty())
                connections.remove(endpoint);
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Nullable
    public OwnedFloatingNode getTerminal(int index) {
        if(index >= externalNodes.size())
            return null;
        return externalNodes.get(index);
    }

    public boolean hasConnection(BlockWireEndpoint source, BlockWireEndpoint destination) {
        if(!connections.containsKey(source))
            return false;
        var sourceConnections = connections.get(source);
        for(var entity : sourceConnections) {
            if(entity.isConnectedTo(destination.getPos(), destination.getTerminal()))
                return true;
        }
        return false;
    }

    public Map<BlockWireEndpoint, Set<BaseWireEntity>> getConnections() {
        return connections;
    }

    public boolean hasTerminal(int terminal) {
        return terminal >= 0 && terminal < externalNodes.size() && externalNodes.get(terminal) != null;
    }

    public void breakConnections() {
        if(destroying)
            return;
        destroying = true;
        var world = getLevel();
        if(!world.isClientSide()) {
            for(var entry : connections.entrySet()) {
                var endpoint = entry.getKey();
                for(var entity : entry.getValue()) {
                    entity.endpointRemoved(endpoint);
                }
            }
            connections.clear();
        }
        destroying = false;
    }

    @Override
    public void read(ValueInput nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        if(clientPacket) {
            var level = nbt.getByteOr("Rebuild", (byte) 0);
            if(level > 0)
                rebuildCircuit(level > 1);
            var list = nbt.listOrEmpty("Nodes", Codec.FLOAT).stream().toList();
            int index = 0;
            for(var node : externalNodes) {
                float value = index < list.size() ? list.get(index) : 0.0f;
                ++index;
                node.setStateValue(value * 0.5f + node.getStateValue() * 0.5f);
            }
            for(var node : internalNodes) {
                float value = index < list.size() ? list.get(index) : 0.0f;
                ++index;
                node.setStateValue(value * 0.5f + node.getStateValue() * 0.5f);
            }
            if(syncCount == lastSyncCount) {
                // Server not sending data to client
                GlobalElectricNetworks.nodeHolderAdded(this);
            }
            lastSyncCount = syncCount;
        }
    }

    @Override
    public void write(ValueOutput nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        if(clientPacket) {
            if(rebuildOnClient != 0) {
                nbt.putByte("Rebuild", rebuildOnClient);
                rebuildOnClient = 0;
            }
            var list = nbt.list("Nodes", Codec.FLOAT);
            for(var node : externalNodes) {
                list.add((float) node.getStateValue());
            }
            for(var node : internalNodes) {
                list.add((float) node.getStateValue());
            }
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(paused)
            unpause();
    }

    public void inheritConnections(ElectricBehaviour otherBehaviour) {
        for(var entry : otherBehaviour.connections.entrySet()) {
            var thisList = connections.computeIfAbsent(entry.getKey(), key -> new HashSet<>());
            thisList.addAll(entry.getValue());
        }
        otherBehaviour.connections.clear();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean hasInternals() {
        return !internalNodes.isEmpty() || !internalWires.isEmpty();
    }

    public static void handleTicketChange(LevelChunk chunk, FullChunkStatus oldStatus, FullChunkStatus newStatus) {
        boolean wasTicking = oldStatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        boolean ticking = newStatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        if(!ticking && wasTicking) {
            for(var be : chunk.getBlockEntities().values()) {
                var electric = BlockEntityBehaviour.get(be, ElectricBehaviour.TYPE);
                if(electric == null)
                    continue;
                electric.pause();
            }
        } else if(ticking) {
            for(var be : chunk.getBlockEntities().values()) {
                var electric = BlockEntityBehaviour.get(be, ElectricBehaviour.TYPE);
                if(electric == null)
                    continue;
                electric.unpause();
            }
        }
    }

    public static void writeToBuffer(FriendlyByteBuf buffer, double value, boolean useDoubles) {
        if(useDoubles) {
            buffer.writeDouble(value);
        } else {
            buffer.writeFloat((float) value);
        }
    }

    public static double readFromBuffer(FriendlyByteBuf buffer, boolean useDoubles) {
        if(useDoubles) {
            return buffer.readDouble();
        } else {
            return buffer.readFloat();
        }
    }

    @Override
    public void writeToSync(FriendlyByteBuf buffer, boolean useDoubles, Function<OwnedFloatingNode, TransmissionLine> lineGetter) {
        var thermal = blockEntity.getBehaviour(ThermalBehaviour.TYPE);
        if(thermal != null) {
            buffer.writeFloat(thermal.getTemperature());
        }
        for (var node : externalNodes) {
            if (node.getNetwork() == null) {
                // Potentially part of a transmission line.
                var line = lineGetter.apply(node);
                writeToBuffer(buffer, line == null ? 0 : line.voltageFor(node), useDoubles);
            } else {
                writeToBuffer(buffer, node.getStateValue(), useDoubles);
            }
        }
        if(!reducedSync) {
            for (var node : internalNodes) {
                writeToBuffer(buffer, node.getStateValue(), useDoubles);
            }
            for (var wire : internalWires) {
                if (wire instanceof SwitchedWire switched)
                    buffer.writeBoolean(switched.getState());
            }
        }
        if(syncAppender != null)
            syncAppender.writeToSync(buffer);
    }

    @Override
    public void readFromSync(FriendlyByteBuf buffer, boolean useDoubles) {
        var thermal = blockEntity.getBehaviour(ThermalBehaviour.TYPE);
        if(thermal != null) {
            thermal.setTemperature(buffer.readFloat());
        }
        for (var node : externalNodes) {
            node.setStateValue(readFromBuffer(buffer, useDoubles));
        }
        if(!reducedSync) {
            for (var node : internalNodes) {
                node.setStateValue(readFromBuffer(buffer, useDoubles));
            }
            for (var wire : internalWires) {
                if (wire instanceof SwitchedWire switched)
                    switched.setState(buffer.readBoolean());
            }
        }
        if(syncAppender != null)
            syncAppender.readFromSync(buffer);
        ++syncCount;
    }

    @Override
    public boolean shouldSync() {
        if(inhibitSyncCount > 0) {
            --inhibitSyncCount;
            return false;
        }
        return true;
    }

    @Override
    public StateS2CPacket.Key getKey() {
        return new StateS2CPacket.PosKey(getPos());
    }

    public void setSyncAppender(@Nullable SyncAppender syncAppender) {
        this.syncAppender = syncAppender;
    }

    public Collection<TransmissionLinePart> wires() {
        return GlobalElectricNetworks.getWorldNetworks(getLevel()).findConnectedWires(this);
    }

    @Override
    public String toString() {
        return String.format("ElectricBehaviour(be=%s)", blockEntity);
    }

    public interface SyncAppender {
        void writeToSync(FriendlyByteBuf buffer);
        void readFromSync(FriendlyByteBuf buffer);
    }

    public record CompoundAppender(SyncAppender[] appenders) implements SyncAppender {
        public static CompoundAppender of(SyncAppender... appenders) {
            return new CompoundAppender(appenders);
        }

        @Override
        public void writeToSync(FriendlyByteBuf buffer) {
            for(var a : appenders) {
                a.writeToSync(buffer);
            }
        }

        @Override
        public void readFromSync(FriendlyByteBuf buffer) {
            for(var a : appenders) {
                a.readFromSync(buffer);
            }
        }
    }
}
