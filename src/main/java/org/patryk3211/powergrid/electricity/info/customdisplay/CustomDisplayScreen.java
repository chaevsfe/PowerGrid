/*
 * Copyright 2026 patryk3211
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
package org.patryk3211.powergrid.electricity.info.customdisplay;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModIcons;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.SetCustomDisplayC2SPacket;
import org.patryk3211.powergrid.utility.EditableScrollBox;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.Unit;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomDisplayScreen extends AbstractSimiContainerScreen<CustomDisplayMenu> {
    protected static final Identifier TEXTURE = PowerGrid.texture("gui/custom_display");
    private static final int TEXTURE_SIZE = 256;
    private static final int WIDTH = 180, HEIGHT = 107;

    private static final Component TOOLTIP_EXPR = Lang.translateDirect("gui.custom_display.expr");
    private static final Component TOOLTIP_UNIT = Lang.translateDirect("gui.custom_display.unit");
    private static final Component TOOLTIP_PREFIX = Lang.translateDirect("gui.custom_display.prefix");

    private final ItemStack stack;

    private EditBox equationField;
    private EditableScrollBox unitSelector;

    public CustomDisplayScreen(CustomDisplayMenu container, Inventory inv, Component title) {
        super(container, inv, title, WIDTH, HEIGHT);
        stack = new ItemStack(container.contentHolder.getBlockState().getBlock());
    }

    public static CustomDisplayScreen create(
        Minecraft minecraft, MenuType<SmartBlockEntity> type, int syncId, Inventory inventory, Component title, RegistryFriendlyByteBuf extraData
    ) {
        SmartBlockEntity be = getBlockEntity(minecraft, extraData);
        if(be == null)
            return null;
        try(var reporter = new ProblemReporter.ScopedCollector(be.problemPath(), PowerGrid.LOGGER)) {
            be.readClient(TagValueInput.create(reporter, extraData.registryAccess(), extraData.readNbt()));
        }
        return type.create(CustomDisplayScreen::new, syncId, inventory, title, be);
    }

    @Override
    protected void init() {
        super.init();

        var prefixBtn = new IconButton(leftPos + 139, topPos + 48, ModIcons.I_PREFIXES);
        prefixBtn.setToolTip(TOOLTIP_PREFIX);
        prefixBtn.withCallback(() -> menu.enablePrefixes = !menu.enablePrefixes);

        var saveBtn = new IconButton(leftPos + 147, topPos + 83, AllIcons.I_CONFIRM);
        saveBtn.withCallback(this::onClose);

        equationField = new EditBox(font, leftPos + 42, topPos + 31, 114, 9, CommonComponents.EMPTY);
        equationField.setValue(menu.expression);
        equationField.setTextColor(-1);
        equationField.setBordered(false);
        equationField.setMaxLength(100);
        equationField.setEditable(true);
        equationField.setResponder(str -> menu.expression = str);

        unitSelector = new EditableScrollBox(font, leftPos + 42, topPos + 53, 90, 9, CommonComponents.EMPTY, TOOLTIP_UNIT);
        unitSelector.setOptions(Arrays.stream(Unit.values()).map(Unit::string).toList());
        unitSelector.setMaxLength(SetCustomDisplayC2SPacket.MAX_UNIT_STR_LENGTH);
        if(menu.unit != null) {
            unitSelector.setState(menu.unit.ordinal());
        } else {
            unitSelector.setValue(menu.unitStr);
        }
        unitSelector.calling(i -> {
            menu.unit = Unit.values()[i];
            menu.unitStr = null;
        }, str -> {
            menu.unit = null;
            menu.unitStr = str;
        });

        addRenderableWidget(prefixBtn);
        addRenderableWidget(saveBtn);
        addRenderableWidget(new TooltipWidget(leftPos + 15, topPos + 26, 18, 18, TOOLTIP_EXPR));
        addRenderableWidget(new TooltipWidget(leftPos + 15, topPos + 48, 18, 18, TOOLTIP_UNIT));
        addRenderableWidget(unitSelector);
        addRenderableWidget(equationField);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        unitSelector.tick();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        var result = super.keyPressed(event);
        if(getFocused() == equationField)
            return true;
        if(getFocused() == unitSelector)
            return true;
        return result;
    }

    @Override
    protected void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
        if(unitSelector != null && unitSelector.isMouseOver(mouseX, mouseY)) {
            List<Component> tooltip = unitSelector.getToolTip();
            if(tooltip.isEmpty())
                return;
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int bgX = getLeftOfCentered(WIDTH);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, bgX, topPos, 0, 0, WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        graphics.centeredText(font, Lang.translateDirect("gui.custom_display.title"), bgX + WIDTH / 2, topPos + 3, -1);

        if(menu.enablePrefixes) {
            AllGuiTextures.INDICATOR_GREEN.render(graphics, bgX + 139, topPos + 66);
        }

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(2, 2);
        graphics.item(stack, (bgX + 180) / 2, (topPos + 77) / 2);
        pose.popMatrix();
    }

    @Override
    public void onClose() {
        super.onClose();
        ModdedPackets.sendToServer(new SetCustomDisplayC2SPacket(menu.contentHolder, menu.expression, menu.enablePrefixes, menu.unit, menu.unitStr));
    }
}
