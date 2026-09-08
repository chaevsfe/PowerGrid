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
package org.patryk3211.powergrid.kinetics.punchcard;

import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

@Environment(EnvType.CLIENT)
public class PunchCardButton extends AbstractSimiWidget {
    private final byte[] dataRef;
    private final int row, column;
    private final boolean locked;

    public PunchCardButton(int x, int y, int r, int c, byte[] dataRef, boolean locked) {
        super(x, y, 10, 7);
        this.row = r;
        this.column = c;
        this.dataRef = dataRef;
        this.locked = locked;
    }

    public boolean getState() {
        return (dataRef[column] & (1 << row)) != 0;
    }

    public void flipState() {
        dataRef[column] ^= (byte) (1 << row);
    }

    @Override
    protected void doRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if(!visible)
            return;
        isHovered = !locked && mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
        int u = isHovered ? 240 : 228;
        int v = getState() ? 29 : 20;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PunchCardScreen.BACKGROUND, getX(), getY(), u, v, 10, 7, PunchCardScreen.TEXTURE_SIZE, PunchCardScreen.TEXTURE_SIZE);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubled) {
        if(locked)
            return;
        flipState();
        if(getState())
            runCallback(event.x(), event.y());
    }
}
