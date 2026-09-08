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
package org.patryk3211.powergrid.electricity.gauge;

import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.TooltipArea;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModIcons;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.EnergyMeterInteractionC2SPacket;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.Unit;

import java.util.List;

import static org.patryk3211.powergrid.network.packets.EnergyMeterInteractionC2SPacket.Action.*;

@Environment(EnvType.CLIENT)
public class EnergyMeterScreen extends AbstractSimiContainerScreen<EnergyMeterMenu> {
    private static final Identifier BACKGROUND = PowerGrid.texture("gui/energy_meter");
    private static final int TEXTURE_SIZE = 256;
    private static final int WIDTH = 246;
    private static final int HEIGHT = 98;

    private TooltipArea valueHover;

    private final ItemStack stack;

    public EnergyMeterScreen(EnergyMeterMenu container, Inventory inv, Component title) {
        super(container, inv, title, WIDTH, HEIGHT);
        stack = new ItemStack(menu.contentHolder.getBlockState().getBlock());
    }

    public static EnergyMeterScreen create(
        Minecraft minecraft, MenuType<EnergyMeterBlockEntity> type, int syncId, Inventory inventory, Component title, RegistryFriendlyByteBuf extraData
    ) {
        EnergyMeterBlockEntity be = getBlockEntity(minecraft, extraData);
        if(be == null)
            return null;
        try(var reporter = new ProblemReporter.ScopedCollector(be.problemPath(), PowerGrid.LOGGER)) {
            be.readClient(TagValueInput.create(reporter, extraData.registryAccess(), extraData.readNbt()));
        }
        return type.create(EnergyMeterScreen::new, syncId, inventory, title, be);
    }

    @Override
    protected void init() {
        setWindowOffset(0, 0);
        super.init();

        IconButton wattHours = new IconButton(leftPos + 7, topPos + 73, ModIcons.I_Wh)
                .withCallback(() -> ModdedPackets.sendToServer(new EnergyMeterInteractionC2SPacket(PRECISION_WH, menu.contentHolder.getBlockPos())));
        wattHours.setToolTip(Lang.translateDirect("gui.energy_meter.wh"));
        IconButton kiloWattHours = new IconButton(leftPos + 25, topPos + 73, ModIcons.I_kWh)
                .withCallback(() -> ModdedPackets.sendToServer(new EnergyMeterInteractionC2SPacket(PRECISION_KWH, menu.contentHolder.getBlockPos())));
        kiloWattHours.setToolTip(Lang.translateDirect("gui.energy_meter.kwh"));
        IconButton reset = new IconButton(leftPos + 185, topPos + 73, AllIcons.I_ROTATE_CCW)
                .withCallback(() -> ModdedPackets.sendToServer(new EnergyMeterInteractionC2SPacket(ZERO, menu.contentHolder.getBlockPos())));
        reset.setToolTip(Lang.translateDirect("gui.energy_meter.reset"));
        valueHover = new TooltipArea(leftPos + 11, topPos + 21, 218, 43);

        IconButton close = new IconButton(leftPos + 213, topPos + 73, AllIcons.I_CONFIRM)
                .withCallback(this::onClose);

        addRenderableWidgets(wattHours, kiloWattHours, reset, valueHover, close);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        double processedEnergy = Math.round(menu.contentHolder.energy * 10) / 10.0;
        processedEnergy = processedEnergy % 100000;
        valueHover.withTooltip(List.of(
                Lang.translate("gui.energy_meter.measured")
                        .style(ChatFormatting.GRAY)
                        .component(),
                Lang.text(" ")
                        .add(Lang.numberConstant(processedEnergy))
                        .add(Lang.text(menu.contentHolder.measurementPrecision ? " " : " k"))
                        .add(Unit.ENERGY.get())
                        .style(ChatFormatting.AQUA)
                        .component()
        ));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int bgX = getLeftOfCentered(WIDTH);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, bgX, topPos, 0, 0, WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        graphics.centeredText(font, title, leftPos + (WIDTH - 8) / 2, topPos + 3, 0xFFFFFFFF);

        var pose = graphics.pose();
        int x = 0;
        for(int i = 10000; i >= 1; i /= 10) {
            float angle = getDialAngle(menu.contentHolder.energy, menu.contentHolder.lastEnergy, i, partialTick);
            pose.pushMatrix();
            pose.translate(leftPos + 31 + x, topPos + 35);
            pose.rotateAbout(angle, 1.5f, 5.5f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 21, 101, 3, 8, TEXTURE_SIZE, TEXTURE_SIZE);
            pose.popMatrix();
            x += 40;
        }

        pose.pushMatrix();
        float angle = getDialAngle(menu.contentHolder.energy, menu.contentHolder.lastEnergy, 0.1, partialTick);
        pose.translate(leftPos + 217, topPos + 53);
        pose.rotateAbout(angle, 0.5f, 3.5f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 25, 103, 1, 4, TEXTURE_SIZE, TEXTURE_SIZE);
        pose.popMatrix();

        int indicatorOffset = menu.contentHolder.measurementPrecision ? 9 : 27;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos + indicatorOffset, topPos + 69, 4, 104, 15, 4, TEXTURE_SIZE, TEXTURE_SIZE);

        pose.pushMatrix();
        pose.scale(2, 2);
        graphics.item(stack, (bgX + 244) / 2, (topPos + 65) / 2);
        pose.popMatrix();
    }

    public static float getDialAngle(double energy, double lastEnergy, double multiplier, double partialTick) {
        float rotation = (float) ((energy / multiplier / 10) % 1);
        float prevRotation = (float) ((lastEnergy / multiplier / 10) % 1);
        if(energy > lastEnergy && rotation < prevRotation) {
            rotation += 1;
        }
        if(energy < lastEnergy && rotation > prevRotation) {
            rotation -= 1;
        }
        return (float) Mth.lerp(partialTick, prevRotation * Math.PI * 2, rotation * Math.PI * 2);
    }
}
