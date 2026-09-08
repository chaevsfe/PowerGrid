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

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.base.SurfaceElectricBlock;
import org.patryk3211.powergrid.electricity.electricswitch.HvBreakerBlock;
import org.patryk3211.powergrid.electricity.fuse.FuseHolderBlock;
import org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchBlock;
import org.patryk3211.powergrid.kinetics.motor.ConstantSpeedMotorBlock;
import org.patryk3211.powergrid.kinetics.plotter.PlotterBlock;
import org.patryk3211.powergrid.kinetics.rheostat.RheostatBlock;

@Environment(EnvType.CLIENT)
public class ValueBoxTransforms {
    public static class CarbonPile extends CenteredSideValueBoxTransform {
        public CarbonPile() {
            super((state, dir) -> dir == Direction.UP);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 12.5f);
        }
    }

    public static class CreativeSource extends CenteredSideValueBoxTransform {
        public CreativeSource() {
            super((state, dir) -> dir.getAxis() != Direction.Axis.Y);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 14.5f);
        }
    }

    public static class HvBreaker extends CenteredSideValueBoxTransform {
        public HvBreaker() {
            super((state, dir) -> dir == state.getValue(HvBreakerBlock.HORIZONTAL_FACING));
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 7.0f, 15.5f);
        }
    }

    public static class FuseHolder extends CenteredSideValueBoxTransform {
        public FuseHolder() {
            super((state, dir) -> dir.getOpposite() == state.getValue(FuseHolderBlock.FACING));
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 5.0f);
        }
    }

    public static class Gauge extends CenteredSideValueBoxTransform {
        public Gauge() {
            super((state, dir) -> dir == Direction.UP);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 13.5f);
        }
    }

    public static class Resistor extends CenteredSideValueBoxTransform {
        public Resistor() {
            super((state, dir) -> dir == state.getValue(SurfaceElectricBlock.FACING).getOpposite());
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 7.6f);
        }
    }

    public static class SparkGap extends CenteredSideValueBoxTransform {
        public SparkGap() {
            super((state, dir) -> dir == Direction.UP);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 2.5f);
        }
    }

    public static class GeneratorClutch extends CenteredSideValueBoxTransform {
        public GeneratorClutch() {
            super((state, dir) -> state.getValue(GeneratorClutchBlock.FACING).getAxis() != dir.getAxis());
        }
    }

    public static class ConstantSpeedMotor extends CenteredSideValueBoxTransform {
        public ConstantSpeedMotor() {
            super((state, dir) -> {
                var facing = state.getValue(ConstantSpeedMotorBlock.FACING);
                if(facing.getAxis() == Direction.Axis.Y)
                    return dir.getAxis() == Direction.Axis.Z;
                return dir == Direction.UP;
            });
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 8.0f, 12.5f);
        }
    }

    public static class Plotter extends CenteredSideValueBoxTransform {
        public Plotter() {
            super((state, face) -> face.getAxis() == state.getValue(PlotterBlock.HORIZONTAL_FACING).getAxis());
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 7, 15.5);
        }
    }

    public static class Rheostat extends CenteredSideValueBoxTransform {
        public Rheostat() {
            super((state, dir) -> dir != state.getValue(RheostatBlock.HORIZONTAL_FACING) && dir.getAxis() != Direction.Axis.Y);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0f, 6.0f, 13.5f);
        }
    }
}
