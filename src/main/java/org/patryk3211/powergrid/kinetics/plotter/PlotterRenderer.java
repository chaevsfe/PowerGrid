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
package org.patryk3211.powergrid.kinetics.plotter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class PlotterRenderer extends KineticBlockEntityRenderer<PlotterBlockEntity, PlotterRenderer.PlotterRenderState> {
    private static final float TIME_SPAN = 9.5f / 16;
    private static final float VOLTAGE_SPAN = 11.5f / 16;
    private static final Vec3 GRAPH_ORIGIN = new Vec3(8.0 / 16.0, 16.2 / 16.0, 13.0 / 16.0);

    private static final Quaternionf GRAPH_ROTATION = new Quaternionf()
            .rotateX(-112.5f / 180f * (float) Math.PI);

    public PlotterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static Vec3 graphPoint(float t, float v, BlockPos pos, Direction facing) {
        return PlotterGraph.graphPoint(t, v, pos, facing);
    }

    @Override
    public PlotterRenderState createRenderState() {
        return new PlotterRenderState();
    }

    @Override
    public void extractRenderState(PlotterBlockEntity be, PlotterRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        if (state.support)
            SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PlotterBlock.HORIZONTAL_FACING);

        state.pointer = CachedBuffers.partial(ModdedPartialModels.PLOTTER_POINTER, blockState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate(Mth.lerp(tickProgress, be.prevHeadPosition, be.headPosition) * VOLTAGE_SPAN * 0.5f, 0, 0)
                .extractRenderState();

        state.viewer = CachedBuffers.partial(ModdedPartialModels.PLOTTER_VIEWER, blockState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .uncenter()
                .extractRenderState();

        var sprite = ModdedPartialModels.PAPER_SHIFT;
        float spriteWidth = sprite.getTarget().getU1() - sprite.getTarget().getU0();
        double uScroll = be.getAnimationSpeed() * AnimationTickHolder.getRenderTime(level) / (256 * 20) * TIME_SPAN;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;
        state.paper = CachedBuffers.partialFacing(ModdedPartialModels.PLOTTER_PAPER, blockState, facing.getOpposite())
                .cardinalLighting(level)
                .light(state.lightCoords)
                .translate(0, 1 / 64f, 0)
                .shiftUVScrolling(sprite, (float) uScroll, 0)
                .extractRenderState();

        state.facing2D = facing.get2DDataValue();
        state.scrolling = be.getAnimationSpeed() != 0;
        state.tickProgress = tickProgress;
        state.argbColor = be.color.getTextureDiffuseColor() | 0xFF000000;
        if (state.samples == null || state.samples.length != be.sampleBuffer.length)
            state.samples = new float[be.sampleBuffer.length];
        for (int i = 0; i < state.samples.length; ++i)
            state.samples[i] = be.sampleBuffer[(be.head + i) % be.sampleBuffer.length];
    }

    @Override
    public void submit(PlotterRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.pointer != null)
            state.pointer.submit(matrices, queue);
        if (state.viewer != null)
            state.viewer.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
        if (state.paper != null)
            state.paper.submit(matrices, queue);
        if (state.samples == null || state.samples.length == 0)
            return;

        float[] samples = state.samples;
        int argbColor = state.argbColor;
        matrices.pushPose();
        matrices.rotateAround(new Quaternionf().rotateY(0.5f * (float) Math.PI * (2 - state.facing2D)), 0.5f, 0.5f, 0.5f);
        matrices.rotateAround(GRAPH_ROTATION, (float) GRAPH_ORIGIN.x, (float) GRAPH_ORIGIN.y, (float) GRAPH_ORIGIN.z);
        matrices.translate(GRAPH_ORIGIN.x, GRAPH_ORIGIN.y, GRAPH_ORIGIN.z);
        matrices.scale(VOLTAGE_SPAN * 0.5f, TIME_SPAN, 1);
        if (state.scrolling)
            matrices.translate(0, -1.0f / samples.length * state.tickProgress, 0);
        queue.submitCustomGeometry(matrices, RenderTypes.debugQuads(), (pose, consumer) -> {
            Matrix4f m4 = pose.pose();
            float yPrev = 0;
            for (int i = 0; i < samples.length; ++i) {
                float y = Mth.clamp(samples[i], -1, 1);
                if (i == 0) {
                    yPrev = y;
                    continue;
                }
                float height = Math.max(1.0f / samples.length, 0.0025f);
                float x2 = (float) i / samples.length;
                float x1 = x2 - height;

                float thickness = 1 / 32f;
                var diff = Math.abs(y - yPrev) * 0.5f;
                thickness += diff;

                float yMid = (yPrev + y) * 0.5f;
                y = yPrev = yMid;
                consumer.addVertex(m4, yPrev - thickness, x1, 0).setColor(argbColor);
                consumer.addVertex(m4, yPrev + thickness, x1, 0).setColor(argbColor);
                consumer.addVertex(m4, y + thickness, x2, 0).setColor(argbColor);
                consumer.addVertex(m4, y - thickness, x2, 0).setColor(argbColor);

                yPrev = y;
            }
        });
        matrices.popPose();
    }

    @Override
    protected BlockState getRenderedBlockState(PlotterBlockEntity be) {
        return shaft(be.getBlockState().getValue(PlotterBlock.HORIZONTAL_FACING).getClockWise().getAxis());
    }

    public static class PlotterRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState pointer;
        public @Nullable SuperByteBufferRenderState viewer;
        public @Nullable SuperByteBufferRenderState paper;
        public float @Nullable [] samples;
        public int argbColor;
        public int facing2D;
        public boolean scrolling;
        public float tickProgress;
    }
}
