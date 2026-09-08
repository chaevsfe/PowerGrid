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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.data.DataGenContext;
import org.patryk3211.powergrid.registrate.data.RegistrateBlockstateProvider;
import org.patryk3211.powergrid.registrate.entry.BlockEntry;
import org.patryk3211.powergrid.registrate.fn.NonNullBiConsumer;
import org.patryk3211.powergrid.registrate.fn.NonNullBiFunction;
import org.patryk3211.powergrid.registrate.fn.NonNullFunction;
import org.patryk3211.powergrid.registrate.fn.NonNullSupplier;
import org.patryk3211.powergrid.registrate.fn.NonNullUnaryOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockBuilder<T extends Block, P> extends AbstractBuilder<T, P, BlockBuilder<T, P>> {
    private final NonNullFunction<BlockBehaviour.Properties, T> factory;
    private final List<NonNullUnaryOperator<BlockBehaviour.Properties>> propertyOperators = new ArrayList<>();
    private final List<TagKey<Block>> tags = new ArrayList<>();
    private NonNullSupplier<? extends Block> initialProperties;
    private ItemBuilder<?, BlockBuilder<T, P>> itemBuilder;
    private T registered;

    public BlockBuilder(Registrate owner, P parent, String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        super(owner, name, parent);
        this.factory = factory;
    }

    public BlockBuilder<T, P> initialProperties(NonNullSupplier<? extends Block> block) {
        this.initialProperties = block;
        return this;
    }

    public BlockBuilder<T, P> properties(NonNullUnaryOperator<BlockBehaviour.Properties> operator) {
        propertyOperators.add(operator);
        return this;
    }

    public BlockBuilder<T, P> blockstate(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> provider) {
        return this;
    }

    public BlockBuilder<T, P> defaultLoot() {
        return this;
    }

    @SafeVarargs
    public final BlockBuilder<T, P> tag(TagKey<Block>... values) {
        Collections.addAll(tags, values);
        return this;
    }

    public List<TagKey<Block>> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
        return item(BlockItem::new);
    }

    public <I extends Item> ItemBuilder<I, BlockBuilder<T, P>> item(NonNullBiFunction<Block, Item.Properties, I> itemFactory) {
        ItemBuilder<I, BlockBuilder<T, P>> builder = new ItemBuilder<>(getOwner(), this, getName(),
                properties -> itemFactory.apply(requireRegistered(), properties), this::requireRegistered);
        this.itemBuilder = builder;
        return builder;
    }

    public BlockBuilder<T, P> simpleItem() {
        return item().build();
    }

    private T requireRegistered() {
        if (registered == null)
            throw new IllegalStateException("Block " + getId() + " is not registered yet");
        return registered;
    }

    public BlockEntry<T> register() {
        Identifier id = getId();
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        BlockBehaviour.Properties properties = initialProperties == null
                ? BlockBehaviour.Properties.of()
                : BlockBehaviour.Properties.ofFullCopy(initialProperties.get());
        for (NonNullUnaryOperator<BlockBehaviour.Properties> operator : propertyOperators)
            properties = operator.apply(properties);
        T block = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        registered = block;
        BlockEntry<T> entry = new BlockEntry<>(id, block);
        getOwner().track(Registries.BLOCK, entry);
        if (itemBuilder != null)
            itemBuilder.register();
        runRegisterCallbacks(block);
        queueAfterRegisterCallbacks(block);
        return entry;
    }
}
