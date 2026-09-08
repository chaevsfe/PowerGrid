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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.patryk3211.powergrid.registrate.builders.BlockBuilder;
import org.patryk3211.powergrid.registrate.fn.NonNullUnaryOperator;

public final class TagGen {
    public static final TagKey<Block> MINEABLE_WITH_PICKAXE = blockTag("mineable/pickaxe");
    public static final TagKey<Block> MINEABLE_WITH_AXE = blockTag("mineable/axe");

    private TagGen() {
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(path));
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> pickaxeOnly() {
        return builder -> builder.tag(MINEABLE_WITH_PICKAXE);
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> axeOrPickaxe() {
        return builder -> builder.tag(MINEABLE_WITH_AXE, MINEABLE_WITH_PICKAXE);
    }
}
