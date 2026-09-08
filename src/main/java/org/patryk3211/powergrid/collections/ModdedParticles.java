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
package org.patryk3211.powergrid.collections;

import org.patryk3211.powergrid.foundation.particle.ICustomParticleData;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.electromagnet.MagnetizationParticleData;
import org.patryk3211.powergrid.electricity.particles.SparkParticleData;
import org.patryk3211.powergrid.electricity.particles.ZapParticleData;

import java.util.function.Supplier;

public class ModdedParticles {
    public static final ParticleType<MagnetizationParticleData> MAGNETIZATION = register("magnetization", MagnetizationParticleData::new);

    public static final ParticleType<SparkParticleData> CUBE_SPARK = register("spark", SparkParticleData::new);
    public static final ParticleType<ZapParticleData> ZAP = register("zap", ZapParticleData::new);

    private static <T extends ParticleOptions> ParticleType<T> register(String name, Supplier<? extends ICustomParticleData<T>> typeFactory) {
        var type = typeFactory.get().createType();
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, PowerGrid.asResource(name), type);
        return type;
    }

    public static void register() {
    }
}
