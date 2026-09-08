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
package org.patryk3211.powergrid.electricity.light.factorylight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

public class FactoryLightRenderer extends SmartBlockEntityRenderer<FactoryLightBlockEntity, FactoryLightRenderer.FactoryLightRenderState> {
    public FactoryLightRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FactoryLightRenderState createRenderState() {
        return new FactoryLightRenderState();
    }

    @Override
    public void extractRenderState(FactoryLightBlockEntity be, FactoryLightRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.glow = null;

        var bulbState = be.getBulbState();
        if (bulbState == null || bulbState.isBurned())
            return;

        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        int part = blockState.getValue(FactoryLightBlock.PART);
        int rotation = 0;
        PartialModel lightModel = switch (part) {
            case 0 -> ModdedPartialModels.FL_RAYS_SINGLE;
            case 1 -> ModdedPartialModels.FL_RAYS_FRONT;
            case 2 -> ModdedPartialModels.FL_RAYS_CENTER;
            case 3 -> ModdedPartialModels.FL_RAYS_BACK;
            case 4 -> {
                rotation = 90;
                yield ModdedPartialModels.FL_RAYS_FRONT;
            }
            case 5 -> {
                rotation = 90;
                yield ModdedPartialModels.FL_RAYS_CENTER;
            }
            case 6 -> {
                rotation = 90;
                yield ModdedPartialModels.FL_RAYS_BACK;
            }
            default -> throw new IllegalStateException();
        };

        int a = (int) (bulbState.getAlpha() * 255);
        if (a > 0) {
            state.glow = CachedBuffers.partial(lightModel, blockState)
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .rotateYCenteredDegrees(rotation)
                    .color(a, a, a, 255)
                    .extractRenderState();
        }
    }

    @Override
    public void submit(FactoryLightRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.glow != null)
            state.glow.submit(ModdedRenderLayers.getAdditive(), matrices, queue.order(1));
    }

    public static class FactoryLightRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState glow;
    }
}
