/*
 * Copyright 2026 chaevsfe
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
package org.patryk3211.powergrid.foundation;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class SharedProperties {
    private SharedProperties() {
    }

    public static Block wooden() {
        return Blocks.STRIPPED_SPRUCE_WOOD;
    }

    public static Block stone() {
        return Blocks.ANDESITE;
    }

    public static Block softMetal() {
        return Blocks.GOLD_BLOCK;
    }

    public static Block copperMetal() {
        return Blocks.COPPER_BLOCK;
    }
}
