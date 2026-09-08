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
package org.patryk3211.powergrid.config;

import org.patryk3211.powergrid.registrate.builders.BlockBuilder;
import org.patryk3211.powergrid.registrate.fn.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.DoubleRawValue;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * @see com.zurrtum.create.infrastructure.config.CStress
 */
public class CStress extends ConfigBase implements ResettableValues, SyncedValues {
    // bump this version to reset configured values.
    private static final int VERSION = 3;

    // IDs need to be used since configs load before registration

    private static final Object2DoubleMap<Identifier> DEFAULT_IMPACTS = new Object2DoubleOpenHashMap<>();
    private static final Object2DoubleMap<Identifier> DEFAULT_CAPACITIES = new Object2DoubleOpenHashMap<>();

    protected final Map<Identifier, DoubleRawValue> capacities = new HashMap<>();
    protected final Map<Identifier, DoubleRawValue> impacts = new HashMap<>();

    @Override
    public void registerAll(Builder builder) {
        builder.comment(Comments.su, Comments.impact)
                .push("impact");
        DEFAULT_IMPACTS.forEach((id, value) -> this.impacts.put(id, builder.define(id.getPath(), value)));
        builder.pop();

        builder.comment(Comments.su, Comments.capacity)
                .push("capacity");
        DEFAULT_CAPACITIES.forEach((id, value) -> this.capacities.put(id, builder.define(id.getPath(), value)));
        builder.pop();
    }

    @NotNull
    @Override
    public String getName() {
        return "stressValues.v" + VERSION;
    }

    @Nullable
    public DoubleSupplier getImpact(Block block) {
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(block);
        DoubleRawValue value = this.impacts.get(id);
        return value == null ? null : value::get;
    }

    @Nullable
    public DoubleSupplier getCapacity(Block block) {
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(block);
        DoubleRawValue value = this.capacities.get(id);
        return value == null ? null : value::get;
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setNoImpact() {
        return setImpact(0);
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(double value) {
        return builder -> {
            assertFromPowerGrid(builder);
            Identifier id = PowerGrid.asResource(builder.getName());
            DEFAULT_IMPACTS.put(id, value);
            return builder;
        };
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setCapacity(double value) {
        return builder -> {
            assertFromPowerGrid(builder);
            Identifier id = PowerGrid.asResource(builder.getName());
            DEFAULT_CAPACITIES.put(id, value);
            return builder;
        };
    }

    private static void assertFromPowerGrid(BlockBuilder<?, ?> builder) {
        if (!builder.getOwner().getModid().equals(PowerGrid.MOD_ID)) {
            throw new IllegalStateException("Non-Power Grid blocks cannot be added to Power Grid's config.");
        }
    }

    @Override
    public Map<String, Map<Identifier, DoubleRawValue>> syncedGroups() {
        return Map.of("impact", impacts, "capacity", capacities);
    }

    @Override
    public void resetToDefaults() {
        impacts.values().forEach(ResettableValues::reset);
        capacities.values().forEach(ResettableValues::reset);
    }

    private static class Comments {
        static String su = "[in Stress Units]";
        static String impact =
                "Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
        static String capacity = "Configure how much stress a source can accommodate for.";
    }

}
