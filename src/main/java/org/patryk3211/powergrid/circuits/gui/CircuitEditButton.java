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

import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CircuitEditButton extends AbstractSimiWidget {
    public CircuitEditButton(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public void doRender(@NotNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTicks) {
        if(visible) {
            if(isMouseOver(mouseX, mouseY)) {
                ctx.horizontalLine(getX() - 1, getX() + width, getY() - 1, 0xFFFFFFFF);
                ctx.verticalLine(getX() - 1, getY() - 1, getY() + height + 1, 0xFFFFFFFF);
                ctx.horizontalLine(getX(), getX() + width, getY() + height, 0xFF888888);
                ctx.verticalLine(getX() + width, getY() - 1, getY() + height + 1, 0xFF888888);
            }
        }
    }
}
