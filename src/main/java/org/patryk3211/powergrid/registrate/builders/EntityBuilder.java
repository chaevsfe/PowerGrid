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
package org.patryk3211.powergrid.registrate.builders;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.entry.EntityEntry;
import org.patryk3211.powergrid.registrate.fn.NonNullConsumer;

import java.util.ArrayList;
import java.util.List;

public class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<T>, P, EntityBuilder<T, P>> {
    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private final List<NonNullConsumer<EntityType.Builder<T>>> propertyOperators = new ArrayList<>();

    public EntityBuilder(Registrate owner, P parent, String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        super(owner, name, parent);
        this.factory = factory;
        this.category = category;
    }

    public EntityBuilder<T, P> properties(NonNullConsumer<EntityType.Builder<T>> operator) {
        propertyOperators.add(operator);
        return this;
    }

    public EntityEntry<T> register() {
        Identifier id = getId();
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, category);
        for (NonNullConsumer<EntityType.Builder<T>> operator : propertyOperators)
            operator.accept(builder);
        EntityType<T> type = builder.build(key);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
        EntityEntry<T> entry = new EntityEntry<>(id, type);
        getOwner().track(Registries.ENTITY_TYPE, entry);
        runRegisterCallbacks(type);
        queueAfterRegisterCallbacks(type);
        return entry;
    }
}
