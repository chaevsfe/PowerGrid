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
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.patryk3211.powergrid.circuits.client.FootprintRenderer;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

import java.util.function.BiConsumer;

import static org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender.CIRCUIT_SCALE;
import static org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender.TRACE_PADDING;
import static org.patryk3211.powergrid.circuits.schematic.CircuitLayer.GRID_SIZE;
import static org.patryk3211.powergrid.circuits.schematic.CircuitLayer.GRID_TO_GRID_SCALE;

@Environment(EnvType.CLIENT)
public class CircuitEditWidget extends AbstractSimiWidget {
    private int scale = CIRCUIT_SCALE;

    private final Font textRenderer;
    private boolean selectStarted = false;
    private int startX, startY;

    private SelectMode selectMode = SelectMode.NONE;
    private int selectionColor = 0;
    private SelectCallback selectionCallback = null;
    private Runnable selectionCancelledCallback = null;

    private PlacedComponent placedComponent = null;
    private BiConsumer<Integer, Integer> placementCallback;

    private final CircuitSchematic schematic;

    public CircuitEditWidget(Font textRenderer, CircuitSchematic schematic, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.textRenderer = textRenderer;
        this.schematic = schematic;
    }

    @Override
    protected void doRender(@NotNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTicks) {
        if (!isHovered)
            return;
        int x = getX();
        int y = getY();

        int gridX = (mouseX - x) / scale;
        int gridY = (mouseY - y) / scale;
        if(gridX >= GRID_SIZE || gridY >= GRID_SIZE)
            return;

        var ms = ctx.pose();
        ms.translate(x, y);
        ms.scale(scale, scale);

        if(placedComponent != null) {
            var footprint = placedComponent.footprint();
            gridX /= GRID_TO_GRID_SCALE;
            gridY /= GRID_TO_GRID_SCALE;
            int offsetX = footprint.getWidth() / 2;
            int offsetY = footprint.getHeight() / 2;
            FootprintRenderer.render(footprint, ctx, placedComponent.component, (gridX - offsetX) * GRID_TO_GRID_SCALE, (gridY - offsetY) * GRID_TO_GRID_SCALE, true);
            FootprintRenderer.renderPadIndices(footprint, ctx, textRenderer, (gridX - offsetX) * GRID_TO_GRID_SCALE, (gridY - offsetY) * GRID_TO_GRID_SCALE);

            ms.pushMatrix();
            ms.scale(1f / scale, 1f / scale);
            int color = schematic.canPlace(placedComponent, gridX - offsetX, gridY - offsetY) ? 0x8080FF80 : 0x80FF8080;
            ctx.outline((gridX - offsetX) * scale * GRID_TO_GRID_SCALE, (gridY - offsetY) * scale * GRID_TO_GRID_SCALE, footprint.getWidth() * scale * GRID_TO_GRID_SCALE, footprint.getHeight() * scale * GRID_TO_GRID_SCALE, color);
            ms.popMatrix();
            return;
        }

        ms.pushMatrix();
        ms.scale(1f / scale, 1f / scale);

        if (selectMode == SelectMode.NONE || selectMode == SelectMode.POINT) {
            // Draw cursor
            ctx.outline(gridX * scale, gridY * scale, scale, scale, 0xFFAAAAFF);
        }
        else if (!selectStarted) {
            // Extend or reduce select cursor by the same rules for rendering selections below
            if (selectMode == SelectMode.LINE) {
                ctx.fill(gridX * scale + TRACE_PADDING, gridY * scale + TRACE_PADDING,
                        gridX * scale + scale - TRACE_PADDING, gridY * scale + scale - TRACE_PADDING,
                        selectionColor);
            }
            else {
                ctx.fill(
                    gridX * scale - (gridX > 0 ? TRACE_PADDING : 0),
                    gridY * scale - (gridY > 0 ? TRACE_PADDING : 0),
                    gridX * scale + scale + (gridX < GRID_SIZE - 1 ? TRACE_PADDING : 0),
                    gridY * scale + scale + (gridY < GRID_SIZE - 1 ? TRACE_PADDING : 0),
                    selectionColor
                );
            }
        }
        else if (selectMode == SelectMode.LINE) {
            int lenX = Math.abs(gridX - startX) + 1;
            int lenY = Math.abs(gridY - startY) + 1;
            if (lenX >= lenY) {
                // Horizontal
                var x1 = Math.min(gridX, startX);
                var x2 = x1 + lenX;
                ctx.fill(x1 * scale + TRACE_PADDING, startY * scale + TRACE_PADDING,
                        x2 * scale - TRACE_PADDING, startY * scale + scale - TRACE_PADDING,
                        selectionColor);
            }
            else {
                // Vertical
                var y1 = Math.min(gridY, startY);
                var y2 = y1 + lenY;
                ctx.fill(startX * scale + TRACE_PADDING, y1 * scale + TRACE_PADDING,
                        startX * scale + scale - TRACE_PADDING, y2 * scale - TRACE_PADDING,
                        selectionColor);
            }
        }
        else if (selectMode == SelectMode.AREA) {
            var x1 = Math.min(startX, gridX);
            var y1 = Math.min(startY, gridY);
            var x2 = Math.max(startX, gridX) + 1;
            var y2 = Math.max(startY, gridY) + 1;

            // Go over boundaries by a bit to show that neighbors will be disconnected
            ctx.fill(
                x1 * scale - (x1 > 0 ? TRACE_PADDING : 0),
                y1 * scale - (y1 > 0 ? TRACE_PADDING : 0),
                x2 * scale + (x2 < GRID_SIZE ? TRACE_PADDING : 0),
                y2 * scale + (y2 < GRID_SIZE ? TRACE_PADDING : 0),
                selectionColor
            );
        }

        ms.popMatrix();
    }

