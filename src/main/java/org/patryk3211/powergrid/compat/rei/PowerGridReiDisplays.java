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
package org.patryk3211.powergrid.compat.rei;

import dev.chaevsfe.createreiviewer.api.CreateReiApi;
import dev.chaevsfe.createreiviewer.api.CreateReiDisplayBuilder;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;
import org.patryk3211.powergrid.electricity.electromagnet.recipe.MagnetizingRecipe;
import org.patryk3211.powergrid.equipment.BoostRecipe;

public final class PowerGridReiDisplays {
    private PowerGridReiDisplays() {
    }

    public static void register(ServerDisplayRegistry registry) {
        CreateReiApi.fill(registry, MagnetizingRecipe.class, ModdedRecipeTypes.MAGNETIZATION,
            PowerGridReiDisplays::magnetizing);
        CreateReiApi.fill(registry, BoostRecipe.class, ModdedRecipeTypes.BOOSTING,
            PowerGridReiDisplays::boosting);
        PowerGrid.LOGGER.info("Recipe fillers registered for {} Power Grid categories", PowerGridReiCategories.ALL.size());
    }

    public static void report(PluginManager<REICommonPlugin> manager, ReloadStage stage) {
        CreateReiApi.report(manager, stage, "Power Grid", PowerGridReiCategories.ALL, PowerGrid.LOGGER);
    }

    private static CreateReiDisplay magnetizing(RecipeHolder<MagnetizingRecipe> holder) {
        MagnetizingRecipe recipe = holder.value();
        return CreateReiDisplayBuilder.of(PowerGridReiCategories.MAGNETIZING)
            .input(recipe.ingredient())
            .results(recipe.results())
            .location(holder)
            .build();
    }

    private static CreateReiDisplay boosting(RecipeHolder<BoostRecipe> holder) {
        BoostRecipe recipe = holder.value();
        return CreateReiDisplayBuilder.of(PowerGridReiCategories.BOOSTING)
            .input(recipe.ingredient())
            .input(recipe.target())
            .results(recipe.results())
            .keepHeldItem(recipe.keepHeldItem())
            .location(holder)
            .build();
    }
}
