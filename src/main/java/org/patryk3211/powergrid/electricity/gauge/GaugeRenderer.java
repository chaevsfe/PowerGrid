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
package org.patryk3211.powergrid.electricity.gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
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

import java.util.ArrayList;
import java.util.List;

import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class GaugeRenderer extends SmartBlockEntityRenderer<GaugeBlockEntity, GaugeRenderer.GaugeRenderState> {
    public GaugeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public GaugeRenderState createRenderState() {
        return new GaugeRenderState();
    }

    @Override
    public void extractRenderState(GaugeBlockEntity be, GaugeRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.parts.clear();

        Level level = be.getLevel();
        BlockState gaugeState = be.getBlockState();
        float progress = Mth.lerp(tickProgress, be.prevDialState, be.dialState);

        for (Direction facing : Iterate.directions) {
            if (!((IGaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(level, be.getBlockPos(), gaugeState, facing))
                continue;

            float dialPivot = 5.75f / 16;
            state.parts.add(rotateBufferTowards(CachedBuffers.partial(getDialModel(gaugeState), gaugeState), facing)
                    .translate(0, dialPivot, dialPivot)
                    .rotate((float) (Math.PI / 2 * -progress), Direction.EAST.getAxis())
                    .translate(0, -dialPivot, -dialPivot)
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .extractRenderState());
            state.parts.add(rotateBufferTowards(CachedBuffers.partial(getHeadModel(gaugeState, be), gaugeState), facing)
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .extractRenderState());
        }
    }

    @Override
    public void submit(GaugeRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        for (SuperByteBufferRenderState part : state.parts)
            part.submit(matrices, queue);
    }

    protected static SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
        return buffer.rotateCentered((float) ((-target.toYRot() - 90) / 180 * Math.PI), Direction.UP);
    }

    public static PartialModel getHeadModel(BlockState state, GaugeBlockEntity entity) {
        if (entity instanceof PowerGaugeBlockEntity)
            return ModdedPartialModels.CONDUCTIVE_POWER_HEAD;
        else if (entity instanceof CurrentGaugeBlockEntity)
            return ModdedPartialModels.CONDUCTIVE_CURRENT_HEAD;
        else
            return ModdedPartialModels.CONDUCTIVE_VOLTAGE_HEAD;
    }

    public static PartialModel getDialModel(BlockState state) {
        return AllPartialModels.GAUGE_DIAL;
    }

    public static class GaugeRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public final List<SuperByteBufferRenderState> parts = new ArrayList<>();
    }
}
