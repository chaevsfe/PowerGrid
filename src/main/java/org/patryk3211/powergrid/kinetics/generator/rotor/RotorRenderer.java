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
package org.patryk3211.powergrid.kinetics.generator.rotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;

public class RotorRenderer<S extends RotorRenderer.RotorRenderState> implements BlockEntityRenderer<RotorBlockEntity, S> {
    public RotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new RotorRenderState();
    }

    @Override
    public void extractRenderState(RotorBlockEntity rotor, S state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        Level level = SmartBlockEntityRenderer.extractBase(rotor, state, crumblingOverlay);
        state.blockState = rotor.getBlockState();
        state.rotor = null;
        if (VisualizationManager.supportsVisualization(level) && rotor.getType() != ModdedBlockEntities.GENERATOR_LARGE_INDUCTION_ROTOR.get())
            return;

        Direction.Axis axis = ((AbstractRotorBlock) state.blockState.getBlock()).getAssemblyRotationAxis(state.blockState);
        state.rotor = getModelForState(state.blockState)
                .light(state.lightCoords)
                .cardinalLighting(level)
                .rotateCentered(getRotorAngle(rotor, tickProgress), Direction.get(Direction.AxisDirection.POSITIVE, axis))
                .extractRenderState();
    }

    @Override
    public void submit(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.rotor != null)
            state.rotor.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
    }

    public static float getRotorAngle(SmartBlockEntity rotor, float partialTicks) {
        var behaviour = rotor.getBehaviour(RotorBehaviour.TYPE);
        var rotorAngle = behaviour.getAngle() + behaviour.getAngularVelocity() * 0.3f * partialTicks;
        rotorAngle = rotorAngle / 180f * (float) Math.PI;
        return rotorAngle;
    }

    protected SuperByteBuffer getModelForState(BlockState state) {
        return CachedBuffers.block(state);
    }

    public static class RotorRenderState extends BlockEntityRenderState {
        public @UnknownNullability BlockState blockState;
        public @Nullable SuperByteBufferRenderState rotor;
    }
}
