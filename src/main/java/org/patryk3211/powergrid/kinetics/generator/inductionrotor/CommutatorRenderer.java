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
package org.patryk3211.powergrid.kinetics.generator.inductionrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.kinetics.generator.rotor.RotorBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer;

public class CommutatorRenderer extends RotorRenderer<CommutatorRenderer.CommutatorRenderState> {
    public CommutatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CommutatorRenderState createRenderState() {
        return new CommutatorRenderState();
    }

    @Override
    public void extractRenderState(RotorBlockEntity rotor, CommutatorRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(rotor, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = rotor.getLevel();

        float brushAngle = getRotorAngle(rotor, tickProgress) * 2;
        double sin = Math.sin(brushAngle);
        double brushOffset = sin * sin * 1 / 16f;

        BlockState blockState = state.blockState;
        if (blockState.getBlock() instanceof VerticalCommutatorBlock) {
            Direction facing = blockState.getValue(VerticalCommutatorBlock.HORIZONTAL_FACING);
            if (!blockState.getValue(VerticalCommutatorBlock.UP))
                facing = facing.getOpposite();

            SuperByteBuffer brush = CachedBuffers.partial(ModdedPartialModels.VERTICAL_COMMUTATOR_BRUSH, blockState);
            state.brush1 = brush.light(state.lightCoords)
                    .cardinalLighting(level)
                    .center()
                    .rotateToFace(facing)
                    .uncenter()
                    .translate(-brushOffset, 0, 0)
                    .extractRenderState();
            state.brush2 = brush.light(state.lightCoords)
                    .cardinalLighting(level)
                    .center()
                    .rotateToFace(facing)
                    .rotateZ((float) Math.PI)
                    .uncenter()
                    .translate(-brushOffset, 0, 0)
                    .extractRenderState();
        } else {
            Direction.Axis axis = blockState.getValue(CommutatorBlock.HORIZONTAL_FACING).getAxis();
            Direction facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);

            SuperByteBuffer brush = CachedBuffers.partial(ModdedPartialModels.COMMUTATOR_BRUSH, blockState);
            state.brush1 = brush.light(state.lightCoords)
                    .cardinalLighting(level)
                    .center()
                    .rotateToFace(facing)
                    .uncenter()
                    .translate(-brushOffset, 0, 0)
                    .extractRenderState();
            state.brush2 = brush.light(state.lightCoords)
                    .cardinalLighting(level)
                    .center()
                    .rotateToFace(facing.getOpposite())
                    .uncenter()
                    .translate(-brushOffset, 0, 0)
                    .extractRenderState();
        }
    }

    @Override
    public void submit(CommutatorRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.brush1 != null)
            state.brush1.submit(matrices, queue);
        if (state.brush2 != null)
            state.brush2.submit(matrices, queue);
    }

    @Override
    protected SuperByteBuffer getModelForState(BlockState state) {
        if (state.getBlock() instanceof VerticalCommutatorBlock)
            return CachedBuffers.partialFacing(ModdedPartialModels.COMMUTATOR_SHAFT, state, Direction.UP);
        return CachedBuffers.partialFacing(ModdedPartialModels.COMMUTATOR_SHAFT, state,
                Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(CommutatorBlock.HORIZONTAL_FACING).getAxis()));
    }

    public static class CommutatorRenderState extends RotorRenderer.RotorRenderState {
        public @Nullable SuperByteBufferRenderState brush1;
        public @Nullable SuperByteBufferRenderState brush2;
    }
}
