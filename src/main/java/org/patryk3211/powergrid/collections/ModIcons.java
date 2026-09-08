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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.patryk3211.powergrid.PowerGrid;

@Environment(EnvType.CLIENT)
public class ModIcons extends AllIcons {
    public static final Identifier ICON_ATLAS = PowerGrid.texture("gui/icons");
    public static final int ATLAS_SIZE = 64;

    private static int x = 0, y = -1;

    public static final ModIcons I_SERIES = newRow();
    public static final ModIcons I_PARALLEL = next();
    public static final ModIcons I_CONNECT = next();
    public static final ModIcons I_CANCEL = next();

    public static final ModIcons I_LAYER_FRONT = newRow();
    public static final ModIcons I_LAYER_BACK = next();
    public static final ModIcons I_RIGHT = next();
    public static final ModIcons I_TOGGLE = next();

    public static final ModIcons I_UPLOAD = newRow();
    public static final ModIcons I_MOTOR = next();
    public static final ModIcons I_GENERATOR = next();
    public static final ModIcons I_PREFIXES = next();

    public static final ModIcons I_Wh = newRow();
    public static final ModIcons I_kWh = next();

    private final int iconX;
    private final int iconY;

    public ModIcons(int x, int y) {
        super(0, 0);
        iconX = x * 16;
        iconY = y * 16;
    }

    private static ModIcons next() {
        return new ModIcons(++x, y);
    }

    private static ModIcons newRow() {
        x = 0;
        return new ModIcons(x, ++y);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_ATLAS, x, y, iconX, iconY, 16, 16, ATLAS_SIZE, ATLAS_SIZE);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_ATLAS, x, y, iconX, iconY, 16, 16, 16, 16, ATLAS_SIZE, ATLAS_SIZE, color);
    }

    @Override
    public void submit(PoseStack ms, SubmitNodeCollector collector, int color) {
        collector.submitCustomGeometry(ms, RenderTypes.text(ICON_ATLAS), new IconRenderState(iconX, iconY, color));
    }

    private record IconRenderState(int iconX, int iconY, int color) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            var matrix = pose.pose();
            int light = 15728880;
            float u1 = (float) iconX / ATLAS_SIZE;
            float u2 = (float) (iconX + 16) / ATLAS_SIZE;
            float v1 = (float) iconY / ATLAS_SIZE;
            float v2 = (float) (iconY + 16) / ATLAS_SIZE;
            consumer.addVertex(matrix, 0, 0, 0).setColor(color).setUv(u1, v1).setLight(light);
            consumer.addVertex(matrix, 0, 1, 0).setColor(color).setUv(u1, v2).setLight(light);
            consumer.addVertex(matrix, 1, 1, 0).setColor(color).setUv(u2, v2).setLight(light);
            consumer.addVertex(matrix, 1, 0, 0).setColor(color).setUv(u2, v1).setLight(light);
        }
    }
}
