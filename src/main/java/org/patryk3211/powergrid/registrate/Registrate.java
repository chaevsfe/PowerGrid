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
package org.patryk3211.powergrid.registrate;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.patryk3211.powergrid.registrate.builders.BlockBuilder;
import org.patryk3211.powergrid.registrate.builders.BlockEntityBuilder;
import org.patryk3211.powergrid.registrate.builders.EntityBuilder;
import org.patryk3211.powergrid.registrate.builders.ItemBuilder;
import org.patryk3211.powergrid.registrate.entry.RegistryEntry;
import org.patryk3211.powergrid.registrate.fn.NonNullFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Registrate {
    private static final List<Consumer<ClientHookSink>> CLIENT_HOOKS = new ArrayList<>();

    private final String modid;
    private final Map<ResourceKey<? extends Registry<?>>, List<RegistryEntry<?, ?>>> tracked = new LinkedHashMap<>();
    private final List<Runnable> deferred = new ArrayList<>();
    private boolean flushed;

    protected Registrate(String modid) {
        this.modid = modid;
    }

    public String getModid() {
        return modid;
    }

    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    public <T extends Block> BlockBuilder<T, Registrate> block(String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(this, this, name, factory);
    }

    public <T extends Item> ItemBuilder<T, Registrate> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return new ItemBuilder<>(this, this, name, factory);
    }

    public <T extends BlockEntity> BlockEntityBuilder<T, Registrate> blockEntity(String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return new BlockEntityBuilder<>(this, this, name, factory);
    }

    public <T extends Entity> EntityBuilder<T, Registrate> entity(String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityBuilder<>(this, this, name, factory, category);
    }

    public void track(ResourceKey<? extends Registry<?>> registry, RegistryEntry<?, ?> entry) {
        tracked.computeIfAbsent(registry, key -> new ArrayList<>()).add(entry);
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<RegistryEntry<T, ? extends T>> getAll(ResourceKey<? extends Registry<T>> registry) {
        List<RegistryEntry<?, ?>> entries = tracked.get(registry);
        if (entries == null)
            return List.of();
        return (Collection<RegistryEntry<T, ? extends T>>) (Collection<?>) Collections.unmodifiableList(entries);
    }

    public void queueAfterRegister(Runnable callback) {
        if (flushed) {
            callback.run();
            return;
        }
        deferred.add(callback);
    }

    public void registerTooltipModifier(Item item) {
    }

    public void register() {
        flushed = true;
        List<Runnable> pending = new ArrayList<>(deferred);
        deferred.clear();
        for (Runnable callback : pending)
            callback.run();
    }

    public static void addClientHook(Consumer<ClientHookSink> hook) {
        CLIENT_HOOKS.add(hook);
    }

    public static List<Consumer<ClientHookSink>> drainClientHooks() {
        List<Consumer<ClientHookSink>> hooks = new ArrayList<>(CLIENT_HOOKS);
        CLIENT_HOOKS.clear();
        return hooks;
    }
}
