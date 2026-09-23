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

import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import org.patryk3211.powergrid.compat.viewer.TallBlockRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import org.patryk3211.powergrid.collections.ModdedClientBehaviours;
import org.patryk3211.powergrid.collections.ModdedEntityRenders;
import org.patryk3211.powergrid.collections.ModdedItemModels;
import org.patryk3211.powergrid.collections.ModdedMenuScreens;
import org.patryk3211.powergrid.collections.ModdedModels;
import org.patryk3211.powergrid.collections.ModdedParticleProviders;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;
import org.patryk3211.powergrid.electricity.transformer.TransformerWindingInteraction;
import org.patryk3211.powergrid.electricity.transformer.TransformerWindingScreen;
import org.patryk3211.powergrid.electricity.wire.ClientWireInteractions;
import org.patryk3211.powergrid.electricity.wire.WirePreview;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItemRenderer;
import org.patryk3211.powergrid.equipment.portablebattery.BatteryArmorRenderer;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerItemRenderer;
import org.patryk3211.powergrid.equipment.zapper.ElectroZapperRenderHandler;
import org.patryk3211.powergrid.kinetics.generator.winding.WindingPreview;
import org.patryk3211.powergrid.client.displaysource.ModdedDisplaySourceRenders;
import org.patryk3211.powergrid.electricity.info.PowerGridTooltipModifiers;
import org.patryk3211.powergrid.ponder.PowerGridPonderPlugin;
import org.patryk3211.powergrid.utility.CustomValueSettingsScreen;
import org.patryk3211.powergrid.utility.LevelKind;
import org.patryk3211.powergrid.utility.PlacementOverlay;
import org.patryk3211.powergrid.utility.sound.SoundScapes;

@Environment(EnvType.CLIENT)
public class PowerGridClient {
	public static final ElectroZapperRenderHandler ELECTRO_ZAPPER_RENDER_HANDLER = new ElectroZapperRenderHandler();

	public static void initClient() {
		LevelKind.PONDER = level -> level instanceof PonderLevel;
		ModdedPartialModels.register();
		ModdedRenderLayers.register();
		ModdedModels.register();
		ModdedItemModels.register();
		ModdedEntityRenders.register();
		ModdedParticleProviders.register();
		ModdedMenuScreens.register();
		ModdedClientBehaviours.register();
		ModdedDisplaySourceRenders.register();
		PowerGridTooltipModifiers.register();
		BatteryArmorRenderer.register();
		TransformerWindingInteraction.setOpener((block, hand, turns, cap) ->
				TransformerWindingScreen.beginInteraction(() -> new TransformerWindingScreen(block, hand, turns, cap)));
		PlacementOverlay.init();
		PowerGridClientEvents.register();
		PonderIndex.addPlugin(new PowerGridPonderPlugin());
		PictureInPictureRendererRegistry.register(context -> new TallBlockRenderer(context.bufferSource()));
	}

	public static void clientTick(Minecraft client) {
		if(client.level == null || client.player == null)
			return;

		SoundScapes.tick();

		ELECTRO_ZAPPER_RENDER_HANDLER.tick();
		CustomValueSettingsScreen.clientTick();
		WindingPreview.tick();
		WirePreview.tick();
		TransformerWindingScreen.clientTick();
		ClientWireInteractions.clientTick();
		ThermometerItemRenderer.clientTick();
		MultimeterItemRenderer.clientTick(client.level, client.player);
	}
}
