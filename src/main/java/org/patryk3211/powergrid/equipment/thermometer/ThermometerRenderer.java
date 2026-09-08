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
package org.patryk3211.powergrid.equipment.thermometer;

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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class ThermometerRenderer extends SmartBlockEntityRenderer<ThermometerBlockEntity, ThermometerRenderer.ThermometerRenderState> {
    public static final float NEEDLE_SPAN = (float) (135 * Math.PI / 180);

    public ThermometerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ThermometerRenderState createRenderState() {
        return new ThermometerRenderState();
    }

    @Override
    public void extractRenderState(ThermometerBlockEntity be, ThermometerRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(ThermometerBlock.FACING);

        state.needle = prepareDial(CachedBuffers.partial(ModdedPartialModels.THERMOMETER_NEEDLE, blockState), facing,
                Mth.lerp(tickProgress, be.prevDialState, be.dialState))
                .cardinalLighting(level)
                .light(state.lightCoords)
                .extractRenderState();
        state.maxNeedle = prepareDial(CachedBuffers.partial(ModdedPartialModels.THERMOMETER_NEEDLE_RED, blockState), facing, be.maxState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .extractRenderState();
    }

    @Override
    public void submit(ThermometerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.needle != null)
            state.needle.submit(matrices, queue);
        if (state.maxNeedle != null)
            state.maxNeedle.submit(matrices, queue);
    }

    private static SuperByteBuffer prepareDial(SuperByteBuffer buffer, Direction facing, float progress) {
        float dialPivotY = 6f / 16, dialPivotX = 8 / 16f;
        return rotateBuffer(buffer, facing).translate(dialPivotX, dialPivotY, 0)
                .rotateZ(NEEDLE_SPAN * -progress)
                .translate(-dialPivotX, -dialPivotY, 0);
    }

    private static SuperByteBuffer rotateBuffer(SuperByteBuffer buffer, Direction facing) {
        if (facing.getAxis() == Direction.Axis.Y) {
            return buffer.rotateXCenteredDegrees(facing == Direction.UP ? 90 : -90);
        } else {
            return buffer.rotateYCenteredDegrees(-facing.toYRot() - 180);
        }
    }

    public static class ThermometerRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState needle;
        public @Nullable SuperByteBufferRenderState maxNeedle;
    }
}
