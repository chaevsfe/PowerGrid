package org.patryk3211.powergrid.collections;

import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.electromagnet.recipe.MagnetizingRecipe;
import org.patryk3211.powergrid.electricity.light.string.StringLightCordRecipe;
import org.patryk3211.powergrid.equipment.BoostRecipe;

import java.util.Optional;

public class ModdedRecipeTypes {
    public static final RecipeType<MagnetizingRecipe> MAGNETIZATION = type("magnetization");
    public static final RecipeType<BoostRecipe> BOOSTING = type("boost_recipe");

    public static final ResourceKey<RecipePropertySet> BOOST_TARGET = ResourceKey.create(RecipePropertySet.TYPE_KEY, PowerGrid.asResource("boost_target"));
    public static final ResourceKey<RecipePropertySet> BOOST_INGREDIENT = ResourceKey.create(RecipePropertySet.TYPE_KEY, PowerGrid.asResource("boost_ingredient"));

    public static RecipeSerializer<MagnetizingRecipe> MAGNETIZATION_SERIALIZER;
    public static RecipeSerializer<BoostRecipe> BOOSTING_SERIALIZER;
    public static RecipeSerializer<StringLightCordRecipe> STRING_LIGHT_CORD_SERIALIZER;

    private static <T extends Recipe<?>> RecipeType<T> type(String name) {
        Identifier id = PowerGrid.asResource(name);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    private static <T extends Recipe<?>> RecipeSerializer<T> serializer(String name, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, PowerGrid.asResource(name), serializer);
    }

    public static void register() {
        MAGNETIZATION_SERIALIZER = serializer("magnetization", MagnetizingRecipe.SERIALIZER);
        BOOSTING_SERIALIZER = serializer("boost_recipe", BoostRecipe.SERIALIZER);
        STRING_LIGHT_CORD_SERIALIZER = serializer("crafting_special_string_light_cord", StringLightCordRecipe.SERIALIZER);

        AllRecipeTypes.DEPLOYER_RECIPES.add(BOOSTING);
        AllRecipeSets.ALL.put(BOOST_TARGET, recipe -> recipe instanceof BoostRecipe boost ? Optional.of(boost.target()) : Optional.empty());
        AllRecipeSets.ALL.put(BOOST_INGREDIENT, recipe -> recipe instanceof BoostRecipe boost ? Optional.of(boost.ingredient()) : Optional.empty());
    }
}
