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
package org.patryk3211.powergrid.electricity.light.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

public class LightFixtureRenderer extends SmartBlockEntityRenderer<LightFixtureBlockEntity, LightFixtureRenderer.LightFixtureRenderState> {
    public LightFixtureRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LightFixtureRenderState createRenderState() {
        return new LightFixtureRenderState();
    }

    @Override
    public void extractRenderState(LightFixtureBlockEntity be, LightFixtureRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.bulb = null;
        state.dyedBulb = null;
        state.glow = null;

        var bulbState = be.getBulbState();
        if (bulbState == null)
            return;

        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(LightFixtureBlock.FACING);
        var modelOffset = ((LightFixtureBlock) blockState.getBlock()).modelOffset;

        var model = bulbState.getModel();
        if (model == null)
            return;
        state.bulb = rotateToFacing(CachedBuffers.partial(model, blockState), facing)
                .translate(modelOffset)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .extractRenderState();

        var color = bulbState.getColor();
        int r = 255, g = 255, b = 255;
        if (color != null) {
            var texDif = color.getTextureDiffuseColor();
            r = (texDif & 0xFF0000) >> 16;
            g = (texDif & 0xFF00) >> 8;
            b = texDif & 0xFF;
            state.dyedBulb = rotateToFacing(CachedBuffers.partial(bulbState.getDyedBulb(), blockState), facing)
                    .color(r, g, b, 255)
                    .translate(modelOffset)
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .extractRenderState();
        }

        if (bulbState.isBurned())
            return;

        float a = bulbState.getAlpha();
        if (a > 0) {
            state.glow = rotateToFacing(CachedBuffers.partial(bulbState.getLightModel(), blockState), facing)
                    .translate(modelOffset)
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .color((int) (a * r), (int) (a * g), (int) (a * b), 255)
                    .extractRenderState();
        }
    }

    @Override
    public void submit(LightFixtureRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.bulb != null)
            state.bulb.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
        if (state.dyedBulb != null)
            state.dyedBulb.submit(CreateRenderTypes.translucent(), matrices, queue);
        if (state.glow != null)
            state.glow.submit(ModdedRenderLayers.getAdditive(), matrices, queue.order(1));
    }

    public static SuperByteBuffer rotateToFacing(SuperByteBuffer buffer, Direction facing) {
        return switch (facing) {
            case UP -> buffer;
            case DOWN -> buffer.rotateCentered((float) Math.PI, Direction.EAST);
            default -> {
                buffer.rotateCentered((float) Math.PI * 0.5f, Direction.EAST);
                yield buffer.rotateCentered((float) ((facing.toYRot()) / 180f * Math.PI), Direction.SOUTH);
            }
        };
    }

    public static class LightFixtureRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState bulb;
        public @Nullable SuperByteBufferRenderState dyedBulb;
        public @Nullable SuperByteBufferRenderState glow;
    }
}
