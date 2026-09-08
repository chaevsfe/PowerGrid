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
package org.patryk3211.powergrid.electricity.info;

import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.patryk3211.powergrid.PowerGrid;

import java.util.List;

/**
 * Kept in its own client only class so the client only {@code TooltipModifier} return type never
 * appears in a method descriptor belonging to a class loaded on a dedicated server.
 */
@Environment(EnvType.CLIENT)
public class PowerGridTooltipModifiers {
    public static void register() {
        PowerGrid.REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    /**
     * Electric properties run off the item tooltip callback rather than the modifier registry
     * because they need the real stack and flag; several implementations read stack components
     * (the portable battery reads its charge).
     */
    public static void appendElectricProperties(ItemStack stack, TooltipFlag flag, List<Component> lines) {
        IHaveElectricProperties properties = propertiesOf(stack);
        if (properties == null)
            return;
        ElectricPropertiesUtils.modify(properties, stack, Minecraft.getInstance().player, flag, lines);
    }

    private static IHaveElectricProperties propertiesOf(ItemStack stack) {
        if (stack.getItem() instanceof IHaveElectricProperties properties)
            return properties;
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IHaveElectricProperties properties)
            return properties;
        return null;
    }
}
