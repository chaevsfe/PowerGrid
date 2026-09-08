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
package org.patryk3211.powergrid.electricity.electricswitch;

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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class HvBreakerRenderer extends KineticBlockEntityRenderer<HvBreakerBlockEntity, HvBreakerRenderer.HvBreakerRenderState> {
    public HvBreakerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public HvBreakerRenderState createRenderState() {
        return new HvBreakerRenderState();
    }

    @Override
    public void extractRenderState(HvBreakerBlockEntity be, HvBreakerRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        if (state.support)
            SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(HvBreakerBlock.HORIZONTAL_FACING);

        state.signalOpen = CachedBuffers.partial(ModdedPartialModels.HV_BREAKER_SIGNAL1, blockState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate(Mth.lerp(tickProgress, be.prevState ? 1 : 0, be.state ? 1 : 0) * 2 / 16f, 0, 0)
                .extractRenderState();

        state.signalCharge = CachedBuffers.partial(ModdedPartialModels.HV_BREAKER_SIGNAL2, blockState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate((1 - be.charge.getValue(tickProgress)) * 2 / 16f, 0, 0)
                .extractRenderState();
    }

    @Override
    public void submit(HvBreakerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.signalOpen != null)
            state.signalOpen.submit(matrices, queue);
        if (state.signalCharge != null)
            state.signalCharge.submit(matrices, queue);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(HvBreakerBlockEntity be, HvBreakerRenderState state) {
        BlockState blockState = be.getBlockState();
        return CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, blockState, blockState.getValue(HvSwitchBlock.HORIZONTAL_FACING).getClockWise());
    }

    public static class HvBreakerRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState signalOpen;
        public @Nullable SuperByteBufferRenderState signalCharge;
    }
}
