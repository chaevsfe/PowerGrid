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
package org.patryk3211.powergrid.electricity.transformer;

import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsScreen;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.TransformerWindingC2SPacket;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TransformerWindingScreen extends ValueSettingsScreen {
    public static final Identifier CAP_TEXTURE = PowerGrid.texture("gui/brass_cover");
    private static final int TEXTURE_SIZE = 256;

    public static ValueSettingsBoard makeBoard(TransformerBlock block) {
        return new ValueSettingsBoard(
                Lang.translateDirect("gui.transformer.turns"),
                block.getMaxTurns(),
                10,
                List.of(Component.literal("N")),
                new ValueSettingsFormatter(TransformerWindingScreen::formatSettings)
        );
    }

    public static MutableComponent formatSettings(ValueSettings settings) {
        return Lang.number(Math.max(1, Math.abs(settings.value()))).component();
    }

    private final InteractionHand hand;
    private final int cap;

    private static int interactionTicks = -1;
    private static TransformerWindingScreen screen = null;

    public static boolean beginInteraction(Supplier<TransformerWindingScreen> potentialScreen) {
        if(interactionTicks == -1) {
            interactionTicks = 0;
            screen = potentialScreen.get();
            return true;
        }
        return false;
    }

    public TransformerWindingScreen(TransformerBlock block, InteractionHand hand, int current, int primaryTurns) {
        super(null, makeBoard(block), new ValueSettings(0, current), setting -> {}, 1000);
        this.hand = hand;
        this.cap = block.getMaxTurns() - primaryTurns;
    }

    @Override
    public ValueSettings getClosestCoordinate(int mouseX, int mouseY) {
        var value = super.getClosestCoordinate(mouseX, mouseY);
        if(value.value() > cap)
            return new ValueSettings(value.row(), cap);
        return value;
    }

    private static void renderCropped(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int u, int v) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CAP_TEXTURE, x, y, u, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public void renderBarCap(GuiGraphicsExtractor graphics, int x, int y, int width, ValueSettingsBoard board) {
        int milestoneCount = board.maxValue() / board.milestoneInterval();
        if(milestoneCount <= 0)
            return;
        int milestoneSegmentWidth = width / milestoneCount;
        int scale = board.maxValue() > 128 ? 1 : 2;

        var milestone = cap / board.milestoneInterval();
        int toMilestoneOffset = milestoneSegmentWidth * milestone + 8 / scale;
        var milestoneFraction = (float) (cap - milestone * board.milestoneInterval()) / board.milestoneInterval();

        int offset = (int) (toMilestoneOffset + (milestoneSegmentWidth - 7 + 1) * milestoneFraction);
        x += offset;
        x -= 1;
        width -= offset;
        width += 2;
        if(width <= 2)
            return;

        int sideWidth = Math.min(3, width / 2);
        int centerWidth = width - sideWidth * 2;

        renderCropped(graphics, x, y, sideWidth, 10, 0, 0);
        renderCropped(graphics, x + sideWidth + centerWidth, y, Math.min(3, width - sideWidth), 10, 253, 0);

        for (int w = 0; w < centerWidth; w += 250 - 1) {
            var segLen = Math.min(250 - 1, centerWidth - w);
            var segX = x + w + sideWidth;
            renderCropped(graphics, segX, y, segLen, 10, 3, 0);
        }
    }

    public void renderBarCapMilestone(GuiGraphicsExtractor graphics, int x, int y, int milestone, ValueSettingsBoard board) {
        var milestoneValue = milestone * board.milestoneInterval();
        if(milestoneValue > cap)
            renderCropped(graphics, x, y + 1, 7, 8, 0, 11);
    }

    @Override
    protected void saveAndClose(double pMouseX, double pMouseY) {
        ValueSettings closest = getClosestCoordinate((int) pMouseX, (int) pMouseY);
        var value = Math.max(closest.value(), 1);
        ModdedPackets.sendToServer(new TransformerWindingC2SPacket(value, hand));
        onClose();
    }

    public static void clientTick() {
        if(interactionTicks == -1)
            return;

        if(++interactionTicks <= 3) {
            var mc = Minecraft.getInstance();
            if (!mc.options.keyUse.isDown()) {
                interactionTicks = -1;
                return;
            }

            if(interactionTicks == 3)
                ScreenOpener.open(screen);
        } else {
            interactionTicks = -1;
        }
    }
}
