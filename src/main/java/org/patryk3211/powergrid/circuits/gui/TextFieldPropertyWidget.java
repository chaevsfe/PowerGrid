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

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;

import static org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget.PROPERTIES;

@Environment(EnvType.CLIENT)
public class TextFieldPropertyWidget<T, P extends PropertyEntry<T>> extends PropertyWidget<T, P> {
    private final EditBox widget;
    private final Runnable changeMadeCallback;

    public TextFieldPropertyWidget(Font textRenderer, int x, int y, P property, Runnable changeMadeCallback) {
        super(textRenderer, x, y, property);

        this.changeMadeCallback = changeMadeCallback;

        widget = new EditBox(textRenderer, x + 8, y + 6, 46, 12, Component.empty());
        widget.setValue(property.stringValue());
        widget.setTextColor(-1);
        widget.setTextColorUneditable(-1);
        widget.setBordered(false);
        widget.setMaxLength(20);
        widget.setEditable(true);
    }

    @Override
    protected void doRender(@NotNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();

        ctx.blit(RenderPipelines.GUI_TEXTURED, PROPERTIES, x, y, 0, 57, 60, 20, 256, 256);
        widget.extractRenderState(ctx, mouseX, mouseY, partialTicks);
    }

    public void acceptInput() {
        if (!property.get().toString().equals(widget.getValue())) {
            changeMadeCallback.run();
        }
        property.setValue(widget.getValue());
        widget.setFocused(false);
        widget.setValue(property.stringValue());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return widget.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        var clicked = widget.mouseClicked(event, doubled);
        if(widget.isFocused() && !clicked) {
            acceptInput();
        }
        setFocused(clicked);
        return clicked;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN) {
            acceptInput();
            return true;
        }
        return widget.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return widget.charTyped(event);
    }

    @Override
    public void setFocused(boolean newFocused) {
        if (widget.isFocused() && !newFocused) {
            acceptInput();
        }
        widget.setFocused(newFocused);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        return widget.nextFocusPath(navigation);
    }
}
