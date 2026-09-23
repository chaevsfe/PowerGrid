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

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModPackets;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.collections.ModdedKeys;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.config.ServerConfigSync;
import org.patryk3211.powergrid.electricity.ClientElectricNetwork;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.electricity.info.PowerGridTooltipModifiers;
import org.patryk3211.powergrid.electricity.info.TerminalHandler;
import org.patryk3211.powergrid.electricity.wire.ClientWireInteractions;
import org.patryk3211.powergrid.electricity.wire.WireItem;
import org.patryk3211.powergrid.electricity.wire.WirePreview;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItemRenderer;
import org.patryk3211.powergrid.network.packets.EntityDataS2CPacket;
import org.patryk3211.powergrid.network.packets.NegotiateSyncC2SPacket;
import org.patryk3211.powergrid.utility.PlacementOverlay;

@Environment(EnvType.CLIENT)
public class PowerGridClientEvents {
    private static boolean alternatePlacementDown;

    public static void register() {
        ModdedKeys.register();
        ModPackets.PACKETS.registerS2CListener();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> negotiateSync());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientElectricNetwork.unloadWorld(client, client.level);
            EntityDataS2CPacket.reset();
            ServerConfigSync.restore();
        });
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> {
            ClientElectricNetwork.levelChanged(client, level);
            EntityDataS2CPacket.reset();
        });

        ClientTickEvents.START_LEVEL_TICK.register(GlobalElectricNetworks::preTick);
        ClientTickEvents.END_LEVEL_TICK.register(TerminalHandler::tick);
        ClientTickEvents.END_CLIENT_TICK.register(PowerGridClientEvents::clientTick);

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            WireItem.tooltip(stack, lines, context, flag);
            PowerGridTooltipModifiers.appendElectricProperties(stack, flag, lines);
        });
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> EntityDataS2CPacket.clientEntityAdded(entity));

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> cutClear(level));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> cutClear(level));
        UseItemCallback.EVENT.register((player, level, hand) -> cutClear(level));

        HudElementRegistry.addLast(PowerGrid.asResource("placement_overlay"),
                (extractor, delta) -> PlacementOverlay.renderOverlay(extractor));

        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            var client = Minecraft.getInstance();
            if(client.level == null || client.player == null)
                return;
            var cameraPos = context.levelState().cameraRenderState.pos;
            if(cameraPos == null)
                return;
            WirePreview.render(context.poseStack(), context.submitNodeCollector(), client.level, client.player, cameraPos);
            MultimeterItemRenderer.render(context.poseStack(), context.submitNodeCollector(), client.level, client.player, cameraPos);
        });
    }

    private static void negotiateSync() {
        ModdedPackets.sendToServer(new NegotiateSyncC2SPacket(ModdedConfigs.common().syncWithDoubles.get()));
    }

    /**
     * Fabric has no raw key or mouse event, so the alternate placement modifier is edge polled
     * once a tick and fed to the same handler the raw input event used to call.
     */
    private static void clientTick(Minecraft client) {
        PowerGridClient.clientTick(client);
        if (client.player == null || client.level == null) {
            alternatePlacementDown = false;
            return;
        }
        boolean down = ModdedKeys.ALTERNATE_WIRE_PLACEMENT.isPressed();
        if (down != alternatePlacementDown) {
            alternatePlacementDown = down;
            ClientWireInteractions.alternatePlacementCheck(client, down ? InputConstants.PRESS : InputConstants.RELEASE);
        }
    }

    private static InteractionResult cutClear(Level level) {
        if (!level.isClientSide())
            return InteractionResult.PASS;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null)
            return InteractionResult.PASS;
        return ClientWireInteractions.cutClearCheck(client) ? InteractionResult.FAIL : InteractionResult.PASS;
    }
}
