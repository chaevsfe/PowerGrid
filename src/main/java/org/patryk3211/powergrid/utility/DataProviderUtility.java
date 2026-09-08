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
package org.patryk3211.powergrid.utility;

import org.patryk3211.powergrid.registrate.data.DataGenContext;
import org.patryk3211.powergrid.registrate.data.RegistrateBlockstateProvider;
import org.patryk3211.powergrid.registrate.data.RegistrateItemModelProvider;
import org.patryk3211.powergrid.registrate.fn.NonNullBiConsumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.electricswitch.HvSwitchBlock;
import org.patryk3211.powergrid.electricity.electricswitch.SwitchBlock;
import org.patryk3211.powergrid.electricity.fuse.FuseHolderBlock;
import org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightBlock;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlock;
import org.patryk3211.powergrid.electricity.transformer.NetherTransformerBlock;
import org.patryk3211.powergrid.electricity.transformer.TransformerMediumBlock;
import org.patryk3211.powergrid.electricity.transformer.TransformerSmallBlock;
import org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampBlock;
import org.patryk3211.powergrid.kinetics.base.TunedBlock;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.VerticalCommutatorBlock;
import org.patryk3211.powergrid.kinetics.generator.rotor.AbstractRotorBlock;
import org.patryk3211.powergrid.kinetics.generator.winding.WindingBlock;

import java.util.function.Function;

public class DataProviderUtility {
    private static <O, T extends O, P> NonNullBiConsumer<DataGenContext<O, T>, P> none() {
        return (context, provider) -> { };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlock(String model) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlock(Function<BlockState, String> model) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> alternateDirectionalBlock(String model) {
        return alternateDirectionalBlock($ -> model);
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> alternateDirectionalBlock(Function<BlockState, String> modelProvider) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalAxisBlock(String model) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> surfaceSwitch(String baseName) {
        return surfaceBlock(state -> baseName + (state.getValue(SwitchBlock.OPEN) ? "_off" : "_on"));
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> surfaceBlock(String baseName) {
        return surfaceBlock(state -> baseName);
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> surfaceBlock(Function<BlockState, String> baseName) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> tunedBlock(String based, String baseless) {
        return horizontalBlock(state -> state.getValue(TunedBlock.BASE) ? based : baseless);
    }

    public static NonNullBiConsumer<DataGenContext<Block, LightFixtureBlock>, RegistrateBlockstateProvider> lightFixture(String baseName) {
        return none();
    }

    public static NonNullBiConsumer<DataGenContext<Block, FactoryLightBlock>, RegistrateBlockstateProvider> factoryLight(String baseFolder) {
        return none();
    }

    public static NonNullBiConsumer<DataGenContext<Block, CeilingTileLampBlock>, RegistrateBlockstateProvider> ceilingTileLamp(String baseFolder) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> housing(String name) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> basinHeater(String baseName) {
        return none();
    }

    public static <T extends AbstractRotorBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> rotorModel(String name) {
        return none();
    }

    public static <T extends VerticalCommutatorBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> verticalCommutator(String name) {
        return none();
    }

    public static <T extends WindingBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> windingModel() {
        return none();
    }

    public static <T extends HvSwitchBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> hvSwitch(String baseName) {
        return none();
    }

    public static NonNullBiConsumer<DataGenContext<Block, TransformerSmallBlock>, RegistrateBlockstateProvider> transformerSmall() {
        return none();
    }

    public static NonNullBiConsumer<DataGenContext<Block, TransformerMediumBlock>, RegistrateBlockstateProvider> transformerMedium() {
        return none();
    }

    public static NonNullBiConsumer<DataGenContext<Block, NetherTransformerBlock>, RegistrateBlockstateProvider> transformerNether() {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> downFacing(String name) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> upFacing(String name) {
        return none();
    }

    public static NonNullBiConsumer<DataGenContext<Block, FuseHolderBlock>, RegistrateBlockstateProvider> fuseHolder() {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cubeColumn(String side, String end) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cubeAllWithItem(String name) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> simple(String model) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> carbonPile(String baseName) {
        return none();
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> generated() {
        return none();
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> gauge(String model, String texture) {
        return none();
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> itemWithParent(String parent) {
        return none();
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> blockItem() {
        return none();
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> barrier() {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> northFacing(String name) {
        return none();
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> air() {
        return none();
    }
}
