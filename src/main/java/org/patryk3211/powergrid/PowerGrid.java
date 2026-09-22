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
package org.patryk3211.powergrid;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.advancements.PowerGridTriggers;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.electricity.solarpanel.registry.SolarBiomeEntry;
import org.patryk3211.powergrid.electricity.solarpanel.registry.SolarBiomeRegistry;
import org.patryk3211.powergrid.circuits.components.Components;
import org.patryk3211.powergrid.collections.*;
import org.patryk3211.powergrid.commands.PerformanceCommand;
import org.patryk3211.powergrid.compat.CompatMod;
import org.patryk3211.powergrid.compat.sable.SableUtils;
import org.patryk3211.powergrid.electricity.deviceconnector.DeviceConnectorBlockEntity;
import org.patryk3211.powergrid.electricity.fan.ElectricFanBlockEntity;
import org.patryk3211.powergrid.electricity.febridge.FEInverterBlockEntity;
import org.patryk3211.powergrid.electricity.heater.HeaterFanProcessingTypes;
import org.patryk3211.powergrid.electricity.redstoneconverter.RedstoneConverterRegistry;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import team.reborn.energy.api.EnergyStorage;
import org.patryk3211.powergrid.electricity.wire.registry.WireItemEntry;
import org.patryk3211.powergrid.electricity.wire.registry.WireRegistry;
import org.patryk3211.powergrid.electricity.sim.ElectricalNetwork;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBlock;
import org.patryk3211.powergrid.equipment.thunder.LightningRodMovementBehaviour;
import org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity;
import org.patryk3211.powergrid.network.PlayerSelectionImpl;
import org.patryk3211.powergrid.utility.proxy.SubstituteBlockEntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PowerGrid {
	public static final String MOD_ID = "powergrid";

	public static final Logger LOGGER = LoggerFactory.getLogger(PowerGrid.class);

	public static final AbstractPowerGridRegistrate REGISTRATE = AbstractPowerGridRegistrate.create(MOD_ID);

	public static void init() {
		LOGGER.info("Power grid starting, prepare to be electrocuted");
		ElectricalNetwork.LOGGER = LOGGER;

		SableUtils.makeDummyProxy();

		register();
		CompatMod.register();

		ModdedConfigs.register();
		setup();

		PowerGridEvents.register();
		PlayerSelectionImpl.init();
		ModPackets.PACKETS.registerC2SListener();
	}

	private static void setup() {
		DynamicRegistries.registerSynced(ComponentRegistry.ITEM_REGISTRY_KEY, ComponentRegistry.ITEM_CODEC, ComponentRegistry.ITEM_CODEC);
		DynamicRegistries.register(SolarBiomeRegistry.KEY, SolarBiomeEntry.CODEC);
		DynamicRegistries.registerSynced(WireRegistry.KEY, WireItemEntry.CODEC, WireItemEntry.CODEC);
		RedstoneConverterRegistry.init();
		ModdedAdvancements.register();
		PowerGridTriggers.register();
	}

	public static void registerBlocksEarly() {
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(DeviceConnectorBlockEntity.class, DeviceConnectorBlockEntity::new);
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(PunchCardReaderBlockEntity.class, PunchCardReaderBlockEntity::new);
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(ElectricFanBlockEntity.class, ElectricFanBlockEntity::new);
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(FEInverterBlockEntity.class, FEInverterBlockEntity::new);
		SubstituteBlockEntityProvider.INSTANCE.lock();

		ModdedDisplaySources.register();
		ModdedBlocks.register();
		ModdedFluids.registerBlock();
	}

	private static void register() {
		ComponentRegistry.REGISTRY = FabricRegistryBuilder.create(ComponentRegistry.REGISTRY_KEY).buildAndRegister();

		ModdedSoundEvents.register();
		ModdedRecipeTypes.register();
		HeaterFanProcessingTypes.register();

		ModdedItems.register();
		ModdedFluids.registerItems();
		ModdedBlockEntities.register();
		ModdedEntities.register();
		ModdedContraptions.register();
		ModdedParticles.register();
		ModdedDataComponents.register();
		ModdedMenus.register();
		Components.register();
		ModdedCreativeTabs.register();

		ArgumentTypeRegistry.registerArgumentType(asResource("performance_counter"),
				PerformanceCommand.PerformanceCounterArgument.class,
				SingletonArgumentInfo.contextFree(PerformanceCommand.PerformanceCounterArgument::new));

		EnergyStorage.SIDED.registerForBlockEntity(
				(be, side) -> be.getEnergyStorage(side), ModdedBlockEntities.FE_INVERTER.get());

		LightningRodMovementBehaviour lightningRodBehaviour = new LightningRodMovementBehaviour();
		for (Block rod : List.of(Blocks.LIGHTNING_ROD, Blocks.EXPOSED_LIGHTNING_ROD, Blocks.WEATHERED_LIGHTNING_ROD, Blocks.OXIDIZED_LIGHTNING_ROD,
				Blocks.WAXED_LIGHTNING_ROD, Blocks.WAXED_EXPOSED_LIGHTNING_ROD, Blocks.WAXED_WEATHERED_LIGHTNING_ROD, Blocks.WAXED_OXIDIZED_LIGHTNING_ROD))
			MovementBehaviour.REGISTRY.register(rod, lightningRodBehaviour);
		registerBlockMovementChecks();

		REGISTRATE.register();
	}

	public static Identifier asResource(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static Identifier texture(String path) {
		return asResource("textures/" + path + ".png");
	}

	public static void registerBlockMovementChecks(){
		BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
			if (!(state.getBlock() instanceof SolarPanelBlock))
				return BlockMovementChecks.CheckResult.PASS;
			BlockState neighbor = world.getBlockState(pos.relative(direction));
			if (neighbor.getBlock() instanceof SolarPanelBlock)
				return BlockMovementChecks.CheckResult.SUCCESS;

			return BlockMovementChecks.CheckResult.PASS;
		});
	}

	public static int maxRPM() {
		return AllConfigs.server().kinetics.maxRotationSpeed.get();
	}
}
