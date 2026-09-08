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
package org.patryk3211.powergrid.electricity.electromagnet;

import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.EnergyStorage;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.electricity.electromagnet.recipe.MagnetizingRecipe;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;

import java.util.List;
import java.util.Optional;

public class ElectromagnetBlockEntity extends ElectricBlockEntity implements MagnetizingBehaviour.MagnetizingBehaviourSpecifics {
    private ElectricWire wire;
    private MagnetizingBehaviour magnetizingBehaviour;

    public ElectromagnetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @Nullable ThermalBehaviour specifyThermalBehaviour() {
        return ThermalBehaviour.fromConfig(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);

        magnetizingBehaviour = new MagnetizingBehaviour(this);
        behaviours.add(magnetizingBehaviour);
    }

    @Override
    public void electricalTick() {
        applyPower(wire);
    }

    @Override
    public void tick() {
        super.tick();
        if(magnetizingBehaviour.running) {
            wire.setResistance(resistance() * 0.5f);
        } else {
            wire.setResistance(resistance());
        }
    }

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(2);
        wire = builder.connect(resistance(), builder.terminalNode(0), builder.terminalNode(1));
    }

    public static int tryTransferPower(TransportedItemStack input, int power, boolean simulate) {
        return tryTransferPower(new SingleStackStorage() {
            @Override
            protected ItemStack getStack() {
                return input.stack;
            }

            @Override
            protected void setStack(ItemStack stack) {
                input.stack = stack;
            }
        }, power, simulate);
    }

    public static int tryTransferPower(ItemEntity itemEntity, int power, boolean simulate) {
        return tryTransferPower(new SingleStackStorage() {
            @Override
            protected ItemStack getStack() {
                return itemEntity.getItem();
            }

            @Override
            protected void setStack(ItemStack stack) {
                itemEntity.setItem(stack);
            }
        }, power, simulate);
    }

    private static int tryTransferPower(SingleStackStorage storage, int power, boolean simulate) {
        var context = ContainerItemContext.ofSingleSlot(storage);
        var itemEnergy = EnergyStorage.ITEM.find(context.getItemVariant().toStack(), context);
        if (itemEnergy == null) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long toTransfer = power;
            long moved;
            do {
                moved = itemEnergy.insert(toTransfer, transaction);
                toTransfer -= moved;
            } while (!simulate && moved != 0 && toTransfer > 0);
            if (!simulate)
                transaction.commit();
            return (int) (power - toTransfer);
        }
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        var recipe = getRecipe(input.stack);
        if(recipe.isEmpty()) {
            int power = (int)(wire.power() * ModdedConfigs.server().electricity.forgeEnergyPerWatt.getF()) * magnetizingBehaviour.runningTicks;
            int transferred = tryTransferPower(input, power, simulate);
            if (simulate) return transferred > 0;
            outputList.add(input.stack);
            return transferred > 0;
        }
        if(simulate)
            return true;

        var outputs = RecipeApplier.applyRecipeOn(level.getRandom(), 1, new SingleRecipeInput(input.stack), recipe.get().value());
//        for(ItemStack created : outputs) {
//            if(!created.isEmpty()) {
//                onItemPressed(created);
//                break;
//            }
//        }

        outputList.addAll(outputs);
        return true;
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        var item = itemEntity.getItem();
        var recipe = getRecipe(item);
        if(recipe.isEmpty()) {
            int power = (int)(wire.power() * ModdedConfigs.server().electricity.forgeEnergyPerWatt.getF()) * magnetizingBehaviour.runningTicks;
            int transferred = tryTransferPower(itemEntity, power, simulate);
            return transferred > 0;
        }
        if(simulate)
            return true;

        for(var result : RecipeApplier.applyRecipeOn(level.getRandom(), 1, new SingleRecipeInput(item), recipe.get().value())) {
            var created = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
            created.setDefaultPickUpDelay();
            created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .05f));
            level.addFreshEntity(created);
        }
        item.shrink(1);
        return true;
    }

    @Override
    public void onMagnetizationComplete() {

    }

    @Override
    public float getFieldStrength() {
        double I = wire.current();
        if(isVirtual())
            I = wire.potentialDifference() * wire.conductance();
        double field = Math.abs(I * 0.1);
        if(field < 0.25)
            return 0;
        return (float) field;
    }

    public Optional<RecipeHolder<MagnetizingRecipe>> getRecipe(ItemStack item) {
        if(!(level instanceof ServerLevel serverLevel))
            return Optional.empty();
        return serverLevel.recipeAccess().getRecipeFor(ModdedRecipeTypes.MAGNETIZATION, new SingleRecipeInput(item), level);
    }

    public MagnetizingBehaviour getMagnetizingBehaviour() {
        return magnetizingBehaviour;
    }
}
