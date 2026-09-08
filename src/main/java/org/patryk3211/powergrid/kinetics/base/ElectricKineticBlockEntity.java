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
package org.patryk3211.powergrid.kinetics.base;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.advancements.PGAdvancementBehaviour;
import org.patryk3211.powergrid.advancements.PowerGridAdvancement;
import org.patryk3211.powergrid.collections.ModdedAdvancements;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.electricity.sim.AbstractElectricWire;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class ElectricKineticBlockEntity extends KineticBlockEntity implements IElectricEntity {
    protected ElectricBehaviour electricBehaviour;
    @Nullable
    protected ThermalBehaviour thermalBehaviour;

    public ElectricKineticBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void setUnsaved() {
        if(level != null && !level.isClientSide()) {
            level.blockEntityChanged(worldPosition);
        }
    }

    @Override
    public void tick() {
        if(!level.isClientSide() || isVirtual())
            electricalTick();
        super.tick();
        if(level.isClientSide())
            tickAudio();
    }

    public void electricalTick() {

    }

    @Environment(EnvType.CLIENT)
    public void tickAudio() {

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        electricBehaviour = new ElectricBehaviour(this);
        behaviours.add(electricBehaviour);

        thermalBehaviour = specifyThermalBehaviour();
        if(thermalBehaviour != null) {
            behaviours.add(thermalBehaviour);
            registerAwardables(behaviours, ModdedAdvancements.BLOW_UP);
        }
    }

    @Nullable
    public ThermalBehaviour specifyThermalBehaviour() {
        return null;
    }

    protected void applyPower(AbstractElectricWire wire) {
        if(thermalBehaviour != null)
            thermalBehaviour.applyWirePower(wire);
    }

    @Override
    public void remove() {
        super.remove();
        if(electricBehaviour != null) {
            electricBehaviour.remove();
        }
    }

    public void registerAwardables(List<BlockEntityBehaviour<?>> behaviours, PowerGridAdvancement... advancements) {
        for(var behaviour : behaviours) {
            if(behaviour instanceof PGAdvancementBehaviour ab) {
                ab.add(advancements);
                return;
            }
        }
        behaviours.add(new PGAdvancementBehaviour(this, advancements));
    }

    public void award(PowerGridAdvancement advancement) {
        var behaviour = getBehaviour(PGAdvancementBehaviour.TYPE);
        if(behaviour != null)
            behaviour.awardPlayer(advancement);
    }

    public void awardIfNear(PowerGridAdvancement advancement, int range) {
        var behaviour = getBehaviour(PGAdvancementBehaviour.TYPE);
        if(behaviour != null)
            behaviour.awardPlayerIfNear(advancement, range);
    }
}
