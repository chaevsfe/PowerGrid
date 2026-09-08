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

import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class TunedBlockEntity extends ElectricKineticBlockEntity {
    public LerpedFloat arm;

    public TunedBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        arm = LerpedFloat.linear().chase(0, 0, LerpedFloat.Chaser.LINEAR);
        arm.setValue(1);
    }

    @Override
    public void initialize() {
        super.initialize();
        arm.forceNextSync();
        sendData();
    }

    private float getChaseSpeed() {
        return Mth.clamp(Math.abs(getSpeed()) / 60.0f * 0.05f, 0, 1);
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        float speed = getSpeed();
        if(speed == 0) {
            arm.chase(arm.getValue(), 0, LerpedFloat.Chaser.LINEAR);
            arm.forceNextSync();
            return;
        }
        if(sequenceContext != null && sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE) {
            var angle = sequenceContext.getEffectiveValue(getTheoreticalSpeed());
            var target = Mth.clamp((arm.getValue() + angle / 315f * Math.signum(speed)), 0, 1);
            arm.chase(target, getChaseSpeed(), LerpedFloat.Chaser.LINEAR);
        } else {
            arm.chase(speed > 0 ? 1 : 0, getChaseSpeed(), LerpedFloat.Chaser.LINEAR);
        }
        sendData();
    }

    public abstract void refreshParameters();

    @Override
    public void tick() {
        super.tick();
        arm.tickChaser();

        if(!arm.settled()) {
            if(getSpeed() == 0) {
                arm.updateChaseTarget(arm.getValue());
            }
            refreshParameters();
            setChanged();
        }
    }

    @Override
    protected void write(ValueOutput compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if(clientPacket)
            arm.forceNextSync();
        arm.write(compound.child("Arm"));
    }

    @Override
    public void writeSafe(ValueOutput tag) {
        super.writeSafe(tag);
        arm.write(tag.child("Arm"));
    }

    @Override
    protected void read(ValueInput compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        arm.read(compound.childOrEmpty("Arm"), clientPacket);
        refreshParameters();
    }
}
