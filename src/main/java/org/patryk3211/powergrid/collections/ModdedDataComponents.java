/*
 * Copyright 2026 patryk3211
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
package org.patryk3211.powergrid.collections;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.light.string.PatternData;
import org.patryk3211.powergrid.electricity.wire.WireConnection;
import org.patryk3211.powergrid.equipment.BoostData;

public class ModdedDataComponents {
    public static final DataComponentType<PatternData> LIGHT_PATTERN = persistent("pattern", PatternData.CODEC);
    public static final DataComponentType<WireConnection> CONNECTION_DATA = persistent("connection", WireConnection.CODEC);

    public static final DataComponentType<BoostData> BOOST = persistent("boost", BoostData.CODEC);

    public static final DataComponentType<BlockPos> WINDING_CONNECTION = persistent("winding_connection", BlockPos.CODEC);

    public static final DataComponentType<Integer> PORTABLE_BATTERY_CHARGE = persistent("portable_battery_charge", Codec.INT);

    public static <T> DataComponentType<T> persistent(String id, Codec<T> codec) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PowerGrid.asResource(id),
                DataComponentType.<T>builder().persistent(codec).build());
    }

    public static void register() {
    }
}
