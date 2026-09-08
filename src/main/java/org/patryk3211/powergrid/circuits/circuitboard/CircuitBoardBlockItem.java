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
package org.patryk3211.powergrid.circuits.circuitboard;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.utility.ClientSideAccess;
import org.patryk3211.powergrid.utility.Env;
import org.patryk3211.powergrid.utility.Lang;

import java.util.function.Consumer;

public class CircuitBoardBlockItem extends BlockItem {
    public CircuitBoardBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        Env.CLIENT.runIfCurrent(() -> () -> {
            var level = ClientSideAccess.world();
            if(level == null)
                return;
            var schematic = CircuitSchematic.fromStack(level.registryAccess(), stack);
            if(schematic != null && schematic.getName() != null) {
                tooltipComponents.accept(Lang
                        .translate("circuit_board.tooltip.schematic")
                        .add(Component.literal(schematic.getName()))
                        .style(ChatFormatting.GRAY)
                        .component());
            }
        });
    }
}
