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
package org.patryk3211.powergrid.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGridClient;
import org.patryk3211.powergrid.equipment.drill.DrillItem;
import org.patryk3211.powergrid.equipment.saw.SawItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void powerGrid$submitToolArm(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if(PowerGridClient.ELECTRO_ZAPPER_RENDER_HANDLER.onRenderPlayerHand(stack, minecraft, entityRenderDispatcher,
                (ItemInHandRenderer) (Object) this, poseStack, collector, light, partialTick, hand, equipProgress, swingProgress)) {
            ci.cancel();
        } else if(stack.getItem() instanceof DrillItem || stack.getItem() instanceof SawItem) {
            powerGrid$submitPortableTool(player, hand, swingProgress, stack, equipProgress, poseStack, collector, light);
            ci.cancel();
        }
    }

    @Unique
    private void powerGrid$submitPortableTool(AbstractClientPlayer player, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
        float flip = rightHand ? 1.0f : -1.0f;

        poseStack.pushPose();
        poseStack.translate(flip * (0.64f - 0.1f), -0.6f + equipProgress * -0.6f, -0.72f - 0.1f);

        RandomSource random = player.level().getRandom();
        poseStack.mulPose(Axis.YP.rotationDegrees(flip * (random.nextFloat() * 2 - 1) * 5.0f * swingProgress));
        poseStack.mulPose(Axis.ZP.rotationDegrees(flip * (random.nextFloat() * 2 - 1) * -2.0f * swingProgress));
        ((ItemInHandRenderer) (Object) this).renderItem(player, stack,
                rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                poseStack, collector, light);
        poseStack.popPose();
    }
}
