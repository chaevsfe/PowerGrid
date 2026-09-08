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
package org.patryk3211.powergrid;

import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.builders.BlockEntityBuilder;
import org.patryk3211.powergrid.registrate.builders.SimpleBuilder;
import org.patryk3211.powergrid.registrate.entry.BlockEntityEntry;
import org.patryk3211.powergrid.registrate.entry.RegistryEntry;
import org.patryk3211.powergrid.registrate.fn.NonNullFunction;
import org.patryk3211.powergrid.registrate.fn.NonNullSupplier;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.components.Component;
import org.patryk3211.powergrid.circuits.components.ComponentBuilder;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.utility.SimpleBlockEntityVisualFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AbstractPowerGridRegistrate extends Registrate {
    private Function<Item, TooltipModifier> tooltipModifierFactory;
    private final Set<Item> tooltipItems = Collections.newSetFromMap(new IdentityHashMap<>());

    protected AbstractPowerGridRegistrate(String modid) {
        super(modid);
    }

    public static AbstractPowerGridRegistrate create(String modid) {
        return new AbstractPowerGridRegistrate(modid);
    }

    public AbstractPowerGridRegistrate setTooltipModifierFactory(Function<Item, TooltipModifier> factory) {
        this.tooltipModifierFactory = factory;
        getAll(Registries.ITEM).forEach(entry -> registerTooltipModifier(entry.get()));
        return this;
    }

    @Override
    public void registerTooltipModifier(Item item) {
        if (tooltipModifierFactory == null || !tooltipItems.add(item))
            return;
        TooltipModifier.REGISTRY.register(item, tooltipModifierFactory.apply(item));
    }

    @NotNull
    @Override
    public <T extends BlockEntity> PowerGridBlockEntityBuilder<T, Registrate> blockEntity(String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return new PowerGridBlockEntityBuilder<>(this, this, name, factory);
    }

    public <T extends Component> ComponentBuilder<T, AbstractPowerGridRegistrate> component(String name, NonNullFunction<ComponentFootprint, T> factory) {
        return new ComponentBuilder<>(this, this, name, factory);
    }

    public <T extends DisplaySource> SimpleBuilder<DisplaySource, T, AbstractPowerGridRegistrate> displaySource(String name, Supplier<T> supplier) {
        return new SimpleBuilder<>(this, this, name, CreateRegistries.DISPLAY_SOURCE, supplier);
    }

    public static class PowerGridBlockEntityBuilder<T extends BlockEntity, P> extends BlockEntityBuilder<T, P> {
        private Supplier<?> visualFactory;
        private Predicate<T> renderNormally;
        private final List<NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>>> deferredValidBlocks = new ArrayList<>();

        protected PowerGridBlockEntityBuilder(Registrate owner, P parent, String name, BlockEntityFactory<T> factory) {
            super(owner, parent, name, factory);
        }

        public PowerGridBlockEntityBuilder<T, P> validBlocksDeferred(NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>> blocks) {
            deferredValidBlocks.add(blocks);
            return this;
        }

        @Override
        protected BlockEntityType<T> createEntry() {
            deferredValidBlocks.stream()
                    .map(Supplier::get)
                    .flatMap(Collection::stream)
                    .forEach(this::validBlock);
            return super.createEntry();
        }

        public PowerGridBlockEntityBuilder<T, P> displaySource(RegistryEntry<DisplaySource, ? extends DisplaySource> source) {
            this.onRegisterAfter(CreateRegistryKeys.DISPLAY_SOURCE, type -> DisplaySource.BY_BLOCK_ENTITY.add(type, source.get()));
            return this;
        }

        public PowerGridBlockEntityBuilder<T, P> displayTarget(RegistryEntry<DisplayTarget, ? extends DisplayTarget> target) {
            this.onRegisterAfter(CreateRegistryKeys.DISPLAY_TARGET, type -> DisplayTarget.BY_BLOCK_ENTITY.register(type, target.get()));
            return this;
        }

        public PowerGridBlockEntityBuilder<T, P> visual(NonNullSupplier<SimpleBlockEntityVisualFactory<T>> visualFactory) {
            return visual(visualFactory, true);
        }

        public PowerGridBlockEntityBuilder<T, P> visual(NonNullSupplier<SimpleBlockEntityVisualFactory<T>> visualFactory, boolean renderNormally) {
            return visual(visualFactory, be -> renderNormally);
        }

        public PowerGridBlockEntityBuilder<T, P> visual(NonNullSupplier<SimpleBlockEntityVisualFactory<T>> visualFactory, Predicate<T> renderNormally) {
            this.visualFactory = visualFactory;
            this.renderNormally = renderNormally;
            return this;
        }

        @Override
        protected void registerClientHooks(BlockEntityType<T> type) {
            if (visualFactory == null) {
                super.registerClientHooks(type);
                return;
            }
            Supplier<?> renderer = rendererSupplier();
            Supplier<?> visual = visualFactory;
            Predicate<T> normally = renderNormally;
            Registrate.addClientHook(sink -> sink.visual(type, renderer, visual, normally));
        }
    }
}
