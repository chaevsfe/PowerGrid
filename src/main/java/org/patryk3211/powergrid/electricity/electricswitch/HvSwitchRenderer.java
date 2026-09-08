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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class HvSwitchRenderer extends KineticBlockEntityRenderer<HvSwitchBlockEntity, HvSwitchRenderer.HvSwitchRenderState> {
    public HvSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public HvSwitchRenderState createRenderState() {
        return new HvSwitchRenderState();
    }

    @Override
    public void extractRenderState(HvSwitchBlockEntity be, HvSwitchRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.rod = null;
        if (state.support)
            return;

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(HvSwitchBlock.HORIZONTAL_FACING);
        float angle = (1.0f - be.rod.getValue(tickProgress)) * (float) Math.PI * 0.5f;
        state.rod = CachedBuffers.partialFacing(ModdedPartialModels.HV_SWITCH_ROD, blockState, facing)
                .rotateCentered(angle, facing.getClockWise())
                .cardinalLighting(state.cardinalLighting)
                .light(state.lightCoords)
                .extractRenderState();
    }

    @Override
    public void submit(HvSwitchRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.rod != null)
            state.rod.submit(matrices, queue);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(HvSwitchBlockEntity be, HvSwitchRenderState state) {
        BlockState blockState = be.getBlockState();
        return CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, blockState, blockState.getValue(HvSwitchBlock.HORIZONTAL_FACING).getClockWise());
    }

    public static class HvSwitchRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState rod;
    }
}
