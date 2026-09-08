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

import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessing;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.electricity.heater.IProcessingTypeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FanProcessing.class)
public class FanProcessingMixin {
    @ModifyVariable(
            method = "decrementProcessingTime",
            at = @At(value = "STORE", ordinal = 0),
            index = 3
    )
    private static int powerGrid$modifyProcessingTimeEntity(int time, ItemEntity entity, FanProcessingType type) {
        if(type instanceof IProcessingTypeModifier modifier)
            return modifier.modifyTime(time);
        return time;
    }

    @Inject(
            method = "applyProcessing(Lcom/zurrtum/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/world/level/Level;Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;)Lcom/zurrtum/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;canProcess(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Z"
            )
    )
    private static void powerGrid$modifyProcessingTimeDepot(TransportedItemStack transported, Level world, FanProcessingType type, CallbackInfoReturnable<TransportedItemStackHandlerBehaviour.TransportedResult> cir) {
        if(type instanceof IProcessingTypeModifier modifier) {
            transported.processingTime = modifier.modifyTime(transported.processingTime);
        }
    }
}
