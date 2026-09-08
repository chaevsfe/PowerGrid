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
package org.patryk3211.powergrid.electricity.light.string;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;
import org.patryk3211.powergrid.electricity.wire.powercord.CordRenderer;

import static org.patryk3211.powergrid.electricity.wire.HangingWireRenderer.quad;

@Environment(EnvType.CLIENT)
public class StringLightCordRenderer extends CordRenderer<StringLightCordEntity, StringLightCordRenderState> {
    public static final Identifier TEXTURE = PowerGrid.texture("entity/bulb");

    public StringLightCordRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public StringLightCordRenderState createRenderState() {
        return new StringLightCordRenderState();
    }

    @Override
    protected void beginExtract(StringLightCordEntity entity, StringLightCordRenderState state, float tickDelta) {
        entity.beginRender();
        state.bulbs.clear();
        state.anyGlow = false;
        state.power = Mth.lerp(tickDelta, entity.prevPower, entity.power);
    }

    @Override
    protected void extractSegmentHook(StringLightCordEntity entity, StringLightCordRenderState state,
                                      double x1, double y1, double z1, double x2, double y2, double z2,
                                      double offset, double length, boolean first, boolean last, int light) {
        if(last)
            return;
        var power = state.power;
        int color = entity.nextColor();

        int r = (int) (((color >> 16) & 0xFF) * 0.85f + 48);
        int g = (int) (((color >> 8) & 0xFF) * 0.85f + 32);
        int b = (int) ((color & 0xFF) * 0.70f);
        if(r > 255) r = 255;
        if(g > 255) g = 255;
        if(b > 255) b = 255;

        int bulbColor = ((int) (r * (power * 0.75f + 0.25f)) << 16)
                | ((int) (g * (power * 0.75f + 0.25f)) << 8)
                | ((int) (b * (power * 0.75f + 0.25f)));

        int glowColor = 0;
        if(power > 0.01f) {
            glowColor = (((int) (r * (power * 0.75f)) << 16)
                    | ((int) (g * (power * 0.75f)) << 8)
                    | ((int) (b * (power * 0.75f)))) | 0xFF000000;
            state.anyGlow = true;
        }

        state.bulbs.add(new StringLightCordRenderState.Bulb(x2, y2 - 0.65f / 16f, z2, light, bulbColor | 0xFF000000, glowColor));
    }

    @Override
    public void submit(StringLightCordRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if(state.skip || state.bulbs.isEmpty())
            return;
        queue.submitCustomGeometry(matrices, RenderTypes.entityCutoutCull(TEXTURE), state::renderBulbs);
        if(state.anyGlow)
            queue.order(1).submitCustomGeometry(matrices, ModdedRenderLayers.getAdditiveColor(), state::renderGlows);
    }

    static void bulb(PoseStack.Pose ms, VertexConsumer buffer, double x, double y, double z, int light, int color) {
        final float SIZE = 2 / 16f;
        final float HALF_SIZE = SIZE * 0.5f;

        quad(ms, buffer, light, color,
                x + HALF_SIZE, y       , z + HALF_SIZE,
                x - HALF_SIZE, y       , z + HALF_SIZE,
                x + HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z + HALF_SIZE,
                0, 0, 1,
                2 / 16f, 0, 2 / 16f, 0);
        quad(ms, buffer, light, color,
                x + HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x + HALF_SIZE, y       , z - HALF_SIZE,
                x - HALF_SIZE, y       , z - HALF_SIZE,
                0, 0, -1,
                -2 / 16f, 2 / 16f, -2 / 16f, 2 / 16f);
        quad(ms, buffer, light, color,
                x - HALF_SIZE, y       , z + HALF_SIZE,
                x - HALF_SIZE, y       , z - HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z - HALF_SIZE,
                -1, 0, 0,
                -2 / 16f, 2 / 16f, 2 / 16f, 0);
        quad(ms, buffer, light, color,
                x + HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x + HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x + HALF_SIZE, y       , z + HALF_SIZE,
                x + HALF_SIZE, y       , z - HALF_SIZE,
                1, 0, 0,
                2 / 16f, 0, -2 / 16f, 2 / 16f);
        quad(ms, buffer, light, color,
                x - HALF_SIZE, y, z + HALF_SIZE,
                x + HALF_SIZE, y, z + HALF_SIZE,
                x - HALF_SIZE, y, z - HALF_SIZE,
                x + HALF_SIZE, y, z - HALF_SIZE,
                0, 1, 0,
                2 / 16f, 0, 2 / 16f, 0);
        quad(ms, buffer, light, color,
                x + HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x + HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z - HALF_SIZE,
                0, -1, 0,
                2 / 16f, 2 / 16f, -2 / 16f, 2 / 16f);
    }

    static void glow(PoseStack.Pose ms, VertexConsumer buffer, double x, double y, double z, int color) {
        final float SMALL_OFFSET = 0.125f / 16f;
        final float SIZE = 2.25f / 16f;
        final float HALF_SIZE = SIZE * 0.5f;

        quad(ms, buffer, color,
                x + HALF_SIZE, y + SMALL_OFFSET, z + HALF_SIZE,
                x - HALF_SIZE, y + SMALL_OFFSET, z + HALF_SIZE,
                x + HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z + HALF_SIZE);
        quad(ms, buffer, color,
                x + HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x + HALF_SIZE, y + SMALL_OFFSET, z - HALF_SIZE,
                x - HALF_SIZE, y + SMALL_OFFSET, z - HALF_SIZE);
        quad(ms, buffer, color,
                x - HALF_SIZE, y + SMALL_OFFSET, z + HALF_SIZE,
                x - HALF_SIZE, y + SMALL_OFFSET, z - HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z - HALF_SIZE);
        quad(ms, buffer, color,
                x + HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x + HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x + HALF_SIZE, y + SMALL_OFFSET, z + HALF_SIZE,
                x + HALF_SIZE, y + SMALL_OFFSET, z - HALF_SIZE);
        quad(ms, buffer, color,
                x - HALF_SIZE, y + SMALL_OFFSET, z + HALF_SIZE,
                x + HALF_SIZE, y + SMALL_OFFSET, z + HALF_SIZE,
                x - HALF_SIZE, y + SMALL_OFFSET, z - HALF_SIZE,
                x + HALF_SIZE, y + SMALL_OFFSET, z - HALF_SIZE);
        quad(ms, buffer, color,
                x + HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z + HALF_SIZE,
                x + HALF_SIZE, y - SIZE, z - HALF_SIZE,
                x - HALF_SIZE, y - SIZE, z - HALF_SIZE);
    }
}
