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

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public abstract class ItemProviderEntry<R, T extends R> extends RegistryEntry<R, T> implements ItemLike {
    protected ItemProviderEntry(Identifier id, T value) {
        super(id, value);
    }

    @Override
    public abstract Item asItem();

    public ItemStack asStack() {
        return new ItemStack(asItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(asItem(), count);
    }

    public boolean isIn(ItemStack stack) {
        return stack != null && stack.is(asItem());
    }
}
