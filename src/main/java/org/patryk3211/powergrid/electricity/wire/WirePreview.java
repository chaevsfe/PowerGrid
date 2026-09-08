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
package org.patryk3211.powergrid.electricity.wire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import org.patryk3211.powergrid.compat.sable.SableCompanion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedDataComponents;
import org.patryk3211.powergrid.compat.sable.SableUtils;
import org.patryk3211.powergrid.electricity.base.IElectric;
import org.patryk3211.powergrid.electricity.base.ITerminalPlacement;
import org.patryk3211.powergrid.electricity.wire.powercord.CordRenderer;
import org.patryk3211.powergrid.electricity.wire.powercord.ICordEndpoint;
import org.patryk3211.powergrid.electricity.wire.registry.WireItemEntry;
import org.patryk3211.powergrid.electricity.wire.registry.WireRegistry;
import org.patryk3211.powergrid.utility.BlockTrace;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.PlacementOverlay;

@Environment(EnvType.CLIENT)
public class WirePreview {
    public static final Object outlineSlot = new Object();

    private static int renderPath = 0;
    private static ICordEndpoint renderedCordEndpoint;
    private static WireItemEntry renderedItem;
    private static Pair<BlockTrace.TraceState, BlockTrace.TraceResult> renderedTrace;
    private static Vec3 renderedPos1, renderedPos2;
    private static int renderedColor;

    @Nullable
    public static ItemStack getUsedWireStack(Player player) {
        var stack1 = player.getMainHandItem();
        var stack2 = player.getOffhandItem();
        if(stack1 != null && IWire.isWire(player.level(), stack1.getItem()) && stack1.has(ModdedDataComponents.CONNECTION_DATA)) {
            return stack1;
        } else if(stack2 != null && IWire.isWire(player.level(), stack2.getItem()) && stack2.has(ModdedDataComponents.CONNECTION_DATA)) {
            return stack2;
        } else {
            return null;
        }
    }

    public static void tick() {
        renderPath = 0;
        var player = Minecraft.getInstance().player;
        if(player == null)
            return;
        ItemStack wireStack = getUsedWireStack(player);
        if(wireStack == null)
            return;
        if(!IWire.isWire(player.level(), wireStack.getItem()))
            return;
        renderedItem = WireRegistry.forItem(player.level(), wireStack.getItem());
        if(IWire.isCord(player.level(), wireStack.getItem())) {
            var endpoint = wireStack.getOrDefault(ModdedDataComponents.CONNECTION_DATA, WireConnection.EMPTY).endpoint();
            if(!(endpoint instanceof ICordEndpoint cordEndpoint))
                return;
            renderedCordEndpoint = cordEndpoint;
            renderPath = 3;
            return;
        }
        var target = Minecraft.getInstance().hitResult;
        if(target == null)
            return;
        if(target.getType() != HitResult.Type.BLOCK) {
            if(target.getType() == HitResult.Type.ENTITY) {
                var entityHit = (EntityHitResult) target;
                if(!(entityHit.getEntity() instanceof BlockWireEntity)) {
                    return;
                }
            } else {
                return;
            }
        }

        var endpoint = wireStack.getOrDefault(ModdedDataComponents.CONNECTION_DATA, WireConnection.EMPTY).endpoint();
        if(endpoint == null)
            return;

        var world = Minecraft.getInstance().level;
        var currentPos = endpoint.getExactPosition(world);
        Direction continueDir = null;
        if(endpoint instanceof BlockWireEntityEndpoint bwe) {
            var entity = bwe.getEntity(world);
            if(entity != null) {
                var segments = entity.segments;
                if(segments.isEmpty())
                    return;
                if (bwe.getEnd()) {
                    var last = segments.get(segments.size() - 1);
                    continueDir = last.direction;
                } else {
                    var first = segments.get(0);
                    continueDir = first.direction.getOpposite();
                }
            }
        }

        var hitPoint = target.getLocation();
        ITerminalPlacement hitTerminal = null;
        if(target.getType() == HitResult.Type.BLOCK) {
            var blockTarget = (BlockHitResult) target;
            var state = world.getBlockState(blockTarget.getBlockPos());
            var electric = IElectric.getAt(world, blockTarget.getBlockPos());
            if(electric != null) {
                var pos = blockTarget.getBlockPos();
                var terminal = electric.terminalAt(state, hitPoint.subtract(pos.getX(), pos.getY(), pos.getZ()));
                if(terminal != null) {
                    hitPoint = terminal.getOrigin().add(pos.getX(), pos.getY(), pos.getZ());
                    hitTerminal = terminal;
                } else {
                    hitPoint = hitPoint.relative(blockTarget.getDirection(), 1/32f);
                }
            } else {
                hitPoint = hitPoint.relative(blockTarget.getDirection(), 1/32f);
            }
        }

        var projCurrentPos = SableCompanion.INSTANCE.projectOutOfSubLevel(world, currentPos);
        var projHitPos = SableCompanion.INSTANCE.projectOutOfSubLevel(world, hitPoint);
        float length = (float) projCurrentPos.distanceTo(projHitPos);
        // Stop rendering the preview above a thousand blocks to stop the game from freezing
        if(length > 1000)
            return;

        if(WireItem.alternateWirePlacement(player)) {
            if(!SableUtils.sameSubLevel(world, currentPos, hitPoint))
                return;
            length = 0;
            currentPos = BlockTrace.alignPosition(currentPos);
            renderedPos1 = projCurrentPos;
            renderedPos2 = currentPos;
            renderedTrace = Pair.of(null, BlockTrace.alternatePath(currentPos, hitPoint));
            for(var p : renderedTrace.getSecond().points()) {
                length += p.length();
            }
            renderPath = 2;
            return;
        }

        boolean isBlockWire = endpoint.type() != WireEndpointType.BLOCK;
        if(isBlockWire || hitTerminal == null) {
            if(!SableUtils.sameSubLevel(world, currentPos, hitPoint))
                return;
            length = 0;
            currentPos = BlockTrace.alignPosition(currentPos);
            renderedPos1 = projCurrentPos;
            renderedPos2 = currentPos;
            renderedTrace = BlockTrace.findPathWithState(world, currentPos, hitPoint, hitTerminal, continueDir);
            if(renderedTrace != null) {
                renderPath = 2;
                var points = renderedTrace.getSecond();
                if(points != null) {
                    for(var p : points.points()) {
                        length += p.length();
                    }
                }
            }
        } else {
            renderedColor = length < renderedItem.maximumLength() ? 0x80AAFFAA : 0x80FFAAAA;
            renderedPos1 = projCurrentPos;
            renderedPos2 = projHitPos;
            renderPath = 1;
        }

        if(!player.isCreative()) {
            int requiredItemCount = Math.max(Math.round(length * renderedItem.itemsPerMeter()), 1);
            PlacementOverlay.setItemRequirement(wireStack.getItem(), requiredItemCount, wireStack.getCount() >= requiredItemCount);
        }
    }

