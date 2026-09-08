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

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.basin.BasinInput;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.utility.RecipeNbtTransfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MixingRecipe.class)
public class MixingRecipeMixin {
    @Inject(
            method = "apply(Lcom/zurrtum/create/content/processing/basin/BasinInput;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/content/processing/basin/BasinInput;acceptOutputs(Ljava/util/List;Ljava/util/List;Z)Z",
                    ordinal = 0
            )
    )
    private void powerGrid$applyTransferNbt(BasinInput input, CallbackInfoReturnable<Boolean> cir, @Local List<ItemStack> outputs) {
        var items = input.items();
        var consumed = new ArrayList<ItemStack>(items.getContainerSize());
        for(int slot = 0; slot < items.getContainerSize(); ++slot) {
            var stack = items.getItem(slot);
            if(!stack.isEmpty())
                consumed.add(stack);
        }
        RecipeNbtTransfer.transfer(consumed, outputs);
    }
}