    private void handleCallback(int endX, int endY) {
        SelectionResult result = SelectionResult.END;
        if(selectionCallback != null) {
            int x1, y1, x2, y2;
            switch(selectMode) {
                case POINT -> {
                    x1 = x2 = endX;
                    y1 = y2 = endY;
                }
                case LINE -> {
                    int lenX = Math.abs(endX - startX);
                    int lenY = Math.abs(endY - startY);
                    if(lenX >= lenY) {
                        // Horizontal
                        x1 = Math.min(endX, startX);
                        x2 = x1 + lenX;
                        y1 = y2 = startY;
                        endY = startY;
                    } else {
                        // Vertical
                        y1 = Math.min(endY, startY);
                        y2 = y1 + lenY;
                        x1 = x2 = startX;
                        endX = startX;
                    }
                }
                case AREA -> {
                    x1 = Math.min(startX, endX);
                    y1 = Math.min(startY, endY);
                    x2 = Math.max(startX, endX);
                    y2 = Math.max(startY, endY);
                }
                default -> throw new IllegalStateException("Cannot handle callback without valid selection mode");
            }
            result = selectionCallback.accept(x1, y1, x2, y2, endX, endY);
        }
        switch(result) {
            case CONTINUE -> {
                startX = endX;
                startY = endY;
                selectStarted = true;
            }
            case END -> {
                selectStarted = false;
                selectMode = SelectMode.NONE;
                selectionCallback = null;
            }
            case BEGIN_NEW -> {
                selectStarted = false;
            }
            case IGNORE -> { }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        int button = event.button();
        int gridX = (int) ((event.x() - getX()) / scale);
        int gridY = (int) ((event.y() - getY()) / scale);
        if(gridX < 0 || gridX >= GRID_SIZE || gridY < 0 || gridY >= GRID_SIZE)
            return false;

        if(placedComponent != null) {
            gridX /= GRID_TO_GRID_SCALE;
            gridY /= GRID_TO_GRID_SCALE;
            var footprint = placedComponent.footprint();
            int offsetX = footprint.getWidth() / 2;
            int offsetY = footprint.getHeight() / 2;
            if(button == InputConstants.MOUSE_BUTTON_LEFT) {
                placementCallback.accept(gridX - offsetX, gridY - offsetY);
            }
            return true;
        }

        if(button == InputConstants.MOUSE_BUTTON_LEFT && selectMode != SelectMode.NONE) {
            if(selectMode == SelectMode.POINT) {
                startX = gridX;
                startY = gridY;
                handleCallback(gridX, gridY);
            } else {
                if(!selectStarted) {
                    startX = gridX;
                    startY = gridY;
                    selectStarted = true;
                    playDownSound(Minecraft.getInstance().getSoundManager());
                } else {
                    handleCallback(gridX, gridY);
                }
            }
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            cancelSelection();
        }
        return true;
    }

    public void requestSelection(SelectMode mode, int color, SelectCallback callback) {
        selectStarted = false;
        selectMode = mode;
        selectionColor = color;
        selectionCallback = callback;
    }

    public void setSelectionCancelledCallback(Runnable callback) {
        selectionCancelledCallback = callback;
    }

    public void cancelSelection() {
        if(selectMode != SelectMode.NONE) {
            selectMode = SelectMode.NONE;
            selectionCallback = null;
            selectStarted = false;
            if(selectionCancelledCallback != null)
                selectionCancelledCallback.run();
        }
    }

    public void componentPlacement(PlacedComponent component, BiConsumer<Integer, Integer> callback) {
        placedComponent = component;
        placementCallback = callback;
    }

    public void stopComponentPlacement() {
        placedComponent = null;
        placementCallback = null;
    }

    public enum SelectMode {
        NONE, POINT, LINE, AREA
    }

    public enum SelectionResult {
        IGNORE, CONTINUE, BEGIN_NEW, END
    }

    public interface SelectCallback {
        SelectionResult accept(int x1, int y1, int x2, int y2, int clickX, int clickY);
    }
}
