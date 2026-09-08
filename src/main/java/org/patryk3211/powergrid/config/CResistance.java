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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class CResistance extends ConfigBase implements ResistanceValues.Provider, ResettableValues, SyncedValues {
    private static final int VERSION = 3;

    private static final Object2DoubleMap<Identifier> DEFAULT_RESISTANCES = new Object2DoubleOpenHashMap<>();

    protected final Map<Identifier, DoubleRawValue> resistances = new HashMap<>();

    @Override
    public void registerAll(Builder builder) {
        builder.comment(Comments.ohm, Comments.resistance)
                .push("resistance");
        DEFAULT_RESISTANCES.forEach((id, value) -> this.resistances.put(id, builder.define(id.getPath(), value)));
        builder.pop();
    }

    @Override
    public String getName() {
        return "resistance-v" + VERSION;
    }

    @Nullable
    @Override
    public DoubleSupplier get(Block block) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        var entry = resistances.get(id);
        return entry == null ? null : entry::get;
    }

    @Nullable
    @Override
    public DoubleSupplier get(Block block, String suffix) {
        var id = BuiltInRegistries.BLOCK.getKey(block)
                .withSuffix("." + suffix);
        var entry = resistances.get(id);
        return entry == null ? null : entry::get;
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setResistance(double value) {
        return builder -> {
            assertFromPowerGrid(builder);
            var id = PowerGrid.asResource(builder.getName());
            DEFAULT_RESISTANCES.put(id, value);
            return builder;
        };
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setResistance(String suffix, double value) {
        return builder -> {
            assertFromPowerGrid(builder);
            var id = PowerGrid.asResource(builder.getName())
                    .withSuffix("." + suffix);
            DEFAULT_RESISTANCES.put(id, value);
            return builder;
        };
    }

    /**
     * This method expects arguments in pairs of two,
     * the first one should always be a string,
     * and the second one, a double.
     */
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setResistances(Object... suffixValuePairs) {
        return builder -> {
            assertFromPowerGrid(builder);
            for(int i = 0; i < suffixValuePairs.length; i += 2) {
                var suffix = (String) suffixValuePairs[i];
                var value = (Number) suffixValuePairs[i + 1];
                var id = PowerGrid.asResource(builder.getName())
                        .withSuffix("." + suffix);
                DEFAULT_RESISTANCES.put(id, value.doubleValue());
            }
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
        return Map.of("resistance", resistances);
    }

    @Override
    public void resetToDefaults() {
        resistances.values().forEach(ResettableValues::reset);
    }

    private static class Comments {
        static String ohm = "[in Ohms]";
        static String resistance = "Configure the individual resistances of electrical blocks";
    }
}
