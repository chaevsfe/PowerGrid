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
package org.patryk3211.powergrid.electricity.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class SparkParticle extends SingleQuadParticle {
    protected SparkParticle(SparkParticleData data, ClientLevel world, double x, double y, double z, double vX, double vY, double vZ, SpriteSet sprites) {
        super(world, x, y, z, sprites.first());
        setSpriteFromAge(sprites);
        xd = vX;
        yd = vY;
        zd = vZ;

        var r = world.getRandom();
        var color = r.nextFloat() * 0.3f + 0.3f;
        bCol = color;
        rCol = 1;
        gCol = 1;

        gravity = data.getGravity() ? 3.0f : 0;
        friction = 0.97f;
        lifetime = data.getLife() < 0 ? r.nextInt(20) + 40 : data.getLife();
        quadSize = r.nextFloat() * 0.1f + 0.1f;
        hasPhysics = data.getCollision();
    }

    @Override
    protected int getLightCoords(float tint) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SparkParticleData> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SparkParticleData data, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new SparkParticle(data, world, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