    public static void render(PoseStack matrixStack, SubmitNodeCollector queue, ClientLevel world, LocalPlayer player, Vec3 cameraPos) {
        int path = renderPath;
        if(path == 0)
            return;
        matrixStack.pushPose();
        switch(path) {
            case 1 -> {
                matrixStack.translate(renderedPos1.x - cameraPos.x, renderedPos1.y - cameraPos.y, renderedPos1.z - cameraPos.z);
                double thickness = renderedItem.wireThickness();
                Vec3 end = renderedPos2.subtract(renderedPos1);
                int color = renderedColor;
                queue.submitCustomGeometry(matrixStack, RenderTypes.entityTranslucent(renderedItem.texture()),
                        (pose, consumer) -> HangingWireRenderer.renderFromPositions(pose, consumer, Vec3.ZERO, end,
                                1.01, 1.2, thickness, LightCoordsUtil.FULL_BRIGHT, color));
            }
            case 2 -> {
                if(renderedTrace != null) {
                    matrixStack.translate(renderedPos1.x - cameraPos.x, renderedPos1.y - cameraPos.y, renderedPos1.z - cameraPos.z);
                    var points = renderedTrace.getSecond();
                    float thickness = renderedItem.wireThickness();
                    if(points != null) {
                        queue.submitCustomGeometry(matrixStack, RenderTypes.entityTranslucent(renderedItem.texture()), (pose, consumer) -> {
                            var currentPos = Vec3.ZERO;
                            int color = points.reachedTarget() ? 0x80AAFFAA : 0x80FFAAAA;
                            for(var p : points.points()) {
                                BlockWireRenderer.renderSegment(pose, consumer, LightCoordsUtil.FULL_BRIGHT, color,
                                        currentPos, p.direction, thickness, p.length(), 0);
                                currentPos = currentPos.add(p.vector());
                            }
                        });
                    }
                }
            }
            case 3 -> CordRenderer.renderPreview(renderedCordEndpoint, player.getRopeHoldPosition(AnimationTickHolder.getPartialTicks()),
                    matrixStack, queue, world, renderedItem, 0xFF413C31, cameraPos);
        }
        matrixStack.popPose();
    }

    public static Component distanceOverlay(Player player) {
        ItemStack wireStack = getUsedWireStack(player);
        if(wireStack == null)
            return null;
        if(!IWire.isWire(player.level(), wireStack.getItem()))
            return null;
        var wireEntry = WireRegistry.forItem(player.level(), wireStack.getItem());

        var endpoint = wireStack.getOrDefault(ModdedDataComponents.CONNECTION_DATA, WireConnection.EMPTY).endpoint();
        if(endpoint == null)
            return null;

        var currentPos = endpoint.getExactPosition(player.level());
        var target = Minecraft.getInstance().hitResult;
        if(target == null || target.getType() != HitResult.Type.BLOCK)
            return null;
        var hitPoint = target.getLocation();
        var distance = SableUtils.projectedDistance(player.level(), currentPos, hitPoint);
        var msg = Lang.translate("gui.endpoint_distance")
                .add(Lang.numberConstant(distance).style(distance < wireEntry.maximumLength() ? ChatFormatting.GREEN : ChatFormatting.RED))
                .style(ChatFormatting.WHITE);
        if(!endpoint.isValid(player.level())) {
            msg.add(Component.literal(" "))
                    .add(Lang.translate("message.no_original_connector")
                    .style(ChatFormatting.YELLOW)
                    .style(ChatFormatting.ITALIC));
        }

        return msg.component();
    }

    public static void notifyOfBlock(BlockPos pos) {
        Outliner.getInstance().showAABB(outlineSlot, new AABB(pos), 50)
                .colored(Color.RED.brighter())
                .withFaceTexture(AllSpecialTextures.CHECKERED)
                .lineWidth(0.05f);
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(AllSoundEvents.DENY.getMainEvent(), 1));
    }
}
