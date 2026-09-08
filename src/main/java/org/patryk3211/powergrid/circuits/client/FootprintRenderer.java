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

package org.patryk3211.powergrid.circuits.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.Component;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender;

import static org.patryk3211.powergrid.circuits.schematic.CircuitLayer.GRID_TO_GRID_SCALE;

@Environment(EnvType.CLIENT)
public class FootprintRenderer {
    private static final Identifier ARROWS = PowerGrid.texture("gui/circuit_arrows");

    private static void renderPads(ComponentFootprint footprint, @NotNull GuiGraphicsExtractor ctx, int x, int y) {
        var ms = ctx.pose();
        ms.pushMatrix();
        ms.scale(0.25f, 0.25f);
        for (var point : footprint.getPads().keySet()) {
            int x1 = (point.x() + x) * 4;
            int y1 = (point.y() + y) * 4;
            ctx.fill(x1 + 1, y1 + 1, x1 + 3, y1 + 3, CircuitSchematicRender.COLOR_TERMINAL);
        }
        ms.popMatrix();
    }

    public static void render(ComponentFootprint footprint, @NotNull GuiGraphicsExtractor ctx, @NotNull Component component, int x, int y, boolean hovering) {
        var ms = ctx.pose();
        int width = footprint.getWidth(), height = footprint.getHeight();
        if(footprint.hasOutline()) {
            ms.pushMatrix();
            ms.scale(0.5f, 0.5f);
            ctx.outline(x * 2, y * 2, footprint.getWidth() * GRID_TO_GRID_SCALE * 2, footprint.getHeight() * GRID_TO_GRID_SCALE * 2, CircuitSchematicRender.COLOR_COMPONENT_OUTLINE);
            ms.popMatrix();
        }
        renderPads(footprint, ctx, x, y);

        if(footprint.hasItem() && !hovering) {
            ms.pushMatrix();
            final int maxSize = 2;
            var scale = Math.min(Math.min(footprint.getWidth(), footprint.getHeight()), maxSize) / 16f * GRID_TO_GRID_SCALE;
            if(width > maxSize && height > maxSize) {
                ms.translate(
                        (width - maxSize) * 0.5f,
                        (height - maxSize) * 0.5f
                );
            } else if(width > height) {
                float offset = (width - height) * 0.5f;
                ms.translate(offset, 0);
            } else if(height > width) {
                float offset = (height - width) * 0.5f;
                ms.translate(0, offset);
            }
            ms.scale(scale, scale);
            var stack = footprint.getRenderedStack(component);
            if(stack != null)
                ctx.item(stack, (int) (x / scale), (int) (y / scale));
            ms.popMatrix();
        }
        var arrow = footprint.getArrow();
        if(arrow != null) {
            ms.pushMatrix();
            int u = (arrow.ordinal() % 2) * 8;
            int v = (arrow.ordinal() / 2) * 8;
            ms.translate(x + (footprint.getWidth() * GRID_TO_GRID_SCALE * 0.5f), y + (footprint.getHeight() * GRID_TO_GRID_SCALE * 0.5f));

            switch(arrow) {
                case RIGHT -> ms.translate(width / 2, 0);
                case LEFT -> ms.translate(-width / 2, 0);
                case DOWN -> ms.translate(0, height / 2);
                case UP -> ms.translate(0, -height / 2);
            }

            ms.scale(0.25f, 0.25f);
            ms.translate(-4, -4);
            ctx.blit(RenderPipelines.GUI_TEXTURED, ARROWS, 0, 0, u, v, 8, 8, 16, 16);

            ms.popMatrix();
        }
    }

    public static void renderPadIndices(ComponentFootprint footprint, @NotNull GuiGraphicsExtractor ctx, @NotNull Font textRenderer, int x, int y) {
        var ms = ctx.pose();
        ms.pushMatrix();
        int scale = 12;
        ms.translate(0.5f, 0.5f);
        ms.scale(1.0f / scale, 1.0f / scale);
        for(var entry : footprint.getPads().entrySet()) {
            var point = entry.getKey();
            int x1 = (point.x() + x) * scale;
            int y1 = (point.y() + y) * scale;

            var text = entry.getValue().shortText();
            if(text == null)
                continue;
            int width = textRenderer.width(text);
            ctx.text(textRenderer, text, x1 - width / 2, y1 - 4, -1, false);
        }
        ms.popMatrix();
    }
}
