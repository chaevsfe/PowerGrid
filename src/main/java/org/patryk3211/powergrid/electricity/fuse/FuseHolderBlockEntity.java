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
package org.patryk3211.powergrid.electricity.fuse;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.catnip.math.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.particles.SparkParticleData;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FuseHolderBlockEntity extends ElectricBlockEntity {
    private FuseSettingBehaviour setting;
    private SwitchedWire fuseWire;

    private FuseState state;

    public FuseHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state.getValue(FuseHolderBlock.STATE);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);

        setting = new FuseSettingBehaviour(this);
        setting.setValueWithoutCallback(10);
        behaviours.add(setting);
    }

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(2);
        fuseWire = builder.connectSwitch(resistance(), builder.terminalNode(0), builder.terminalNode(1), state == FuseState.CLOSED);
    }

    @Environment(EnvType.CLIENT)
    public void playEffect() {
        if(level == null)
            return;
        var pos = Vec3.atCenterOf(this.worldPosition);
        var facing = getBlockState().getValue(FuseHolderBlock.FACING);
        SparkParticleData.explodeParticles(level, (float) pos.x, (float) pos.y, (float) pos.z, facing.getOpposite(), 5);
        ModdedSoundEvents.FUSE_POPS.playAt(level, pos, 1.0f, 1.0f, false);
    }

    @Override
    public void electricalTick() {
        if(fuseWire.getState()) {
            if(fuseWire.isConverged() && Math.abs(fuseWire.current()) > setting.getValue()) {
                setState(FuseState.BLOWN);
            }
        }
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        var prevState = state;
        state = FuseState.values()[Mth.clamp(tag.getIntOr("State", 0), 0, FuseState.values().length - 1)];
        if(clientPacket && state == FuseState.BLOWN && prevState == FuseState.CLOSED)
            playEffect();
        fuseWire.setState(state == FuseState.CLOSED);
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("State", state.ordinal());
    }

    @Override
    public void writeSafe(ValueOutput tag) {
        super.writeSafe(tag);
        tag.putInt("State", state.ordinal());
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state) {
        if(this.state == FuseState.CLOSED)
            return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, ModdedItems.IRON_WIRE.asStack());
        return ItemRequirement.NONE;
    }

    public boolean resetFuse() {
        if(state == FuseState.OPEN || state == FuseState.BLOWN) {
            if(!level.isClientSide()) {
                setState(FuseState.CLOSED);
                ModdedSoundEvents.FUSE_INSTALL.playOnServer(level, worldPosition);
            }
            return true;
        }
        return false;
    }

    public boolean removeBlown() {
        if(state == FuseState.BLOWN) {
            setState(FuseState.OPEN);
            return true;
        }
        return false;
    }

    public void setState(FuseState state) {
        if(this.state != state) {
            this.state = state;
            if(level != null) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(FuseHolderBlock.STATE, state));
                if(!level.isClientSide())
                    notifyUpdate();
            }
            fuseWire.setState(state == FuseState.CLOSED);
        }
    }


}
