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
package org.patryk3211.powergrid.circuits.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.utility.sound.ContinuousSound;

import java.util.Map;
import java.util.WeakHashMap;

import static org.patryk3211.powergrid.circuits.components.NeonBulbComponent.LIT;

@Environment(EnvType.CLIENT)
public class ThyratronSounds {
    private static final float HUM_VOLUME = 0.1f;
    private static final int HUM_DELAY_TICKS = 5;
    private static final int HUM_FADE_TICKS = 5;

    private static final Map<PlacedComponent, State> STATES = new WeakHashMap<>();

    public static void tick(@NotNull PlacedComponent placed, boolean lit) {
        var state = STATES.computeIfAbsent(placed, $ -> new State());
        var sounds = Minecraft.getInstance().getSoundManager();
        if (lit)
            state.humTicks = Math.min(state.humTicks + 1, HUM_DELAY_TICKS);
        else
            state.humTicks = 0;
        boolean hum = state.humTicks >= HUM_DELAY_TICKS;
        if (hum) {
            if (state.buzz == null || state.buzz.isStopped()) {
                var pos = net.minecraft.world.phys.Vec3.atCenterOf(placed.getPos());
                state.buzz = new ContinuousSound(
                        ModdedSoundEvents.THYRATRON_HUM.getMainEvent(), SoundSource.BLOCKS,
                        pos.x, pos.y, pos.z, HUM_VOLUME, 1.0f, HUM_FADE_TICKS,
                        () -> !placed.destroyed && placed.get(LIT) && state.humTicks >= HUM_DELAY_TICKS);
                sounds.play(state.buzz);
            } else {
                state.buzz.keepAlive();
            }
        } else if (state.buzz != null && state.buzz.isStopped()) {
            state.buzz = null;
        }
    }

    private static class State {
        int humTicks;
        ContinuousSound buzz;
    }
}
