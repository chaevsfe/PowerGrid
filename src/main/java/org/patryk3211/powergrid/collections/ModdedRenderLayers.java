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
package org.patryk3211.powergrid.collections;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.patryk3211.powergrid.PowerGrid;

@Environment(EnvType.CLIENT)
public class ModdedRenderLayers {
    private static final RenderPipeline ADDITIVE_COLOR_PIPELINE = RenderPipelines.register(RenderPipeline
            .builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(PowerGrid.asResource("pipeline/additive_color"))
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withCull(false)
            .build());

    private static final RenderPipeline ADDITIVE_CRT_PIPELINE = RenderPipelines.register(RenderPipeline
            .builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(PowerGrid.asResource("pipeline/additive_crt"))
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withCull(false)
            .build());

    private static final RenderType ADDITIVE_COLOR = RenderType.create(
            "powergrid_additive_color",
            RenderSetup.builder(ADDITIVE_COLOR_PIPELINE).createRenderSetup());

    private static final RenderType ADDITIVE_CRT = RenderType.create(
            "powergrid_additive_crt",
            RenderSetup.builder(ADDITIVE_CRT_PIPELINE).createRenderSetup());

    public static RenderType getColor() {
        return RenderTypes.debugQuads();
    }

    public static RenderType getAdditiveCrt() {
        return ADDITIVE_CRT;
    }

    public static RenderType getAdditiveColor() {
        return ADDITIVE_COLOR;
    }

    public static RenderType getAdditive() {
        return CreateRenderTypes.additive();
    }

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }
}
