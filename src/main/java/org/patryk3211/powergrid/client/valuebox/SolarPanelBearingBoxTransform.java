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
package org.patryk3211.powergrid.client.valuebox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingBlock;

@Environment(EnvType.CLIENT)
public class SolarPanelBearingBoxTransform extends ValueBoxTransform.Sided {
    @Override
    public float getScale() {
        return .4f;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        if (!state.hasProperty(SolarPanelBearingBlock.FACING))
            return false;
        var facing = state.getValue(SolarPanelBearingBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Y) {
            if (getSide().equals(Direction.EAST))
                return true;
            return getSide().equals(Direction.WEST);
        } else {
            if (getSide().equals(facing.getClockWise()))
                return true;
            return getSide().equals(facing.getCounterClockWise());
        }
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction side = getSide();
        Direction facing = state.getValue(SolarPanelBearingBlock.FACING);

        float roll = 0;
        for (Pointing p : Pointing.values())
            if (p.getCombinedDirection(facing) == side)
                roll = p.getXRotation();
        if (facing == Direction.UP)
            roll += 180;

        float horizontalAngle = AngleHelper.horizontalAngle(facing);
        float verticalAngle = AngleHelper.verticalAngle(facing);
        Vec3 local = VecHelper.voxelSpace(8, 14.5, 9);

        local = VecHelper.rotateCentered(local, roll, Direction.Axis.Z);
        local = VecHelper.rotateCentered(local, horizontalAngle, Direction.Axis.Y);
        local = VecHelper.rotateCentered(local, verticalAngle, Direction.Axis.X);

        return local;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Direction facing = state.getValue(SolarPanelBearingBlock.FACING);

        if (facing.getAxis() == Direction.Axis.Y) {
            super.rotate(state, ms);
            return;
        }

        float roll = 0;
        for (Pointing p : Pointing.values())
            if (p.getCombinedDirection(facing) == getSide())
                roll = p.getXRotation();

        float yRot = AngleHelper.horizontalAngle(facing) + (facing == Direction.DOWN ? 180 : 0);
        TransformStack.of(ms)
                .rotateYDegrees(yRot)
                .rotateYDegrees(roll);
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }
}
