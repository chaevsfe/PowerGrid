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
 */
package org.patryk3211.powergrid.registrate.entry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityEntry<T extends BlockEntity> extends RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> {
    public BlockEntityEntry(Identifier id, BlockEntityType<T> value) {
        super(id, value);
    }

    public boolean is(@Nullable BlockEntity blockEntity) {
        return blockEntity != null && blockEntity.getType() == get();
    }

    public boolean isValid(BlockState state) {
        return get().isValid(state);
    }

    @Nullable
    public T at(BlockGetter level, BlockPos pos) {
        return get().getBlockEntity(level, pos);
    }
}
