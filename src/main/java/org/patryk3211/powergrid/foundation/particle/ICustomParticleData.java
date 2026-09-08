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
package org.patryk3211.powergrid.foundation.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ICustomParticleData<T extends ParticleOptions> {
    MapCodec<T> getCodec(ParticleType<T> type);

    StreamCodec<? super RegistryFriendlyByteBuf, T> getStreamCodec();

    default boolean getOverrideLimiter() {
        return false;
    }

    default ParticleType<T> createType() {
        return new ParticleType<T>(getOverrideLimiter()) {
            @Override
            public MapCodec<T> codec() {
                return ICustomParticleData.this.getCodec(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return ICustomParticleData.this.getStreamCodec();
            }
        };
    }
}
