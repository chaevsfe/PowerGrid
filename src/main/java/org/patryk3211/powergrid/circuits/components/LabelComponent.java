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
package org.patryk3211.powergrid.circuits.components;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.EnumProperty;
import org.patryk3211.powergrid.circuits.components.properties.IntProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class LabelComponent extends Component implements IRenderedComponent, IGoggleLabel {
    public static final EnumProperty<DyeColor> COLOR = new EnumProperty<DyeColor>(PowerGrid.MOD_ID, "color", DyeColor.class, DyeColor.values(), DyeColor.BLACK);
    public static final IntProperty SIZE = new IntProperty(PowerGrid.MOD_ID, "size", 3, 3, 5).hidden().cast();

    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 5;

    public static final int[] SEGMENT_TINT = {255, 255, 235, 240, 245};

    public LabelComponent(ComponentFootprint footprint) {
        super(footprint);
    }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(LABEL, SIZE, COLOR);
    }

    @Override
    public boolean rotate(@NotNull PlacedComponent placed, boolean counterClockwise) {
        if (counterClockwise) {
            if (placed.get(SIZE) == MIN_SIZE) {
                placed.set(SIZE, MAX_SIZE);
            } else {
                placed.set(SIZE, placed.get(SIZE) - 1);
            }
        } else {
            if (placed.get(SIZE) == MAX_SIZE) {
                placed.set(SIZE, MIN_SIZE);
            } else {
                placed.set(SIZE, placed.get(SIZE) + 1);
            }
        }
        return true;
    }

    @Override
    public ComponentFootprint footprint(@Nullable PlacedComponent placed) {
        var footprint = super.footprint(placed);
        if (placed == null)
            return footprint;
        return new ComponentFootprint.Builder(placed.get(SIZE), 1).withOutline().build();
    }

    @Override
    public void bake(@NotNull PlacedComponent placed, @NotNull ComponentCircuitBuilder builder, ThermalBuilder.@NotNull IEmitter thermals) {
    
    }


}
