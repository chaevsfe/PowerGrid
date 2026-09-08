/*
 * Copyright 2026 chaevsfe
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
 *
 * Inert API compatible stub; contains no code from Sable Companion
 * (dev.ryanhcode.sable) - see NOTICE.
 */
package org.patryk3211.powergrid.compat.sable;

import org.patryk3211.powergrid.compat.sable.math.BoundingBox3d;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public final class SableCompanion {
    public static final SableCompanion INSTANCE = new SableCompanion();

    private SableCompanion() { }

    public Vec3 projectOutOfSubLevel(Level level, Vec3 pos) {
        return pos;
    }

    public Vector3d projectOutOfSubLevel(Level level, Vector3d pos) {
        return pos;
    }

    @Nullable
    public SubLevelAccess getContaining(Level level, Vec3 pos) {
        return null;
    }

    @Nullable
    public SubLevelAccess getContaining(Level level, BlockPos pos) {
        return null;
    }

    @Nullable
    public SubLevelAccess getContaining(Level level, Vector3d pos) {
        return null;
    }

    @Nullable
    public SubLevelAccess getContaining(Entity entity) {
        return null;
    }

    @Nullable
    public SubLevelAccess getContaining(BlockEntity blockEntity) {
        return null;
    }

    @Nullable
    public ClientSubLevelAccess getContainingClient(Vec3 pos) {
        return null;
    }

    public List<SubLevelAccess> getAllIntersecting(Level level, BoundingBox3d box) {
        return List.of();
    }
}
