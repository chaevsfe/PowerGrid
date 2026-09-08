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
package org.patryk3211.powergrid.electricity.electromagnet.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;

import java.util.List;

public record MagnetizingRecipe(List<ProcessingOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<MagnetizingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingOutput.CODEC.listOf(1, 3).fieldOf("results").forGetter(MagnetizingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MagnetizingRecipe::ingredient)
    ).apply(instance, MagnetizingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MagnetizingRecipe> STREAM_CODEC = StreamCodec.composite(
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), MagnetizingRecipe::results,
            Ingredient.CONTENTS_STREAM_CODEC, MagnetizingRecipe::ingredient,
            MagnetizingRecipe::new);

    public static final RecipeSerializer<MagnetizingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<MagnetizingRecipe> getSerializer() {
        return ModdedRecipeTypes.MAGNETIZATION_SERIALIZER;
    }

    @Override
    public RecipeType<MagnetizingRecipe> getType() {
        return ModdedRecipeTypes.MAGNETIZATION;
    }
}
