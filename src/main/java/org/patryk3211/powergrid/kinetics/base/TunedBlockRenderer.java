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
package org.patryk3211.powergrid.kinetics.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
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
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.kinetics.variac.VariacBlock;

public class TunedBlockRenderer<T extends TunedBlockEntity> extends KineticBlockEntityRenderer<T, TunedBlockRenderer.TunedRenderState> {
    public TunedBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TunedRenderState createRenderState() {
        return new TunedRenderState();
    }

    @Override
    public void extractRenderState(T be, TunedRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        if (state.support)
            SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        BlockState blockState = be.getBlockState();
        float angle = be.arm.getValue(tickProgress) * (float) Math.PI * 1.75f;
        state.armature = CachedBuffers
                .partialFacing(ModdedPartialModels.VARIAC_ARMATURE, blockState, blockState.getValue(VariacBlock.HORIZONTAL_FACING).getOpposite())
                .rotateCentered(angle, Direction.UP)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .extractRenderState();
    }

    @Override
    public void submit(TunedRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.armature != null)
            state.armature.submit(matrices, queue);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(T be, TunedRenderState state) {
        BlockState blockState = be.getBlockState();
        if (blockState.getValue(TunedBlock.BASE))
            return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, Direction.UP);
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT, blockState, Direction.NORTH);
    }

    public static class TunedRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState armature;
    }
}
