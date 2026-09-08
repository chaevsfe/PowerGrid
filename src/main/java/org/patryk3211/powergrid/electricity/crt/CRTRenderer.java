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
package org.patryk3211.powergrid.electricity.crt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

public class CRTRenderer extends SmartBlockEntityRenderer<CRTBlockEntity, CRTRenderer.CRTRenderState> {
    private static final float CRT_SPAN = 7.5f / 16f;

    public static float zDepth() {
        return ModdedConfigs.client().crtZDepth.getF();
    }

    public CRTRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CRTRenderState createRenderState() {
        return new CRTRenderState();
    }

    @Override
    public void extractRenderState(CRTBlockEntity be, CRTRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(CRTBlock.HORIZONTAL_FACING);

        state.background = CachedBuffers.partialFacing(ModdedPartialModels.CRT_BACKGROUND, blockState, facing.getOpposite())
                .cardinalLighting(level)
                .light(state.lightCoords)
                .extractRenderState();

        state.facing2D = facing.get2DDataValue();
        state.color = be.getColor().getTextureDiffuseColor();
        state.dotSize = ModdedConfigs.client().crtDotSize.getF();
        state.tracePersistence = ModdedConfigs.client().crtTracePersistence.getF();
        state.zDepth = zDepth();

        int count = be.brightness.length;
        if (state.xPoints == null || state.xPoints.length != count) {
            state.xPoints = new float[count];
            state.yPoints = new float[count];
            state.brightness = new float[count];
        }
        for (int i = 0; i < count; ++i) {
            int index = (i + be.head + 1) % count;
            state.xPoints[i] = be.xPoints[index];
            state.yPoints[i] = be.yPoints[index];
            state.brightness[i] = be.brightness[index];
        }
    }

    @Override
    public void submit(CRTRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.background != null)
            state.background.submit(matrices, queue);
        if (state.xPoints == null || state.xPoints.length < 2)
            return;

