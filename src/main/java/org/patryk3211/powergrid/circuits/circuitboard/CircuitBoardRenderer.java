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
package org.patryk3211.powergrid.circuits.circuitboard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.client.ComponentDrawCall;
import org.patryk3211.powergrid.circuits.client.ComponentRenderers;
import org.patryk3211.powergrid.circuits.components.IRenderedComponent;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CircuitBoardRenderer extends SmartBlockEntityRenderer<CircuitBoardBlockEntity, CircuitBoardRenderer.CircuitBoardRenderState> {
    public CircuitBoardRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CircuitBoardRenderState createRenderState() {
        return new CircuitBoardRenderState();
    }

    @Override
    public void extractRenderState(CircuitBoardBlockEntity be, CircuitBoardRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.components.clear();

        var components = be.getComponents(IRenderedComponent.class);
        if(components.isEmpty())
            return;

        var blockState = be.getBlockState();
        state.angleY = CircuitBoardBlock.getAngleY(blockState);
        state.angleX = CircuitBoardBlock.getAngleX(blockState);

        for(var placed : components) {
            if(placed.destroyed)
                continue;
            var renderer = ComponentRenderers.get(placed.component);
            if(renderer == null)
                continue;
            var calls = new ArrayList<ComponentDrawCall>();
            renderer.extract(be, placed, tickProgress, state.lightCoords, OverlayTexture.NO_OVERLAY, calls);
            if(!calls.isEmpty())
                state.components.add(new PlacedDraws(placed.x, placed.y, calls));
        }
    }

    @Override
    public void submit(CircuitBoardRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if(state.components.isEmpty())
            return;

        for(var component : state.components) {
            matrices.pushPose();
            TransformStack.of(matrices)
                    .center()
                    .rotateYDegrees(state.angleY)
                    .rotateXDegrees(state.angleX)
                    .uncenter()
                    .translate(component.x() / 16f, 2 / 16f, component.y() / 16f);
            for(var call : component.calls())
                call.submit(matrices, queue);
            matrices.popPose();
        }
    }

    public record PlacedDraws(int x, int y, List<ComponentDrawCall> calls) { }

    public static class CircuitBoardRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public final List<PlacedDraws> components = new ArrayList<>();
        public int angleX;
        public int angleY;
    }
}
