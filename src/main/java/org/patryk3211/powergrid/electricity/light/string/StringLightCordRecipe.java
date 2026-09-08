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
package org.patryk3211.powergrid.electricity.light.string;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedDataComponents;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;

import java.util.ArrayList;
import net.minecraft.core.component.DataComponents;

public class StringLightCordRecipe extends CustomRecipe {
    public static final RecipeSerializer<StringLightCordRecipe> SERIALIZER =
            new RecipeSerializer<>(MapCodec.unit(new StringLightCordRecipe()), StreamCodec.unit(new StringLightCordRecipe()));

    public StringLightCordRecipe() {
    }


    @Override
    public boolean matches(CraftingInput input, Level level) {
        var hasCord = false;

        for(var stack : input.items()) {
            if(ModdedItems.STRING_LIGHT_CORD.isIn(stack)) {
                if(hasCord)
                    return false;
                hasCord = true;
            } else if(!stack.isEmpty() && !(stack.getItem() instanceof DyeItem)) {
                return false;
            }
        }

        return hasCord;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        var result = ModdedItems.STRING_LIGHT_CORD.asStack();
        var colors = new ArrayList<DyeColor>();

        for(var stack : input.items()) {
            if(stack.getItem() instanceof DyeItem dye) {
                colors.add(stack.get(DataComponents.DYE));
            }
        }

        if(!colors.isEmpty())
            result.set(ModdedDataComponents.LIGHT_PATTERN, PatternData.of(colors));
        return result;
    }

    @Override
    public RecipeSerializer<StringLightCordRecipe> getSerializer() {
        return ModdedRecipeTypes.STRING_LIGHT_CORD_SERIALIZER;
    }
}
