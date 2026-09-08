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
package org.patryk3211.powergrid.mixin;

import com.zurrtum.create.foundation.recipe.CreateRollableRecipe;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.patryk3211.powergrid.collections.ModdedDataComponents;
import org.patryk3211.powergrid.collections.ModdedTags;
import org.patryk3211.powergrid.equipment.BoostRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeApplier.class)
public class RecipeApplierMixin {
    @Inject(
            method = "applyRecipeOn(Lnet/minecraft/util/RandomSource;ILnet/minecraft/world/item/crafting/RecipeInput;Lcom/zurrtum/create/foundation/recipe/CreateRollableRecipe;)Ljava/util/List;",
            at = @At("RETURN"),
            order = 1500
    )
    private static void powerGrid$recipeTransferNbt(RandomSource random, int count, RecipeInput input, CreateRollableRecipe<?> recipe, CallbackInfoReturnable<List<ItemStack>> cir) {
        var outputs = cir.getReturnValue();
        if(outputs == null || outputs.isEmpty() || input.size() == 0)
            return;
        var stackIn = input.getItem(0);
        if(recipe instanceof BoostRecipe) {
            if(stackIn.has(ModdedDataComponents.BOOST)) {
                outputs.get(0).set(ModdedDataComponents.BOOST, stackIn.get(ModdedDataComponents.BOOST));
            }
            return;
        }
        if(!stackIn.is(ModdedTags.Item.CIRCUIT_SCHEMATIC_HOLDER.tag) ||
                !stackIn.has(DataComponents.CUSTOM_DATA) || !stackIn.get(DataComponents.CUSTOM_DATA).copyTag().contains("Schematic"))
            return;
        // Modify output with NBT
        for(var output : outputs) {
            if(output.is(ModdedTags.Item.CIRCUIT_SCHEMATIC_HOLDER.tag)) {
                var schematic = stackIn.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty("Schematic").copy();
                CompoundTag compoundTag = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                compoundTag.put("Schematic", schematic);
                output.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
            }
        }
    }
}
