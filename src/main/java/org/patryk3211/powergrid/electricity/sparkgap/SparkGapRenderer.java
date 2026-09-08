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
package org.patryk3211.powergrid.electricity.sparkgap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
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
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class SparkGapRenderer extends SmartBlockEntityRenderer<SparkGapBlockEntity, SparkGapRenderer.SparkGapRenderState> {
    public SparkGapRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SparkGapRenderState createRenderState() {
        return new SparkGapRenderState();
    }

    @Override
    public void extractRenderState(SparkGapBlockEntity be, SparkGapRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        Direction facing = Direction.fromAxisAndDirection(blockState.getValue(SparkGapBlock.HORIZONTAL_AXIS), Direction.AxisDirection.POSITIVE);
        float offset = be.setting.getValue() / 18f * (1.5f / 18f);

        SuperByteBuffer buffer = CachedBuffers.partial(ModdedPartialModels.SPARK_GAP_ARM, blockState);
        state.arm1 = buffer
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate(0, 0, -offset)
                .extractRenderState();
        state.arm2 = buffer
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .rotateYDegrees(180)
                .uncenter()
                .translate(0, 0, -offset)
                .extractRenderState();
    }

    @Override
    public void submit(SparkGapRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.arm1 != null)
            state.arm1.submit(matrices, queue);
        if (state.arm2 != null)
            state.arm2.submit(matrices, queue);
    }

    public static class SparkGapRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState arm1;
        public @Nullable SuperByteBufferRenderState arm2;
    }
}
