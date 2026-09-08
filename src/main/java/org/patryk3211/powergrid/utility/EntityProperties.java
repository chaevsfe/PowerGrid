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
package org.patryk3211.powergrid.utility;

import org.patryk3211.powergrid.registrate.builders.EntityBuilder;
import org.patryk3211.powergrid.registrate.fn.NonNullConsumer;
import org.patryk3211.powergrid.registrate.fn.NonNullUnaryOperator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityProperties {
    private final EntityType.Builder<?> builder;

    private EntityProperties(EntityType.Builder<?> builder) {
        this.builder = builder;
    }

    public static <T extends Entity, P> NonNullUnaryOperator<EntityBuilder<T, P>> apply(NonNullConsumer<EntityProperties> consumer) {
        return b -> b.properties(builder -> consumer.accept(new EntityProperties(builder)));
    }

    public EntityProperties dimensions(float width, float height) {
        builder.sized(width, height);
        return this;
    }

    public EntityProperties trackRangeChunks(int range) {
        builder.clientTrackingRange(range);
        return this;
    }

    public EntityProperties trackedUpdateRate(int rate) {
        builder.updateInterval(rate);
        return this;
    }

    public EntityProperties forceTrackedVelocityUpdates(boolean force) {
        return this;
    }
}
