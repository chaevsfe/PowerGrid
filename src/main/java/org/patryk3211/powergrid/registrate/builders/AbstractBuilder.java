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
import net.minecraft.resources.ResourceKey;
import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.fn.NonNullConsumer;
import org.patryk3211.powergrid.registrate.fn.NonNullUnaryOperator;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBuilder<V, P, S extends AbstractBuilder<V, P, S>> {
    private final Registrate owner;
    private final String name;
    private final P parent;
    private final List<NonNullConsumer<? super V>> onRegister = new ArrayList<>();
    private final List<NonNullConsumer<? super V>> onRegisterAfter = new ArrayList<>();

    protected AbstractBuilder(Registrate owner, String name, P parent) {
        this.owner = owner;
        this.name = name;
        this.parent = parent;
    }

    public Registrate getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public P getParent() {
        return parent;
    }

    public Identifier getId() {
        return owner.id(name);
    }

    @SuppressWarnings("unchecked")
    protected final S self() {
        return (S) this;
    }

    public S onRegister(NonNullConsumer<? super V> callback) {
        onRegister.add(callback);
        return self();
    }

    public S onRegisterAfter(ResourceKey<? extends Registry<?>> registry, NonNullConsumer<? super V> callback) {
        onRegisterAfter.add(callback);
        return self();
    }

    public S transform(NonNullUnaryOperator<S> operator) {
        return operator.apply(self());
    }

    public S lang(String translation) {
        return self();
    }

    public P build() {
        return parent;
    }

    protected void runRegisterCallbacks(V value) {
        for (NonNullConsumer<? super V> callback : onRegister)
            callback.accept(value);
        onRegister.clear();
    }

    protected void queueAfterRegisterCallbacks(V value) {
        for (NonNullConsumer<? super V> callback : onRegisterAfter)
            owner.queueAfterRegister(() -> callback.accept(value));
        onRegisterAfter.clear();
    }
}
