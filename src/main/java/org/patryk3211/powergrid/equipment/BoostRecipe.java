package org.patryk3211.powergrid.equipment;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationInput;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;

import java.util.List;

public record BoostRecipe(List<ProcessingOutput> results, boolean keepHeldItem, Ingredient target, Ingredient ingredient) implements ItemApplicationRecipe {
    public static final MapCodec<BoostRecipe> MAP_CODEC = ItemApplicationRecipe.createCodec(BoostRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BoostRecipe> STREAM_CODEC = ItemApplicationRecipe.createStreamCodec(BoostRecipe::new);
    public static final RecipeSerializer<BoostRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(ItemApplicationInput input, Level level) {
        return ItemApplicationRecipe.super.matches(input, level) && !ItemBoostUtils.isBoosted(input.target());
    }

    @Override
    public List<ItemStack> assemble(ItemApplicationInput input, RandomSource random) {
        List<ItemStack> output = ItemApplicationRecipe.super.assemble(input, random);
        for (ItemStack stack : output)
            ItemBoostUtils.setBoosted(stack, true);
        return output;
    }

    @Override
    public RecipeSerializer<BoostRecipe> getSerializer() {
        return ModdedRecipeTypes.BOOSTING_SERIALIZER;
    }

    @Override
    public RecipeType<BoostRecipe> getType() {
        return ModdedRecipeTypes.BOOSTING;
    }
}
