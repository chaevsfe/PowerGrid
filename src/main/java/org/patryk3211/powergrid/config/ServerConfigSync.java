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
package org.patryk3211.powergrid.config;

import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.ConfigValue;
import com.zurrtum.create.catnip.config.DoubleRawValue;
import net.minecraft.nbt.CompoundTag;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.mixin.ConfigBaseAccessor;

import java.util.IdentityHashMap;
import java.util.Map;

public class ServerConfigSync {
    private static final Map<ConfigValue<?>, Object> ORIGINAL_VALUES = new IdentityHashMap<>();
    private static final Map<DoubleRawValue, Double> ORIGINAL_RAW_VALUES = new IdentityHashMap<>();

    public static CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        write(ModdedConfigs.server(), tag);
        return tag;
    }

    public static void apply(CompoundTag tag) {
        apply(ModdedConfigs.server(), tag);
    }

    public static void restore() {
        ORIGINAL_VALUES.forEach(ServerConfigSync::set);
        ORIGINAL_VALUES.clear();
        ORIGINAL_RAW_VALUES.forEach(DoubleRawValue::set);
        ORIGINAL_RAW_VALUES.clear();
    }

    private static void write(ConfigBase config, CompoundTag tag) {
        var accessor = (ConfigBaseAccessor) config;
        for (ConfigBase.CValue<?> cValue : accessor.getAllValues()) {
            ConfigValue<?> value = ((ConfigBaseAccessor.CValueAccessor<?>) cValue).getValue();
            if (value == null)
                continue;
            Object current = value.get();
            String name = cValue.getName();
            if (current instanceof Boolean v)
                tag.putBoolean(name, v);
            else if (current instanceof Integer v)
                tag.putInt(name, v);
            else if (current instanceof Float v)
                tag.putFloat(name, v);
            else if (current instanceof Double v)
                tag.putDouble(name, v);
            else if (current instanceof String v)
                tag.putString(name, v);
            else if (current instanceof Enum<?> v)
                tag.putString(name, v.name());
        }
        if (config instanceof SyncedValues synced) {
            synced.syncedGroups().forEach((group, values) -> {
                CompoundTag groupTag = new CompoundTag();
                values.forEach((id, value) -> groupTag.putDouble(id.toString(), value.get()));
                tag.put(group, groupTag);
            });
        }
        for (ConfigBase child : accessor.getChildren()) {
            CompoundTag childTag = new CompoundTag();
            write(child, childTag);
            tag.put(child.getName(), childTag);
        }
    }

    private static void apply(ConfigBase config, CompoundTag tag) {
        var accessor = (ConfigBaseAccessor) config;
        for (ConfigBase.CValue<?> cValue : accessor.getAllValues()) {
            ConfigValue<?> value = ((ConfigBaseAccessor.CValueAccessor<?>) cValue).getValue();
            if (value == null)
                continue;
            Object current = value.get();
            String name = cValue.getName();
            if (!tag.contains(name))
                continue;
            Object synced = read(tag, name, current);
            if (synced == null || synced.equals(current))
                continue;
            ORIGINAL_VALUES.putIfAbsent(value, current);
            set(value, synced);
        }
        if (config instanceof SyncedValues synced) {
            synced.syncedGroups().forEach((group, values) -> {
                CompoundTag groupTag = tag.getCompoundOrEmpty(group);
                values.forEach((id, value) -> {
                    String key = id.toString();
                    if (!groupTag.contains(key))
                        return;
                    double current = value.get();
                    double syncedValue = groupTag.getDoubleOr(key, current);
                    if (syncedValue == current)
                        return;
                    ORIGINAL_RAW_VALUES.putIfAbsent(value, current);
                    value.set(syncedValue);
                });
            });
        }
        for (ConfigBase child : accessor.getChildren())
            apply(child, tag.getCompoundOrEmpty(child.getName()));
    }

    private static Object read(CompoundTag tag, String name, Object current) {
        if (current instanceof Boolean v)
            return tag.getBooleanOr(name, v);
        if (current instanceof Integer v)
            return tag.getIntOr(name, v);
        if (current instanceof Float v)
            return tag.getFloatOr(name, v);
        if (current instanceof Double v)
            return tag.getDoubleOr(name, v);
        if (current instanceof String v)
            return tag.getStringOr(name, v);
        if (current instanceof Enum<?> v) {
            String synced = tag.getStringOr(name, v.name());
            for (Object constant : v.getDeclaringClass().getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(synced))
                    return constant;
            }
            PowerGrid.LOGGER.warn("Server sent unknown value '{}' for config option '{}'", synced, name);
            return null;
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void set(ConfigValue<?> value, Object object) {
        ((ConfigValue) value).set(object);
    }
}
