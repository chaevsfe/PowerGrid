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
import net.minecraft.resources.Identifier;
import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.entry.RegistryEntry;

import java.util.function.Supplier;

public class SimpleBuilder<R, T extends R, P> extends AbstractBuilder<T, P, SimpleBuilder<R, T, P>> {
    private final Registry<R> registry;
    private final Supplier<T> supplier;

    public SimpleBuilder(Registrate owner, P parent, String name, Registry<R> registry, Supplier<T> supplier) {
        super(owner, name, parent);
        this.registry = registry;
        this.supplier = supplier;
    }

    public RegistryEntry<R, T> register() {
        Identifier id = getId();
        T value = Registry.register(registry, id, supplier.get());
        RegistryEntry<R, T> entry = new RegistryEntry<>(id, value);
        runRegisterCallbacks(value);
        queueAfterRegisterCallbacks(value);
        return entry;
    }
}
