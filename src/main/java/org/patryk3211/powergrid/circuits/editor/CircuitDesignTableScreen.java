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
package org.patryk3211.powergrid.circuits.editor;

import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.TagValueInput;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.gui.CircuitEditButton;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender;
import org.patryk3211.powergrid.collections.ModIcons;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.ChangeScreenC2SPacket;
import org.patryk3211.powergrid.network.packets.SaveSchematicC2SPacket;
import org.patryk3211.powergrid.utility.Lang;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender.COLOR_TRACE_BACK;
import static org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender.COLOR_TRACE_FRONT;

@Environment(EnvType.CLIENT)
public class CircuitDesignTableScreen extends AbstractSimiContainerScreen<CircuitDesignTableMenu> {
    private static final Identifier BACKGROUND = PowerGrid.texture("gui/circuit_design_table");
    private static final int TEXTURE_SIZE = 256;
    private static final int WIDTH = 180;
    private static final int HEIGHT = 92;
    public static final int SCALE = 4;

    private static final Component TOOLTIP_EDIT = Lang.translateDirect("gui.circuit_designer.edit");

    private IconButton confirmButton;
    private IconButton loadButton;
    private CircuitEditButton editButton;
    private IconButton openButton;

    private final CircuitSchematic schematic;

    public CircuitDesignTableScreen(CircuitDesignTableMenu container, Inventory inv, Component title) {
        super(container, inv, title, WIDTH, HEIGHT + 4 + PLAYER_INVENTORY.getHeight());

        schematic = container.contentHolder.getSchematic();
    }

    public static CircuitDesignTableScreen create(
        Minecraft minecraft, MenuType<CircuitDesignTableBlockEntity> type, int syncId, Inventory inventory, Component title, RegistryFriendlyByteBuf extraData
    ) {
        CircuitDesignTableBlockEntity be = getBlockEntity(minecraft, extraData);
        if(be == null)
            return null;
        try(var reporter = new ProblemReporter.ScopedCollector(be.problemPath(), PowerGrid.LOGGER)) {
            be.readClient(TagValueInput.create(reporter, extraData.registryAccess(), extraData.readNbt()));
        }
        return type.create(CircuitDesignTableScreen::new, syncId, inventory, title, be);
    }

    @Override
    protected void init() {
        setWindowOffset(11, 0);

        super.init();

        confirmButton = new IconButton(leftPos + 116 - windowXOffset, topPos + 66, AllIcons.I_CONFIRM);
        loadButton = new IconButton(leftPos + 15 - windowXOffset, topPos + 65, ModIcons.I_RIGHT);
        openButton = new IconButton(leftPos + 15 - windowXOffset, topPos + 21, AllIcons.I_OPEN_FOLDER);
        editButton = new CircuitEditButton(leftPos + 42 - windowXOffset, topPos + 18, 68, 68);

        confirmButton.withCallback(() ->
                ModdedPackets.sendToServer(new SaveSchematicC2SPacket(menu.contentHolder, false)));
        loadButton.withCallback(() ->
                ModdedPackets.sendToServer(new SaveSchematicC2SPacket(menu.contentHolder, true)));

        openButton.withCallback(() ->
                ModdedPackets.sendToServer(new ChangeScreenC2SPacket(menu.contentHolder, 2)));
        editButton.withCallback(() ->
                ModdedPackets.sendToServer(new ChangeScreenC2SPacket(menu.contentHolder, 1)));
        editButton.setTooltip(Tooltip.create(TOOLTIP_EDIT));

        addRenderableWidget(confirmButton);
        addRenderableWidget(loadButton);
        addRenderableWidget(editButton);
        addRenderableWidget(openButton);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(ctx, mouseX, mouseY, partialTick);
        int bgX = getLeftOfCentered(WIDTH);
        int invY = topPos + HEIGHT + 4;
        renderPlayerInventory(ctx, bgX + 2, invY);

        ctx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, bgX, topPos, 0, 0, WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        int x = leftPos + 44 - 11;
        int y = topPos + 20;
        CircuitSchematicRender.renderLayer(schematic.front(), ctx, x, y, SCALE, COLOR_TRACE_FRONT);
        CircuitSchematicRender.renderLayer(schematic.back(), ctx, x, y, SCALE, COLOR_TRACE_BACK);
        CircuitSchematicRender.renderComponents(schematic, ctx, leftPos + 44 - 11, topPos + 20, SCALE);

        ctx.centeredText(font, title, leftPos + (WIDTH - 8) / 2, topPos + 3, 0xFFFFFFFF);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if(!menu.player.isCreative() && !menu.contentHolder.isPowered())
            onClose();
    }
}
