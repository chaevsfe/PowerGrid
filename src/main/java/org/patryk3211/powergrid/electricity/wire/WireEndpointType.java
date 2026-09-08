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
package org.patryk3211.powergrid.electricity.wire;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.patryk3211.powergrid.electricity.wire.powercord.AutoCordEndpoint;
import org.patryk3211.powergrid.electricity.wire.powercord.SocketEndpoint;
import org.patryk3211.powergrid.electricity.wire.powercord.SplitCordEndpoint;

import java.util.function.Supplier;

public enum WireEndpointType {
    BLOCK(BlockWireEndpoint::new, true),
    JUNCTION(JunctionWireEndpoint::new, true),
    BLOCK_WIRE(BlockWireEntityEndpoint::new, false),
    IMAGINARY(ImaginaryWireEndpoint::new, false),
    DEFERRED_JUNCTION(DeferredJunctionWireEndpoint::new, true),

    // Cord endpoint types
    SOCKET(SocketEndpoint::new),
    SPLIT_CORD(SplitCordEndpoint::new),
    AUTO_CORD(AutoCordEndpoint::new),

    // Special endpoint type used by the multimeter, appended last because this enum serializes by ordinal
    CIRCUIT_BOARD(CircuitBoardEndpoint::new)
    ;

    private final Supplier<IWireEndpoint> factory;
    // This is only used by the block wire placement code.
    private final boolean connectable;

    WireEndpointType(Supplier<IWireEndpoint> factory, boolean connectable) {
        this.factory = factory;
        this.connectable = connectable;
    }

    WireEndpointType(Supplier<IWireEndpoint> factory) {
        this(factory, false);
    }

    public boolean isConnectable() {
        return connectable;
    }

    public CompoundTag serialize(IWireEndpoint endpoint) {
        var tag = new CompoundTag();
        tag.putInt("Type", ordinal());
        endpoint.write(tag);
        return tag;
    }

    @Contract("null -> null")
    public static IWireEndpoint deserialize(CompoundTag tag) {
        if(tag == null)
            return null;
        if(!tag.contains("Type"))
            return null;
        var all = values();
        var index = tag.getIntOr("Type", -1);
        if(index < 0 || index >= all.length)
            return null;
        var type = all[index];
        var endpoint = type.factory.get();
        endpoint.read(tag);
        if(!endpoint.isComplete())
            return null;
        return endpoint;
    }
}
