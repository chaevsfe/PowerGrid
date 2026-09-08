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
package org.patryk3211.powergrid.electricity.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class ElectricFanRenderer extends SmartBlockEntityRenderer<ElectricFanBlockEntity, ElectricFanRenderer.ElectricFanRenderState> {
    public ElectricFanRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ElectricFanRenderState createRenderState() {
        return new ElectricFanRenderState();
    }

    @Override
    public void extractRenderState(ElectricFanBlockEntity be, ElectricFanRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        Direction direction = blockState.getValue(FACING);

        float time = AnimationTickHolder.getRenderTime(level);
        float speed = be.getSpeed() * 5;
        if (speed > 0)
            speed = Mth.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = Mth.clamp(speed, -64 * 20, -80);
        float angle = (time * speed * 3 / 10f) % 360;
        angle = angle / 180f * (float) Math.PI;

        state.propeller = CachedBuffers.partialFacing(ModdedPartialModels.FAN_PROPELLER, blockState, direction.getOpposite())
                .cardinalLighting(level)
                .light(state.lightCoords)
                .rotateCentered(angle, Direction.get(Direction.AxisDirection.POSITIVE, direction.getAxis()))
                .extractRenderState();
    }

    @Override
    public void submit(ElectricFanRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.propeller != null)
            state.propeller.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
    }

    public static class ElectricFanRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState propeller;
    }
}
