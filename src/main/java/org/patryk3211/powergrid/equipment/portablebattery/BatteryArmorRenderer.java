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
package org.patryk3211.powergrid.equipment.portablebattery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;

@Environment(EnvType.CLIENT)
public class BatteryArmorRenderer implements ArmorRenderer {
    public static void register() {
        ArmorRenderer.register(new BatteryArmorRenderer(), ModdedItems.PORTABLE_BATTERY.get());
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector submitter, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if(state.pose == Pose.SLEEPING)
            return;

        if(!(stack.getItem() instanceof PortableBatteryItem item))
            return;

        var renderedState = item.getBlock().defaultBlockState()
                .setValue(HorizontalElectricBlock.HORIZONTAL_FACING, Direction.NORTH);
        var battery = CachedBuffers.block(renderedState);

        matrices.pushPose();

        contextModel.body.translateAndRotate(matrices);
        matrices.translate(-1 / 2f, 10 / 16f, 1f);
        matrices.scale(1, -1, -1);

        battery.disableDiffuse()
                .light(light)
                .extractRenderState()
                .submit(Sheets.cutoutBlockItemSheet(), matrices, submitter);

        matrices.popPose();
    }
}
