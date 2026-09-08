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
package org.patryk3211.powergrid.circuits.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;

import static org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget.PROPERTIES;

@Environment(EnvType.CLIENT)
public class ConstantPropertyWidget<T> extends PropertyWidget<T, PropertyEntry<T>> {
    protected ConstantPropertyWidget(Font textRenderer, int x, int y, PropertyEntry<T> property) {
        super(textRenderer, x, y, property);
    }

    @Override
    protected void doRender(@NotNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();

        ctx.blit(RenderPipelines.GUI_TEXTURED, PROPERTIES, x, y, 0, 99, 60, 20, 256, 256);

        var text = property.stringValue();
        int len = textRenderer.width(text);
        ctx.text(textRenderer, text, x + 60 - len - 8, y + 6, 0xFF404040, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        return false;
    }
}
