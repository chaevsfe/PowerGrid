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
package org.patryk3211.powergrid.equipment.zapper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.patryk3211.powergrid.PowerGrid;

@Environment(EnvType.CLIENT)
public class ZapProjectileRenderer extends EntityRenderer<ZapProjectileEntity, ZapProjectileRenderer.ZapProjectileRenderState> {
    public static final Identifier TEXTURE = PowerGrid.texture("entity/zap_projectile");

    private static final float UNIT = 1 / 16f;
    private static final float HALF_UNIT = UNIT / 2f;

    private static final float[] QUAD = {
            -HALF_UNIT, -HALF_UNIT, -HALF_UNIT * 5,
            -HALF_UNIT, -HALF_UNIT, HALF_UNIT * 5,
            HALF_UNIT, HALF_UNIT, HALF_UNIT * 5,
            HALF_UNIT, HALF_UNIT, -HALF_UNIT * 5
    };

    private static final float[] QUAD_UV = {
            0, 0,
            UNIT * 5, 0,
            UNIT * 5, UNIT,
            0, UNIT
    };

    public ZapProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ZapProjectileRenderState createRenderState() {
        return new ZapProjectileRenderState();
    }

    @Override
    public void extractRenderState(ZapProjectileEntity entity, ZapProjectileRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);

        var base = new Matrix4f()
                .translate(0, 0.125f, 0)
                .rotateY(-entity.getViewYRot(tickDelta) * Mth.DEG_TO_RAD)
                .rotateX(-entity.getViewXRot(tickDelta) * Mth.DEG_TO_RAD);

        var transformed = new Vector3f();
        for(int i = 0; i < 4; ++i) {
            base.rotateZ(90 * Mth.DEG_TO_RAD);
            for(int v = 0; v < 4; ++v) {
                base.transformPosition(QUAD[v * 3], QUAD[v * 3 + 1], QUAD[v * 3 + 2], transformed);
                int o = (i * 4 + v) * 3;
                state.vertices[o] = transformed.x;
                state.vertices[o + 1] = transformed.y;
                state.vertices[o + 2] = transformed.z;
            }
        }
    }

    @Override
    public void submit(ZapProjectileRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        queue.submitCustomGeometry(matrices, RenderTypes.entitySolid(TEXTURE), state::renderQuads);
    }

    @Environment(EnvType.CLIENT)
    public static class ZapProjectileRenderState extends EntityRenderState {
        public final float[] vertices = new float[4 * 4 * 3];

        public void renderQuads(PoseStack.Pose pose, VertexConsumer buffer) {
            int light = LightCoordsUtil.FULL_BRIGHT;
            for(int i = 0; i < 4; ++i) {
                for(int v = 0; v < 4; ++v) {
                    int o = (i * 4 + v) * 3;
                    buffer.addVertex(pose.pose(), vertices[o], vertices[o + 1], vertices[o + 2])
                            .setColor(255, 255, 255, 255)
                            .setUv(QUAD_UV[v * 2], QUAD_UV[v * 2 + 1])
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setUv2(light & 65535, light >> 16 & 65535)
                            .setNormal(pose, 0, 1, 0);
                }
            }
        }
    }
}
