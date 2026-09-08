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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleRenderType;
import org.patryk3211.powergrid.electricity.electromagnet.MagnetizationParticle;
import org.patryk3211.powergrid.electricity.electromagnet.MagnetizationParticleData;
import org.patryk3211.powergrid.electricity.particles.SparkParticle;
import org.patryk3211.powergrid.electricity.particles.SparkParticleData;
import org.patryk3211.powergrid.electricity.particles.ZapParticle;
import org.patryk3211.powergrid.electricity.particles.ZapParticleGroup;

@Environment(EnvType.CLIENT)
public class ModdedParticleProviders {
    public static void register() {
        var registry = ParticleProviderRegistry.getInstance();
        registry.register(ModdedParticles.MAGNETIZATION,
                (ParticleProviderRegistry.PendingParticleProvider<MagnetizationParticleData>) MagnetizationParticle.Factory::new);
        registry.register(ModdedParticles.CUBE_SPARK,
                (ParticleProviderRegistry.PendingParticleProvider<SparkParticleData>) SparkParticle.Factory::new);
        registry.register(ModdedParticles.ZAP, new ZapParticle.Factory());

        ParticleGroupRegistry.register(ZapParticleGroup.ZAP, ZapParticleGroup::new);
        ParticleGroupRegistry.registerOrdering(ParticleRenderType.SINGLE_QUADS, ZapParticleGroup.ZAP);
    }
}
