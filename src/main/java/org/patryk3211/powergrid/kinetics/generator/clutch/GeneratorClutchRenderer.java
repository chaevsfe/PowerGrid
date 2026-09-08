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
package org.patryk3211.powergrid.kinetics.generator.clutch;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

import static org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer.getRotorAngle;

public class GeneratorClutchRenderer extends KineticBlockEntityRenderer<GeneratorClutchBlockEntity, GeneratorClutchRenderer.GeneratorClutchRenderState> {
    public GeneratorClutchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public GeneratorClutchRenderState createRenderState() {
        return new GeneratorClutchRenderState();
    }

    @Override
    public void extractRenderState(GeneratorClutchBlockEntity be, GeneratorClutchRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.assembly = null;
        if (state.support)
            return;

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        state.assembly = CachedBuffers.partialFacing(ModdedPartialModels.CLUTCH_SHAFT, blockState, facing.getOpposite())
                .rotateCentered(getRotorAngle(be, tickProgress), Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()))
                .cardinalLighting(state.cardinalLighting)
                .light(state.lightCoords)
                .extractRenderState();
    }

    @Override
    public void submit(GeneratorClutchRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.assembly != null)
            state.assembly.submit(matrices, queue);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(GeneratorClutchBlockEntity be, GeneratorClutchRenderState state) {
        BlockState blockState = be.getBlockState();
        return CachedBuffers.partialFacing(ModdedPartialModels.SHAFT_BIT, blockState, blockState.getValue(GeneratorClutchBlock.FACING));
    }

    public static class GeneratorClutchRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState assembly;
    }
}
