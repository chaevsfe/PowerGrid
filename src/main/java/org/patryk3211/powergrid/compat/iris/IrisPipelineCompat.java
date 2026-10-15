/*
 * Copyright 2026 chaevsfe
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
package org.patryk3211.powergrid.compat.iris;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.api.v0.IrisShadowProgram;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

@Environment(EnvType.CLIENT)
public final class IrisPipelineCompat {
    private static final int PIPELINE_API_REVISION = 3;
    private static final int SHADOW_PIPELINE_API_REVISION = 4;
    private static final String SHADOW_PROGRAM = "SHADOW_LIGHTNING";

    private IrisPipelineCompat() {
    }

    public static void register() {
        IrisApi api = IrisApi.getInstance();
        int revision = api.getMinorApiRevision();
        if (revision < PIPELINE_API_REVISION) {
            PowerGrid.LOGGER.warn("Iris API revision {} cannot take Power Grid's render pipelines", revision);
            return;
        }
        boolean shadows = revision >= SHADOW_PIPELINE_API_REVISION;
        boolean assigned = assign(api, shadows, ModdedRenderLayers.getColorPipeline());
        assigned &= assign(api, shadows, ModdedRenderLayers.getAdditiveColorPipeline());
        assigned &= assign(api, shadows, ModdedRenderLayers.getAdditiveCrtPipeline());
        if (assigned) {
            PowerGrid.LOGGER.info("Assigned Iris programs to Power Grid's render pipelines (Iris API revision {})", revision);
        }
    }

    private static boolean assign(IrisApi api, boolean shadows, RenderPipeline pipeline) {
        try {
            api.assignPipeline(pipeline, IrisProgram.BASIC);
        } catch (RuntimeException | LinkageError e) {
            PowerGrid.LOGGER.warn("Iris refused Power Grid render pipeline {}", pipeline.getLocation(), e);
            return false;
        }
        if (!shadows) {
            return true;
        }
        try {
            api.assignPipelineShadow(pipeline, IrisShadowProgram.valueOf(SHADOW_PROGRAM));
            return true;
        } catch (RuntimeException | LinkageError e) {
            PowerGrid.LOGGER.warn("Iris refused a shadow program for Power Grid render pipeline {}", pipeline.getLocation(), e);
            return false;
        }
    }
}
