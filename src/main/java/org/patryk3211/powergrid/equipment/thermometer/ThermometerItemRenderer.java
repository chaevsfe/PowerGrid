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
package org.patryk3211.powergrid.equipment.thermometer;

import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.Unit;

@Environment(EnvType.CLIENT)
public class ThermometerItemRenderer {
    private static final RandomSource random = RandomSource.create();
    private static float progress = 0;
    private static float prevProgress = 0;
    private static Float temperature = null;

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        var item = ModdedBlocks.THERMOMETER.asItem();
        if(player != null && (player.getMainHandItem().is(item) || player.getOffhandItem().is(item))) {
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
                var hit = (BlockHitResult) mc.hitResult;
                var behaviour = BlockEntityBehaviour.get(mc.level, hit.getBlockPos(), ThermalBehaviour.TYPE);
                if(behaviour != null) {
                    var target = Mth.clamp((behaviour.getTemperature() - 22f) / (175f - 22f), 0, 1.125f);
                    goTo(target);
                    temperature = behaviour.getTemperature();
                    return;
                }
            }
        }
        temperature = null;
        goTo(0);
    }

    private static void goTo(float target) {
        prevProgress = progress;
        progress += (target - progress) * .125f;
        if (progress > 1 && random.nextFloat() < 1 / 2f)
            progress -= (progress - 1) * random.nextFloat();
    }

    public static float needleAngle(float partialTicks) {
        if(Float.isNaN(progress)) {
            progress = 0;
            prevProgress = 0;
        }
        return ThermometerRenderer.NEEDLE_SPAN * -Mth.lerp(partialTicks, prevProgress, progress);
    }

    @Nullable
    public static Component overlayText(Player player) {
        if(temperature == null || !GogglesItem.isWearingGoggles(player))
            return null;
        var color = ChatFormatting.GREEN;
        if(temperature > 150) {
            color = ChatFormatting.RED;
        } else if(temperature > 125) {
            color = ChatFormatting.YELLOW;
        }
        var temperatureText = Lang.numberConstant(temperature);
        if(temperature > 175) {
            temperatureText = Lang.text(">175.0");
        }
        return Lang.translate("gui.thermometer.temperature").style(ChatFormatting.WHITE)
                .add(temperatureText.style(color)).add(Component.literal(" "))
                .add(Unit.TEMPERATURE.get()).component();
    }
}
