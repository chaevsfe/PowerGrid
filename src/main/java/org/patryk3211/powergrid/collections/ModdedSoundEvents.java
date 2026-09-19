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

import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.PowerGrid;

public class ModdedSoundEvents {
    public record SoundEntry(SoundSource category, SoundEvent event, float volume, float pitch) {
        public SoundEvent getMainEvent() {
            return event;
        }

        public void play(Level level, Player player, double x, double y, double z, float volume, float pitch) {
            level.playSound(player, x, y, z, event, category, volume * this.volume, pitch * this.pitch);
        }

        public void playAt(Level level, double x, double y, double z, float volume, float pitch, boolean distanceDelay) {
            level.playLocalSound(x, y, z, event, category, volume * this.volume, pitch * this.pitch, distanceDelay);
        }

        public void playOnServer(Level level, Vec3i pos) {
            playOnServer(level, pos, 1, 1);
        }

        public void playOnServer(Level level, Vec3i pos, float volume, float pitch) {
            play(level, null, pos, volume, pitch);
        }

        public void play(Level level, Player player, Vec3i pos) {
            play(level, player, pos, 1, 1);
        }

        public void play(Level level, Player player, Vec3i pos, float volume, float pitch) {
            play(level, player, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch);
        }

        public void play(Level level, Player player, Vec3 pos, float volume, float pitch) {
            play(level, player, pos.x, pos.y, pos.z, volume, pitch);
        }

        public void playAt(Level level, Vec3i pos, float volume, float pitch, boolean distanceDelay) {
            playAt(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, distanceDelay);
        }

        public void playAt(Level level, Vec3 pos, float volume, float pitch, boolean distanceDelay) {
            playAt(level, pos.x, pos.y, pos.z, volume, pitch, distanceDelay);
        }

        public void playFrom(Entity entity) {
            playFrom(entity, 1, 1);
        }

        public void playFrom(Entity entity, float volume, float pitch) {
            if (!entity.isSilent())
                play(entity.level(), null, entity.blockPosition(), volume, pitch);
        }
    }

    public static final SoundEntry LV_SWITCH_CLICK = register("lv_switch_click", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry MV_SWITCH_CLICK = register("mv_switch_click", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry HV_SWITCH_CONNECT = register("hv_switch_connect", SoundSource.BLOCKS, 0.5f, 0.75f);
    public static final SoundEntry HV_SWITCH_DISCONNECT = register("hv_switch_disconnect", SoundSource.BLOCKS, 0.5f, 0.75f);
    public static final SoundEntry CONTACTOR_ON = register("contactor_on", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry CONTACTOR_OFF = register("contactor_off", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry BREAKER_ON = register("breaker_on", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry BREAKER_OFF = register("breaker_off", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry WIRE_CUT = register("wire_cut", SoundSource.BLOCKS, 0.75f, 1.25f);
    public static final SoundEntry MAGNETIZING = register("magnetizing", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry ELECTROZAPPER_SHOOT = register("electrozapper_shoot", SoundSource.PLAYERS, 1.0f, 1.0f);
    public static final SoundEntry UI_CLICK = register("ui.click", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry UI_COMPONENT_ROTATE = register("ui.component_rotate", SoundSource.BLOCKS, 0.75f, 0.75f);
    public static final SoundEntry UI_PLACE_TRACE = register("ui.place_trace", SoundSource.BLOCKS, 0.75f, 1.75f);
    public static final SoundEntry UI_DELETE_AREA = register("ui.delete_area", SoundSource.BLOCKS, 0.5f, 1.0f);
    public static final SoundEntry UI_PLACE_COMPONENT = register("ui.place_component", SoundSource.BLOCKS, 0.5f, 1.0f);
    public static final SoundEntry UI_FAIL = register("ui.action.fail", SoundSource.BLOCKS, 1.0f, 0.5f);
    public static final SoundEntry UI_SELECT_COMPONENT = register("ui.select_component", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry RELAY_CLICK = register("relay_click", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry COMPONENT_EXPLODE = register("component_explode", SoundSource.BLOCKS, 0.25f, 2.0f);
    public static final SoundEntry FUSE_POPS = register("fuse_pops", SoundSource.BLOCKS, 0.5f, 1.6f);
    public static final SoundEntry THYRATRON_FIRE = register("thyratron_fire", SoundSource.BLOCKS, 0.35f, 1.85f);
    public static final SoundEntry THYRATRON_HUM = register("thyratron_hum", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry MICROSWITCH_ON = register("uswitch_on", SoundSource.BLOCKS, 1.0f, 2.0f);
    public static final SoundEntry MICROSWITCH_OFF = register("uswitch_off", SoundSource.BLOCKS, 1.0f, 2.0f);
    public static final SoundEntry MICROBUTTON_ON = register("ubutton_on", SoundSource.BLOCKS, 0.75f, 2.0f);
    public static final SoundEntry MICROBUTTON_OFF = register("ubutton_off", SoundSource.BLOCKS, 0.75f, 2.0f);
    public static final SoundEntry TRANSFORMER_HUM = register("transformer", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry SPARK = register("spark", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry GENERATOR = register("generator", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry ALARM_BELL = register("alarm_bell", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry ALARM_BELL_END = register("alarm_bell_end", SoundSource.BLOCKS, 1.0f, 1.0f);
    public static final SoundEntry FUSE_INSTALL = register("fuse_install", SoundSource.BLOCKS, 0.8f, 0.7f);
    public static final SoundEntry WIRE_BURNED = register("wire_burned", SoundSource.NEUTRAL, 0.2f, 2.0f);
    public static final SoundEntry BOOSTING = register("boosting", SoundSource.BLOCKS, 1.0f, 1.0f);

    private static SoundEntry register(String name, SoundSource category, float volume, float pitch) {
        Identifier id = PowerGrid.asResource(name);
        SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        return new SoundEntry(category, event, volume, pitch);
    }

    public static void register() { }
}
