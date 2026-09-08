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
package org.patryk3211.powergrid.electricity.electromagnet;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MagnetizationParticle extends SingleQuadParticle {
    private final BlockPos controller;
    private boolean triggered;

    protected MagnetizationParticle(MagnetizationParticleData data, ClientLevel world, double x, double y, double z, SpriteSet sprites) {
        super(world, x, y, z, sprites.first());
        this.controller = data.getControllerPos();
        this.hasPhysics = false;

        quadSize = Mth.lerp(world.getRandom().nextFloat(), 0.125f, 0.25f);
        lifetime = 20;
        triggered = false;
        setSprite(sprites.get(world.getRandom()));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        MagnetizingBehaviour behaviour = null;
        var behaviourGeneric = controller == null ? null : BlockEntityBehaviour.get(level, controller, BeltProcessingBehaviour.TYPE);
        if(behaviourGeneric instanceof MagnetizingBehaviour magnetizingBehaviour)
            behaviour = magnetizingBehaviour;

        if(behaviour == null) {
            if(age++ >= lifetime) {
                remove();
                return;
            }
            jiggle();
        } else {
            if(!behaviour.running && !triggered) {
                triggered = true;
                lifetime = 10;
                age = 0;
                return;
            }

            if(triggered && age++ >= lifetime) {
                remove();
                return;
            }

            if(!triggered)
                jiggle();

            if(behaviour.runningTicks >= MagnetizingBehaviour.COLLAPSE_TIME - 5 && !triggered) {
                age = 0;
                lifetime = 5;
                final float SPEED_CONST = 0.3f;
                var dX = behaviour.target.x - x;
                var dY = behaviour.target.y - y;
                var dZ = behaviour.target.z - z;
                xd = dX * SPEED_CONST;
                yd = dY * SPEED_CONST;
                zd = dZ * SPEED_CONST;
                hasPhysics = true;
                triggered = true;
            }
        }

        move(this.xd, this.yd, this.zd);
        quadSize = 0.25f * (1.0f - (float) age / lifetime);
    }

    private void jiggle() {
        xd = (random.nextFloat() - 0.5f) * 0.05f;
        yd = (random.nextFloat() - 0.5f) * 0.05f;
        zd = (random.nextFloat() - 0.5f) * 0.05f;
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<MagnetizationParticleData> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(MagnetizationParticleData data, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new MagnetizationParticle(data, world, x, y, z, this.sprites);
        }
    }
}
