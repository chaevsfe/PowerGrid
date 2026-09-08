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
package org.patryk3211.powergrid.circuits.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;

import java.util.Objects;
import java.util.Optional;

public class ComponentRegistry {
    public static final ResourceKey<Registry<Component>> REGISTRY_KEY = ResourceKey.createRegistryKey(PowerGrid.asResource("components"));

    public static final ResourceKey<Registry<ComponentItemEntry>> ITEM_REGISTRY_KEY = ResourceKey.createRegistryKey(PowerGrid.asResource("component_items"));
    public static final Codec<ComponentItemEntry> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ComponentItemEntry::item),
                    Codec.optionalField("tag", TagKey.codec(Registries.ITEM), false).forGetter(ComponentItemEntry::tag)
            ).apply(instance, ComponentItemEntry::new));

    public static Registry<Component> REGISTRY;

    public static Iterable<Component> entries() {
        return REGISTRY;
    }

    @Contract("_, null -> null")
    public static Item getItem(@NotNull Level level, Component component) {
        if(component == null)
            return null;
        return level.registryAccess()
                .lookupOrThrow(ITEM_REGISTRY_KEY)
                .getValue(getId(component)).item();
    }

    @Contract("_, null -> null")
    public static Optional<TagKey<Item>> getItemTag(@NotNull Level level, Component component) {
        if(component == null)
            return null;
        return level.registryAccess()
                .lookupOrThrow(ITEM_REGISTRY_KEY)
                .getValue(getId(component)).tag();
    }

    public static Identifier getComponentId(@NotNull Level level, ItemStack stack) {
        var registry = level.registryAccess()
                .lookupOrThrow(ComponentRegistry.ITEM_REGISTRY_KEY);
        for(var entry : registry.entrySet()) {
            if(stack.is(entry.getValue().item()))
                return entry.getKey().identifier();
            var tag = entry.getValue().tag();
            if(tag.isPresent() && stack.is(tag.get()))
                return entry.getKey().identifier();
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    public static Item getItem(Component component) {
        if(component == null)
            return null;
        return Minecraft.getInstance().getConnection().registryAccess()
                .lookupOrThrow(ITEM_REGISTRY_KEY)
                .getValue(getId(component)).item();
    }

    @Environment(EnvType.CLIENT)
    public static Component getComponent(Item item) {
        var registry = Minecraft.getInstance().getConnection().registryAccess()
                .lookupOrThrow(ComponentRegistry.ITEM_REGISTRY_KEY);
        for(var entry : registry.entrySet()) {
            if(entry.getValue().item() == item)
                return get(entry.getKey().identifier());
            var tag = entry.getValue().tag();
            if(tag.isPresent() && item.builtInRegistryHolder().is(tag.get()))
                return get(entry.getKey().identifier());
        }
        return null;
    }

    @NotNull
    public static Identifier getId(@NotNull Component component) {
        return Objects.requireNonNull(REGISTRY.getKey(component), "This component is not registered");
    }

    public static void validateItemEntries(RegistryAccess registries) {
        var items = registries.lookupOrThrow(ITEM_REGISTRY_KEY);
        for(var component : REGISTRY) {
            var id = getId(component);
            if(items.getValue(id) == null)
                PowerGrid.LOGGER.error("Component {} has no component_items entry, it cannot be assembled into a circuit", id);
        }
    }

    @NotNull
    public static Component get(@NotNull Identifier id) {
        return Objects.requireNonNull(REGISTRY.getValue(id), "This id doesn't exist");
    }
}
