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
package org.patryk3211.powergrid.collections;

import com.zurrtum.create.client.AllCasings;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardModel;
import org.patryk3211.powergrid.electricity.battery.BatteryCTBehaviour;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelCTBehaviour;
import org.patryk3211.powergrid.general.ceilingtile.solar.CeilingTileSolarBlockCTBehaviour;

@Environment(EnvType.CLIENT)
public class ModdedModels {
    public static void register() {
        connectedTextures(ModdedBlocks.BATTERY.get(), new BatteryCTBehaviour());
        AllCasings.make(ModdedBlocks.CONDUCTIVE_CASING.get(), ModdedPartialModels.CONDUCTIVE_CASING);
        AllCasings.make(ModdedBlocks.COPPER_PLATING.get(), ModdedPartialModels.COPPER_PLATING);
        AllCasings.make(ModdedBlocks.COPPER_PLATING_STAIRS.get(), ModdedPartialModels.COPPER_PLATING, ModdedModels::stairsFaceConnects);
        AllCasings.make(ModdedBlocks.COPPER_PLATING_SLAB.get(), ModdedPartialModels.COPPER_PLATING, ModdedModels::slabFaceConnects);
        connectedTextures(ModdedBlocks.CONDUCTIVE_CASING.get(), new EncasedCTBehaviour(ModdedPartialModels.CONDUCTIVE_CASING));
        connectedTextures(ModdedBlocks.COPPER_PLATING.get(), new EncasedCTBehaviour(ModdedPartialModels.COPPER_PLATING));
        connectedTextures(ModdedBlocks.COPPER_PLATING_STAIRS.get(), new EncasedCTBehaviour(ModdedPartialModels.COPPER_PLATING));
        connectedTextures(ModdedBlocks.COPPER_PLATING_SLAB.get(), new EncasedCTBehaviour(ModdedPartialModels.COPPER_PLATING));
        connectedTextures(ModdedBlocks.SOLAR_PANEL.get(), new SolarPanelCTBehaviour());
        connectedTextures(ModdedBlocks.CEILING_TILE_SOLAR.get(), new CeilingTileSolarBlockCTBehaviour());
        AllModels.register(ModdedBlocks.CIRCUIT_BOARD.get(), CircuitBoardModel.of());
        BlockColorRegistry.register(CircuitBoardModel.tintSources(), ModdedBlocks.CIRCUIT_BOARD.get());
        AllFluidConfigs.model(ModdedFluids.ACID);
    }

    private static boolean stairsFaceConnects(BlockState state, Direction face) {
        var shape = state.getValue(StairBlock.SHAPE);
        var half = state.getValue(StairBlock.HALF);
        var facing = state.getValue(StairBlock.FACING);
        if(half == Half.TOP && face == Direction.UP)
            return true;
        if(half == Half.BOTTOM && face == Direction.DOWN)
            return true;
        if(facing == face)
            return true;
        if(shape == StairsShape.INNER_LEFT && face == facing.getCounterClockWise())
            return true;
        return shape == StairsShape.INNER_RIGHT && face == facing.getClockWise();
    }

    private static boolean slabFaceConnects(BlockState state, Direction face) {
        var type = state.getValue(SlabBlock.TYPE);
        if(type == SlabType.TOP && face == Direction.UP)
            return true;
        return type == SlabType.BOTTOM && face == Direction.DOWN;
    }

    private static void connectedTextures(Block block, ConnectedTextureBehaviour behaviour) {
        AllModels.register(block, CTModel.of(behaviour));
    }
}
