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
package org.patryk3211.powergrid.equipment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedTags;

import java.util.EnumMap;
import java.util.Map;

public class ZincArmorMaterial {
    public static final ResourceKey<EquipmentAsset> ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, PowerGrid.asResource("zinc"));

    private static final Map<ArmorType, Integer> DEFENSES = new EnumMap<>(ArmorType.class);

    static {
        DEFENSES.put(ArmorType.HELMET, 1);
        DEFENSES.put(ArmorType.CHESTPLATE, 3);
        DEFENSES.put(ArmorType.LEGGINGS, 2);
        DEFENSES.put(ArmorType.BOOTS, 1);
        DEFENSES.put(ArmorType.BODY, 3);
    }

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            15,
            DEFENSES,
            12,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            0.0f,
            0.0f,
            ModdedTags.Item.ZINC_INGOTS.tag,
            ASSET
    );
}
