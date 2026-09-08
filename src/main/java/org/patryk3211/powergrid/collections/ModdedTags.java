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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.patryk3211.powergrid.PowerGrid;

public class ModdedTags {
    public static final String FORGE_NAMESPACE = "c";
    public static final String SABLE_NAMESPACE = "sable";

    public enum Item {
        RAW_ORES(FORGE_NAMESPACE, "raw_ores"),
        PLATES(FORGE_NAMESPACE, "plates"),
        WIRES(FORGE_NAMESPACE, "wires"),
        LIGHT_WIRES("light_wires"),
        COILS(FORGE_NAMESPACE, "coils"),
        CIRCUIT_SCHEMATIC_HOLDER("circuit_schematic_holder"),
        CIRCUIT_COMPONENT("circuit_component"),
        FUSE_RESETTING("fuse_resetting"),
        WIRE_CUTTERS("wire_cutters"),
        BAD_WIRE_CUTTERS("bad_wire_cutters"),
        ZINC_INGOTS(FORGE_NAMESPACE, "ingots/zinc")
        ;

        public final TagKey<net.minecraft.world.item.Item> tag;

        Item(String name) {
            this(PowerGrid.MOD_ID, name);
        }

        Item(String namespace, String name) {
            tag = itemTag(Identifier.fromNamespaceAndPath(namespace, name));
        }
    }

    public enum Block {
        SILVER_ORES(FORGE_NAMESPACE, "silver_ores"),
        AFFECTED_BY_LAMP("affected_by_lamp"),
        IGNORE_IN_ROTOR_ASSEMBLY_SIZE("ignore_in_rotor_assembly_size"),
        CONDUCTIVE_GROUND("conductive_ground"),
        CARBON_PILE_BLOCK("carbon_pile_block"),

        SABLE_QUARTER_VOLUME(SABLE_NAMESPACE, "quarter_volume"),
        SABLE_HALF_VOLUME(SABLE_NAMESPACE, "half_volume"),

        SABLE_SUPER_LIGHT(SABLE_NAMESPACE, "super_light"),
        SABLE_LIGHT(SABLE_NAMESPACE, "light"),
        SABLE_HEAVY(SABLE_NAMESPACE, "heavy"),
        SABLE_SUPER_HEAVY(SABLE_NAMESPACE, "super_heavy"),

        GLASS_BLOCK(FORGE_NAMESPACE, "glass_blocks"),
        GLASS_PANE(FORGE_NAMESPACE, "glass_panes"),
        SOLAR_QUARTER_LIGHT("solar_quarter_light"),
        SOLAR_HALF_LIGHT("solar_half_light"),
        SOLAR_3QUARTER_LIGHT("solar_three_quarters_light"),
        SOLAR_FULL_LIGHT("solar_full_light"),
        ;

        public final TagKey<net.minecraft.world.level.block.Block> tag;

        Block(String name) {
            this(PowerGrid.MOD_ID, name);
        }

        Block(String namespace, String name) {
            tag = blockTag(Identifier.fromNamespaceAndPath(namespace, name));
        }
    }

    public enum Entity {
        RETAIN_IN_SUB_LEVEL(SABLE_NAMESPACE, "retain_in_sub_level");

        public final TagKey<EntityType<?>> tag;

        Entity(String name) {
            this(PowerGrid.MOD_ID, name);
        }

        Entity(String namespace, String name) {
            tag = entityTag(Identifier.fromNamespaceAndPath(namespace, name));
        }
    }

    public static TagKey<net.minecraft.world.level.block.Block> blockTag(Identifier id) {
        return TagKey.create(Registries.BLOCK, id);
    }

    public static TagKey<net.minecraft.world.item.Item> itemTag(Identifier id) {
        return TagKey.create(Registries.ITEM, id);
    }

    public static TagKey<EntityType<?>> entityTag(Identifier id) {
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }

    public static TagKey<net.minecraft.world.item.Item> forgeItemTag(String path) {
        return itemTag(Identifier.fromNamespaceAndPath(FORGE_NAMESPACE, path));
    }

    public static TagKey<net.minecraft.world.level.block.Block> forgeBlockTag(String path) {
        return blockTag(Identifier.fromNamespaceAndPath(FORGE_NAMESPACE, path));
    }

    public static TagKey<net.minecraft.world.item.Item> plates(String ingot) {
        return forgeItemTag("plates/" + ingot);
    }

    public static TagKey<net.minecraft.world.item.Item> nuggets(String ingot) {
        return forgeItemTag("nuggets/" + ingot);
    }

    public static TagKey<net.minecraft.world.item.Item> ingots(String ingot) {
        return forgeItemTag("ingots/" + ingot);
    }

    public static TagKey<net.minecraft.world.item.Item> wires(String ingot) {
        return forgeItemTag("wires/" + ingot);
    }
}
