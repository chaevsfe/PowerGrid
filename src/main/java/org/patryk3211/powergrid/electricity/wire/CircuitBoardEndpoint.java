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
package org.patryk3211.powergrid.electricity.wire;

import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.sim.node.IElectricNode;

import java.util.Objects;

public class CircuitBoardEndpoint implements IWireEndpoint {
    private BlockPos pos;
    private int x;
    private int y;

    public CircuitBoardEndpoint() {
        this(null, 0, 0);
    }

    public CircuitBoardEndpoint(BlockPos pos, int x, int y) {
        this.pos = pos;
        this.x = x;
        this.y = y;
    }

    @Override
    public WireEndpointType type() {
        return WireEndpointType.CIRCUIT_BOARD;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean isComplete() {
        return pos != null;
    }

    @Override
    public void read(CompoundTag nbt) {
        pos = nbt.read("Pos", BlockPos.CODEC).orElse(null);
        x = nbt.getByteOr("X", (byte) 0);
        y = nbt.getByteOr("Y", (byte) 0);
    }

    @Override
    public void write(CompoundTag nbt) {
        nbt.putIntArray("Pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.putByte("X", (byte) x);
        nbt.putByte("Y", (byte) y);
    }

    @Override
    public @NotNull Vec3 getExactPosition(Level world) {
        var pos = new Vec3((x + 0.5f) / 16f, 2 / 16f, (y + 0.5f) / 16f);
        var state = world.getBlockState(this.pos);
        pos = VecHelper.rotateCentered(pos, CircuitBoardBlock.getAngleX(state), Direction.Axis.X);
        pos = VecHelper.rotateCentered(pos, CircuitBoardBlock.getAngleY(state), Direction.Axis.Y);
        return pos.add(this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public boolean isValid(Level world) {
        var opt = world.getBlockEntity(pos, ModdedBlockEntities.CIRCUIT_BOARD.get());
        return opt.map(be -> be.getSchematic().front().hasTrace(x, y))
                .orElse(false);
    }

    public IElectricNode getGenericNode(Level world) {
        var opt = world.getBlockEntity(pos, ModdedBlockEntities.CIRCUIT_BOARD.get());
        return opt.map(be -> {
            var baked = be.getBaked();
            if(baked == null)
                return null;
            var bundle = be.getSchematic().flood(CircuitSchematic.Layer.FRONT, x, y, new CircuitSchematic.VisitMap());
            if(bundle.isEmpty())
                return null;
            var node = bundle.iterator().next();
            return baked.getNode(node);
        }).orElse(null);
    }

    public ElectricBehaviour getElectricBehaviour(Level world) {
        if(pos == null)
            return null;
        if(!world.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())))
            return null;
        return world.getBlockEntity(pos, ModdedBlockEntities.CIRCUIT_BOARD.get())
                .map(ElectricBlockEntity::getElectricBehaviour)
                .orElse(null);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof CircuitBoardEndpoint other) {
            return pos.equals(other.pos) && x == other.x && y == other.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, x, y);
    }
}
