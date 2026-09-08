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
package org.patryk3211.powergrid.circuits.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

@Environment(EnvType.CLIENT)
public class CircuitDesignTableRenderer extends SmartBlockEntityRenderer<CircuitDesignTableBlockEntity, CircuitDesignTableRenderer.CircuitDesignTableRenderState> {
    public CircuitDesignTableRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CircuitDesignTableRenderState createRenderState() {
        return new CircuitDesignTableRenderState();
    }

    @Override
    public void extractRenderState(CircuitDesignTableBlockEntity be, CircuitDesignTableRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.glow = null;

        float power = Mth.clamp((be.power() - 15) / 15, 0, 1);
        int a = (int) (power * 192);
        if(a <= 0)
            return;

        var blockState = be.getBlockState();
        state.glow = CachedBuffers.partial(ModdedPartialModels.CIRCUIT_TABLE_GLOW, blockState)
                .rotateYCenteredDegrees(blockState.getValue(CircuitDesignTableBlock.HORIZONTAL_FACING).getAxis() == Direction.Axis.X ? 90 : 0)
                .disableDiffuse()
                .color(a, a, a, 255)
                .extractRenderState();
    }

    @Override
    public void submit(CircuitDesignTableRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if(state.glow != null)
            state.glow.submit(ModdedRenderLayers.getAdditive(), matrices, queue.order(1));
    }

    public static class CircuitDesignTableRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState glow;
    }
}
