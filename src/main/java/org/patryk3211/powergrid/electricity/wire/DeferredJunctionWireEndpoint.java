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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public class DeferredJunctionWireEndpoint implements IWireEndpoint {
    private BlockPos entityPos;
    private UUID entityId;
    private int segmentIndex;
    private int segmentPoint;

    public DeferredJunctionWireEndpoint() {

    }

    public DeferredJunctionWireEndpoint(BlockWireEntity entity, int segmentIndex, int segmentPoint) {
        this.entityPos = entity.blockPosition();
        this.entityId = entity.getUUID();
        this.segmentIndex = segmentIndex;
        this.segmentPoint = segmentPoint;
    }

    @Override
    public WireEndpointType type() {
        return WireEndpointType.DEFERRED_JUNCTION;
    }

    @Override
    public boolean isComplete() {
        return entityPos != null && entityId != null;
    }

    @Override
    public void read(CompoundTag nbt) {
        entityPos = nbt.read("Pos", BlockPos.CODEC).orElse(null);
        entityId = nbt.read("Id", UUIDUtil.CODEC).orElse(null);
        segmentIndex = nbt.getIntOr("Index", 0);
        segmentPoint = nbt.getIntOr("Point", 0);
    }

    @Override
    public void write(CompoundTag nbt) {
        nbt.putIntArray("Pos", new int[] { entityPos.getX(), entityPos.getY(), entityPos.getZ() });
        nbt.store("Id", UUIDUtil.CODEC, entityId);
        nbt.putInt("Index", segmentIndex);
        nbt.putInt("Point", segmentPoint);
    }

    @Nullable
    public BlockWireEntity getEntity(Level world) {
        var entities = world.getEntitiesOfClass(BlockWireEntity.class, new AABB(entityPos), e -> entityId.equals(e.getUUID()));
        if(entities.isEmpty())
            return null;
        return entities.get(0);
    }

    @Override
    @NotNull
    public Vec3 getExactPosition(Level world) {
        var wire = getEntity(world);
        if(wire == null)
            return Vec3.atCenterOf(entityPos);
        if(segmentIndex >= wire.segments.size())
            segmentIndex = wire.segments.size() - 1;
        if(segmentIndex < 0)
            return Vec3.atCenterOf(entityPos);
        var segment = wire.segments.get(segmentIndex);
        return segment.start.relative(segment.direction, segmentPoint / 16f);
    }

    @Nullable
    public JunctionWireEndpoint resolve(Level world) {
        var entity = getEntity(world);
        if(entity == null)
            return null;
        return entity.split(segmentIndex, segmentPoint);
    }

    @Override
    public <T extends BaseWireEntity> boolean canAcceptType(Class<T> clazz) {
        return BlockWireEntity.class.isAssignableFrom(clazz);
    }
}
