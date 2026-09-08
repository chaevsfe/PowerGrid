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
package org.patryk3211.powergrid.kinetics.punchcard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
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

public class PunchCardReaderRenderer extends KineticBlockEntityRenderer<PunchCardReaderBlockEntity, PunchCardReaderRenderer.PunchCardReaderRenderState> {
    public PunchCardReaderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PunchCardReaderRenderState createRenderState() {
        return new PunchCardReaderRenderState();
    }

    @Override
    public void extractRenderState(PunchCardReaderBlockEntity be, PunchCardReaderRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        if (state.support)
            SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        state.card = null;
        if (be.currentItem().isEmpty())
            return;

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PunchCardReaderBlock.HORIZONTAL_FACING);
        state.card = CachedBuffers.partial(ModdedPartialModels.PUNCH_CARD, blockState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate(0, 0, -Mth.lerp(tickProgress, be.prevAngle, be.angle) * 6 / 16f / 16f)
                .extractRenderState();
    }

    @Override
    public void submit(PunchCardReaderRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.card != null)
            state.card.submit(matrices, queue);
    }

    @Override
    protected BlockState getRenderedBlockState(PunchCardReaderBlockEntity be) {
        return shaft(be.getBlockState().getValue(PunchCardReaderBlock.HORIZONTAL_FACING).getClockWise().getAxis());
    }

    public static class PunchCardReaderRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState card;
    }
}
