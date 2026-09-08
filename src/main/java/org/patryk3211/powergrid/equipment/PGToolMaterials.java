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

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import org.patryk3211.powergrid.collections.ModdedTags;

public final class PGToolMaterials {
    public static final ToolMaterial ZINC = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 150, 6.0f, 1.5f, 12, ModdedTags.Item.ZINC_INGOTS.tag);
    public static final ToolMaterial ZINC_DRILL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 250, 8.0f, 3.0f, 12, ModdedTags.Item.ZINC_INGOTS.tag);

    private PGToolMaterials() { }
}
