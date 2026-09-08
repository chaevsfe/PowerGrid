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

import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.builders.AbstractBuilder;
import org.patryk3211.powergrid.registrate.entry.RegistryEntry;
import org.patryk3211.powergrid.registrate.fn.NonNullFunction;
import org.patryk3211.powergrid.registrate.fn.NonNullUnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

public class ComponentBuilder<T extends Component, P> extends AbstractBuilder<T, P, ComponentBuilder<T, P>> {
    private final NonNullFunction<ComponentFootprint, T> factory;
    private ComponentFootprint footprint;

    public ComponentBuilder(Registrate owner, P parent, String name, NonNullFunction<ComponentFootprint, T> factory) {
        super(owner, name, parent);
        this.factory = factory;
    }

    public ComponentBuilder<T, P> footprint(ComponentFootprint footprint) {
        this.footprint = footprint;
        return this;
    }

    public ComponentBuilder<T, P> footprint(int width, int height, NonNullUnaryOperator<ComponentFootprint.Builder> transform) {
        var sharedKeyBase = "component." + getOwner().getModid();
        var keyBase = sharedKeyBase + "." + getName();
        this.footprint = transform.apply(new ComponentFootprint.Builder(width, height, keyBase, sharedKeyBase)).build();
        return this;
    }

    /**
     * The component to item mapping is data driven and lives in
     * data/powergrid/powergrid/component_items; this call records nothing. A component
     * without a matching file is reported by {@link ComponentRegistry#validateItemEntries}.
     */
    public ComponentBuilder<T, P> item(ItemLike item) {
        return this;
    }

    /**
     * @see #item(ItemLike)
     */
    public ComponentBuilder<T, P> item(ItemLike item, TagKey<Item> tag) {
        return this;
    }

    public RegistryEntry<Component, T> register() {
        if(footprint == null)
            throw new IllegalStateException("Cannot create entry without a set footprint");
        T value = factory.apply(footprint);
        Registry.register(ComponentRegistry.REGISTRY, getOwner().id(getName()), value);
        runRegisterCallbacks(value);
        queueAfterRegisterCallbacks(value);
        return new RegistryEntry<>(getOwner().id(getName()), value);
    }
}
