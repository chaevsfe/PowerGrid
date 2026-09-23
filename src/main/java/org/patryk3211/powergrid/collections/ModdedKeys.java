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

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.patryk3211.powergrid.PowerGrid;

/**
 * @see com.zurrtum.create.client.AllKeys
 */
public enum ModdedKeys {
    CATEGORY_POWER_GRID("main"),

    ALTERNATE_WIRE_PLACEMENT("alternate_wire_placement", InputConstants.KEY_LCONTROL),

    CATEGORY_CIRCUIT_EDITOR("circuit_editor"),

    ROTATE_COMPONENT("rotate_component", InputConstants.KEY_R),
    PLACE_TRACE("place_trace", InputConstants.KEY_T),
    DELETE_AREA("delete_area", InputConstants.KEY_D),
    PICK_COMPONENT("pick_component", InputConstants.KEY_S),
    SWITCH_LAYER("switch_layer", InputConstants.KEY_X),

    ;

    public KeyMapping keybind;
    public String description;
    public int key;

    public final String categoryPath;

    ModdedKeys(String description, int defaultKey) {
        this.description = PowerGrid.MOD_ID + ".keyinfo." + description;
        this.key = defaultKey;
        this.categoryPath = null;
    }

    ModdedKeys(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    @Environment(EnvType.CLIENT)
    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.MISC;
        for (ModdedKeys entry : values()) {
            if (entry.categoryPath != null) {
                category = KeyMapping.Category.register(PowerGrid.asResource(entry.categoryPath));
                continue;
            }
            entry.keybind = new KeyMapping(entry.description, entry.key, category);
            KeyMappingHelper.registerKeyMapping(entry.keybind);
        }
    }

    public KeyMapping getKeybind() {
        return keybind;
    }

    public boolean isPressed() {
        return keybind != null && keybind.isDown();
    }

    public String getBoundKey() {
        return keybind == null ? "" : keybind.getTranslatedKeyMessage().getString().toUpperCase();
    }

    public boolean matches(KeyEvent event) {
        return keybind != null && keybind.matches(event);
    }

    public boolean matchesMouse(MouseButtonEvent event) {
        return keybind != null && keybind.matchesMouse(event);
    }
}
