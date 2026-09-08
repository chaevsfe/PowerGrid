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
package org.patryk3211.powergrid.electricity.sparkgap;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.catnip.math.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.particles.HvSparkSoundInstance;
import org.patryk3211.powergrid.electricity.particles.SparkSoundOwner;
import org.patryk3211.powergrid.electricity.particles.ZapParticleData;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SparkGapBlockEntity extends ElectricBlockEntity implements SparkSoundOwner {
    private SwitchedWire plasmaChannel;

    protected SparkGapValueBehaviour setting;
    private boolean wasSparking;

    public SparkGapBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);

        setting = new SparkGapValueBehaviour(this);
        behaviours.add(setting);
    }

    @Override
    public void electricalTick() {
        if(!plasmaChannel.getState() && Math.abs(plasmaChannel.potentialDifference()) > setting.getVoltage()) {
            plasmaChannel.setState(true);
            notifyUpdate();
        } else if(plasmaChannel.getState() && Math.abs(plasmaChannel.current()) < setting.getCurrent()) {
            plasmaChannel.setState(false);
            notifyUpdate();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide() && plasmaChannel.getState()) {
            var center = Vec3.atCenterOf(worldPosition).subtract(0, 0.125f, 0);
            float offset = (1 + setting.getValue() * 2.8f / 18f) / 33f;

            var axis = getBlockState().getValue(SparkGapBlock.HORIZONTAL_AXIS);
            var end = center.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE), offset);
            var start = center.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE), offset);
            level.addParticle(new ZapParticleData(end, true)
                            .withLife(1)
                            .withSegments(5),
                    start.x, start.y, start.z, 0, 0, 0);
            double dist = start.distanceTo(end);
            int sparks = (int) (dist / 0.05f);
            var random = level.getRandom();
            for(int i = 0; i < sparks + 1; ++i) {
                double x = Mth.lerp((float) i / sparks, start.x, end.x) + random.nextFloat() * 0.05f - 0.025f;
                double y = Mth.lerp((float) i / sparks, start.y, end.y) + random.nextFloat() * 0.05f - 0.025f;
                double z = Mth.lerp((float) i / sparks, start.z, end.z) + random.nextFloat() * 0.05f - 0.025f;
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
            }
        }
        if(level.isClientSide()) {
            if (!wasSparking && plasmaChannel.getState()) {
                makeSparkSound();
            }
            wasSparking = plasmaChannel.getState();
        }
    }

    @Override
    public boolean isSparking() {
        return plasmaChannel.getState();
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        plasmaChannel.setState(tag.getBooleanOr("State", false));
    }

    @Environment(EnvType.CLIENT)
    public void makeSparkSound() {
        Minecraft.getInstance().getSoundManager().play(new HvSparkSoundInstance(this));
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putBoolean("State", plasmaChannel.getState());
    }

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(2);
        plasmaChannel = builder.connectSwitch(1, builder.terminalNode(0), builder.terminalNode(1), false);
    }


}
