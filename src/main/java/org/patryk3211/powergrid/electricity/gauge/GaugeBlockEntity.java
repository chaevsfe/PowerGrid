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
package org.patryk3211.powergrid.electricity.gauge;

import com.zurrtum.create.Create;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.info.customdisplay.CustomDisplayBehaviour;
import org.patryk3211.powergrid.electricity.info.customdisplay.IVarSet;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.Unit;

import java.util.List;

/**
 * Electric gauge block entity base class.
 * PowerGrid's electric equivalent of the kinetic gauge from Create.
 * @see com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity
 */
public abstract class GaugeBlockEntity extends ElectricBlockEntity implements IHaveGoggleInformation, IVarSet {
    protected GaugeValueBehaviour gaugeValue;
    protected CustomDisplayBehaviour display;
    protected float maxValue;

    public float dialTarget;
    public float prevDialState;
    public float dialState;
    public int redstoneOutput;

    public GaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if(!Float.isNaN(dialTarget)) {
            prevDialState = dialState;
            dialState += (dialTarget - dialState) * .125f;
            if (dialState > 1 && level.getRandom().nextFloat() < 1 / 2f)
                dialState -= (dialState - 1) * level.getRandom().nextFloat();
            var newOutput = Mth.floor(Mth.clamp(dialTarget * 15, 0, 15));
            if(newOutput != redstoneOutput) {
                redstoneOutput = newOutput;
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        display = displayBehaviour();
        behaviours.add(display);
    }

    protected CustomDisplayBehaviour displayBehaviour() {
        return new CustomDisplayBehaviour(this, getUnit(), this instanceof CurrentGaugeBlockEntity, this::getMaxValue, this::getColor, "x");
    }

    public float getProgress() {
        return Mth.clamp(dialTarget, 0, 1);
    }

    public MutableComponent getCustomFormatted() {
        return display.format(getValue(), this).component();
    }

    public abstract float getMaxValue();
    public abstract ChatFormatting getColor(float value);
    public abstract float getValue();
    public abstract Unit getUnit();

    @Override
    public float get(String name) {
        float x = getValue();
        float max = getMaxValue();
        if(x > max)
            return max;
        if(x < -max)
            return -max;
        return x;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Use default Create header here.
        Lang.builder(Create.MOD_ID).translate("gui.gauge.info_header").forGoggles(tooltip);
        return true;
    }


}