        matrices.pushPose();
        matrices.rotateAround(new Quaternionf().rotateY(0.5f * (float) Math.PI * (2 - state.facing2D)), 0.5f, 0.5f, 0.5f);
        matrices.translate(0.5f, 0.375f, 1 / 32f + state.zDepth * 0.5f);
        matrices.scale(CRT_SPAN * 0.5f, CRT_SPAN * 0.5f, 1);
        queue.submitCustomGeometry(matrices, ModdedRenderLayers.getAdditiveCrt(), (pose, consumer) -> trace(state, pose.pose(), consumer));
        matrices.popPose();
    }

    private static void trace(CRTRenderState state, Matrix4f m4, VertexConsumer consumer) {
        final int R = (state.color >> 16) & 0xFF,
                G = (state.color >> 8) & 0xFF,
                B = state.color & 0xFF;
        final float size = state.dotSize;
        final float zDepth = state.zDepth;
        float x1 = 0, y1 = 0, b1 = 0;
        for (int i = 0; i < state.xPoints.length - 1; ++i) {
            var x2 = state.xPoints[i];
            var y2 = state.yPoints[i];
            var b2 = state.brightness[i] * (float) (1 - Math.exp(-i * state.tracePersistence));

            var nx = x2 - x1;
            var ny = y2 - y1;

            var snx = Math.signum(nx);
            var sny = Math.signum(ny);

            if (i == 0 || (Math.abs(nx) < 1e-3f && Math.abs(ny) < 1e-3f)) {
                putPoint(consumer, m4, zDepth, x2 - size, y2 - size, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 - size, y2 + size, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 + size, y2 + size, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 + size, y2 - size, R * b2, G * b2, B * b2, i);

                putPoint(consumer, m4, zDepth, x2 - size, y2 - size, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 + size, y2 + size, R * b2, G * b2, B * b2, i);
            } else if (Math.abs(nx) < 1e-3f) {
                putPoint(consumer, m4, zDepth, x2 - size, y1 + size * sny, R * b1, G * b1, B * b1, i - 1);
                putPoint(consumer, m4, zDepth, x2 + size, y1 + size * sny, R * b1, G * b1, B * b1, i - 1);
                putPoint(consumer, m4, zDepth, x2 + size, y2 + size * sny, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 - size, y2 + size * sny, R * b2, G * b2, B * b2, i);

                putPoint(consumer, m4, zDepth, x2 + size, y2 + size * sny, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 - size, y1 + size * sny, R * b1, G * b1, B * b1, i - 1);
            } else if (Math.abs(ny) < 1e-3f) {
                putPoint(consumer, m4, zDepth, x1 + size * snx, y2 - size, R * b1, G * b1, B * b1, i - 1);
                putPoint(consumer, m4, zDepth, x1 + size * snx, y2 + size, R * b1, G * b1, B * b1, i - 1);
                putPoint(consumer, m4, zDepth, x2 + size * snx, y2 + size, R * b2, G * b2, B * b2, i);
                putPoint(consumer, m4, zDepth, x2 + size * snx, y2 - size, R * b2, G * b2, B * b2, i);

                putPoint(consumer, m4, zDepth, x1 + size * snx, y2 - size, R * b1, G * b1, B * b1, i - 1);
                putPoint(consumer, m4, zDepth, x2 + size * snx, y2 + size, R * b2, G * b2, B * b2, i);
            } else {
                var mainX1 = x1 + size * snx;
                var mainY1 = y1 + size * sny;
                var mainX2 = x2 - size * snx;
                var mainY2 = y2 - size * sny;

                var leftX1 = mainX1 - size * 2 * snx;
                var topY2 = mainY2 + size * 2 * sny;
                var bottomY1 = mainY1 - size * 2 * sny;
                var rightX2 = mainX2 + size * 2 * snx;

                var xInside = (mainX2 - mainX1) * snx < 0;
                var yInside = (mainY2 - mainY1) * sny < 0;

                if (xInside && yInside) {
                    putPoint(consumer, m4, zDepth, leftX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                    putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                    putPoint(consumer, m4, zDepth, mainX2, topY2, R * b2, G * b2, B * b2, i);

                    putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                    putPoint(consumer, m4, zDepth, mainX2, topY2, R * b2, G * b2, B * b2, i);
                    putPoint(consumer, m4, zDepth, rightX2, topY2, R * b2, G * b2, B * b2, i);

                    putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                    putPoint(consumer, m4, zDepth, rightX2, mainY2, R * b2, G * b2, B * b2, i);
                    putPoint(consumer, m4, zDepth, rightX2, topY2, R * b2, G * b2, B * b2, i);

                    putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                    putPoint(consumer, m4, zDepth, mainX1, bottomY1, R * b1, G * b1, B * b1, i - 1);
                    putPoint(consumer, m4, zDepth, rightX2, mainY2, R * b2, G * b2, B * b2, i);
                } else {
                    putPoint(consumer, m4, zDepth, x2 - size, y2 - size, R * b2, G * b2, B * b2, i);
                    putPoint(consumer, m4, zDepth, x2 - size, y2 + size, R * b2, G * b2, B * b2, i);
                    putPoint(consumer, m4, zDepth, x2 + size, y2 + size, R * b2, G * b2, B * b2, i);
                    putPoint(consumer, m4, zDepth, x2 + size, y2 - size, R * b2, G * b2, B * b2, i);

                    putPoint(consumer, m4, zDepth, x2 - size, y2 - size, R * b2, G * b2, B * b2, i);
                    putPoint(consumer, m4, zDepth, x2 + size, y2 + size, R * b2, G * b2, B * b2, i);

                    if (yInside) {
                        putPoint(consumer, m4, zDepth, mainX2, topY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                        putPoint(consumer, m4, zDepth, leftX1, mainY1, R * b1, G * b1, B * b1, i - 1);

                        putPoint(consumer, m4, zDepth, mainX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                        putPoint(consumer, m4, zDepth, mainX2, topY2, R * b2, G * b2, B * b2, i);
                    } else {
                        putPoint(consumer, m4, zDepth, mainX2, topY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, leftX1, mainY1, R * b1, G * b1, B * b1, i - 1);

                        putPoint(consumer, m4, zDepth, mainX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                        putPoint(consumer, m4, zDepth, leftX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                    }
                    if (xInside) {
                        putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                        putPoint(consumer, m4, zDepth, mainX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, rightX2, mainY2, R * b2, G * b2, B * b2, i);

                        putPoint(consumer, m4, zDepth, rightX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                        putPoint(consumer, m4, zDepth, mainX1, bottomY1, R * b1, G * b1, B * b1, i - 1);
                    } else {
                        putPoint(consumer, m4, zDepth, mainX1, mainY1, R * b1, G * b1, B * b1, i - 1);
                        putPoint(consumer, m4, zDepth, mainX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX1, bottomY1, R * b1, G * b1, B * b1, i - 1);

                        putPoint(consumer, m4, zDepth, mainX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, rightX2, mainY2, R * b2, G * b2, B * b2, i);
                        putPoint(consumer, m4, zDepth, mainX1, bottomY1, R * b1, G * b1, B * b1, i - 1);
                    }
                }
            }

            x1 = x2;
            y1 = y2;
            b1 = b2;
        }
    }

    private static void putPoint(VertexConsumer consumer, Matrix4f m4, float zDepth, float x, float y, float r, float g, float b, int z) {
        consumer.addVertex(m4, x, y, -z * zDepth)
                .setColor((int) r, (int) g, (int) b, 255);
    }

    public static class CRTRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState background;
        public float @Nullable [] xPoints;
        public float @Nullable [] yPoints;
        public float @Nullable [] brightness;
        public int color;
        public int facing2D;
        public float dotSize;
        public float tracePersistence;
        public float zDepth;
    }
}
