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

import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class PunchCardItem extends Item implements MenuProvider {
    public PunchCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer() == null)
            return InteractionResult.PASS;
        return use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if(!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if(!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                MenuProvider.openHandledScreen(serverPlayer, this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Punch Card");
    }

    @Override
    public MenuBase<?> createMenu(int syncId, Inventory inventory, Player player, RegistryFriendlyByteBuf extraData) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack.STREAM_CODEC.encode(extraData, heldItem);
        return new PunchCardMenu(syncId, inventory, heldItem);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, tooltipContext, display, tooltipComponents, isAdvanced);
        if(!stack.has(DataComponents.CUSTOM_DATA))
            return;
        if(stack.get(DataComponents.CUSTOM_DATA).copyTag().getBooleanOr("Locked", false)) {
            var author = stack.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("Author", "");
            if(author.isEmpty())
                return;
            var line = Lang.translate("gui.punch_card.author")
                    .add(Component.literal(author)).style(ChatFormatting.GRAY);
            tooltipComponents.accept(line.component());
        }
    }

}
