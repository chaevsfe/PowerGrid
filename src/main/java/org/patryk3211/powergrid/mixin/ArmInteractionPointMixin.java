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

import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.circuitboard.IncompleteCircuitItem;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ArmInteractionPoint.class)
public abstract class ArmInteractionPointMixin {
    @Shadow protected abstract Container getHandler(ArmBlockEntity blockEntity);

    @Shadow public abstract Level getLevel();

    @Shadow public abstract BlockPos getPos();

    @Unique
    private static ItemStack powerGrid$consumeOne(ItemStack stack) {
        return stack.getCount() <= 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
    }

    @Unique
    @Nullable
    private ItemStack powerGrid$handleDepot(ArmBlockEntity blockEntity, ItemStack stack, boolean simulate) {
        var handler = getHandler(blockEntity);
        if(handler == null)
            return null;
        for(int slot = 0; slot < handler.getContainerSize(); ++slot) {
            var circuit = handler.getItem(slot);
            if(!ModdedItems.INCOMPLETE_CIRCUIT.isIn(circuit))
                continue;
            var newCircuit = IncompleteCircuitItem.insert(getLevel(), circuit, stack);
            if(newCircuit == null)
                return stack;
            if(!simulate) {
                handler.setItem(slot, newCircuit);
                handler.setChanged();
            }
            return powerGrid$consumeOne(stack);
        }
        return null;
    }

    @Unique
    @Nullable
    private ItemStack powerGrid$handleBelt(ItemStack stack, boolean simulate) {
        var beltBE = BeltHelper.getSegmentBE(getLevel(), getPos());
        if(beltBE == null)
            return null;
        var transport = beltBE.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
        if(transport == null)
            return null;
        var found = new MutableBoolean(false);
        var inserted = new MutableBoolean(false);
        transport.handleCenteredProcessingOnAllItems(0.05f, tis -> {
            if(found.isTrue() || !ModdedItems.INCOMPLETE_CIRCUIT.isIn(tis.stack))
                return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            found.setTrue();
            var newCircuit = IncompleteCircuitItem.insert(getLevel(), tis.stack, stack);
            if(newCircuit == null)
                return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            inserted.setTrue();
            if(simulate)
                return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            var result = new TransportedItemStack(newCircuit);
            result.lockedExternally = ModdedItems.INCOMPLETE_CIRCUIT.isIn(newCircuit);
            return TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(List.of(), result);
        });
        if(found.isFalse())
            return null;
        if(inserted.isFalse())
            return stack;
        return powerGrid$consumeOne(stack);
    }

    @Inject(
            method = "insert(Lcom/zurrtum/create/content/kinetics/mechanicalArm/ArmBlockEntity;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void powerGrid$insertAssembleCircuit(ArmBlockEntity blockEntity, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        var level = getLevel();
        if(level == null || level.isClientSide() || stack.isEmpty())
            return;
        ItemStack remainder = null;
        if((Object) this instanceof AllArmInteractionPointTypes.BeltPoint) {
            remainder = powerGrid$handleBelt(stack, simulate);
        } else if((Object) this instanceof AllArmInteractionPointTypes.DepotPoint) {
            remainder = powerGrid$handleDepot(blockEntity, stack, simulate);
        }
        if(remainder != null)
            cir.setReturnValue(remainder);
    }
}
