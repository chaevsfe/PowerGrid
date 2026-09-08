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
public class PunchCardBigButton extends AbstractSimiWidget {
    private final int u, v;
    private boolean down;

    public PunchCardBigButton(int x, int y, int u, int v) {
        super(x, y, 18, 18);
        this.u = u;
        this.v = v;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubled) {
        super.onClick(event, doubled);
        down = true;
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        down = false;
    }

    @Override
    protected void doRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if(!visible)
            return;
        isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
        int offset;
        if(isHovered && down)
            offset = 0;
        else if(isHovered)
            offset = 38;
        else
            offset = 19;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PunchCardScreen.BACKGROUND, getX(), getY(), u, v + offset, 18, 18, PunchCardScreen.TEXTURE_SIZE, PunchCardScreen.TEXTURE_SIZE);
    }
}
