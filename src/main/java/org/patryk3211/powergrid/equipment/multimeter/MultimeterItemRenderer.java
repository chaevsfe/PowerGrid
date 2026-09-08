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
package org.patryk3211.powergrid.equipment.multimeter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import org.patryk3211.powergrid.compat.sable.SableCompanion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.wire.HangingWireRenderer;
import org.patryk3211.powergrid.electricity.wire.WireEndpointType;

@Environment(EnvType.CLIENT)
public class MultimeterItemRenderer {
    private static final Identifier TEXTURE = PowerGrid.texture("special/copper_wire");
    private static float mainPrevDial;
    private static float mainDial;
    private static float offPrevDial;
    private static float offDial;

    public static float getDialState(ItemStack stack) {
        var player = Minecraft.getInstance().player;
        var pt = AnimationTickHolder.getPartialTicks();
        if(player == null) {
            return 0;
        } else if(player.getMainHandItem() == stack) {
            return Mth.lerp(pt, mainPrevDial, mainDial);
        } else if(player.getOffhandItem() == stack) {
            return Mth.lerp(pt, offPrevDial, offDial);
        } else {
            return 0;
        }
    }

    public static void clientTick(Level level, Player player) {
        var stack1 = player.getMainHandItem();
        if(stack1.getItem() instanceof MultimeterItem multimeter) {
            mainPrevDial = mainDial;
            mainDial = multimeter.getDial(level, stack1);
        } else {
            mainDial = 0;
            mainPrevDial = 0;
        }
        var stack2 = player.getOffhandItem();
        if(stack2.getItem() instanceof MultimeterItem multimeter) {
            offPrevDial = offDial;
            offDial = multimeter.getDial(level, stack2);
        } else {
            offDial = 0;
            offPrevDial = 0;
        }
    }

    /* -------=========   Probe Rendering   =========------- */
    public static void renderProbe(Vec3 point, PoseStack matrixStack, SubmitNodeCollector queue, ClientLevel world, int color) {
        queue.submitCustomGeometry(matrixStack, RenderTypes.entitySolid(TEXTURE), (pose, consumer) ->
                HangingWireRenderer.renderFromPositions(pose, consumer, Vec3.ZERO,
                        point, 1.01f, 1.01f, 1 / 16f, world, color));
    }

    public static void render(PoseStack matrixStack, SubmitNodeCollector queue, ClientLevel world, LocalPlayer player, ItemStack stack, Vec3 cameraPos) {
        if(!(stack.getItem() instanceof MultimeterItem multimeter))
            return;
        var origin = player.getRopeHoldPosition(AnimationTickHolder.getPartialTicks());
        matrixStack.pushPose();
        matrixStack.translate(origin.x - cameraPos.x, origin.y - cameraPos.y, origin.z - cameraPos.z);
        var data = MultimeterItem.getModeData(stack);
        switch(multimeter.getMode(stack)) {
            case 0 -> {
                var pos = WireEndpointType.deserialize(data.getCompoundOrEmpty("Pos"));
                if(pos != null && pos.isValid(world)) {
                    var position = SableCompanion.INSTANCE.projectOutOfSubLevel(world, pos.getExactPosition(world));
                    renderProbe(position.subtract(origin), matrixStack, queue, world, 0xFFFF4040);
                }
                var neg = WireEndpointType.deserialize(data.getCompoundOrEmpty("Neg"));
                if(neg != null && neg.isValid(world)) {
                    var position = SableCompanion.INSTANCE.projectOutOfSubLevel(world, neg.getExactPosition(world));
                    renderProbe(position.subtract(origin), matrixStack, queue, world, 0xFF202020);
                }
            }
            case 1 -> {
                if(data.contains("X")) {
                    var pos = new Vec3(data.getDoubleOr("X", 0), data.getDoubleOr("Y", 0), data.getDoubleOr("Z", 0));
                    renderProbe(SableCompanion.INSTANCE.projectOutOfSubLevel(world, pos).subtract(origin), matrixStack, queue, world, 0xFF202020);
                }
            }
        }
        matrixStack.popPose();
    }

    public static void render(PoseStack matrixStack, SubmitNodeCollector queue, ClientLevel world, LocalPlayer player, Vec3 cameraPos) {
        render(matrixStack, queue, world, player, player.getMainHandItem(), cameraPos);
        render(matrixStack, queue, world, player, player.getOffhandItem(), cameraPos);
    }

    public static Component multimeterOverlayText(Player player) {
        Component right = null, left = null;
        var stack1 = player.getMainHandItem();
        if(stack1.getItem() instanceof MultimeterItem multimeter) {
            right = multimeter.getText(player.level(), player, stack1);
        }
        var stack2 = player.getOffhandItem();
        if(stack2.getItem() instanceof MultimeterItem multimeter) {
            left = multimeter.getText(player.level(), player, stack2);
        }
        if(right != null && left != null) {
            return Component.empty().append(left).append(" - ").append(right);
        } else if(right != null) {
            return right;
        } else {
            return left;
        }
    }
}
