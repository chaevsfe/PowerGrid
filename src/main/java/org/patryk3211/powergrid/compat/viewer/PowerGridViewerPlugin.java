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
package org.patryk3211.powergrid.compat.viewer;

import dev.chaevsfe.createreiviewer.api.CreateViewerPlugin;
import dev.chaevsfe.createreiviewer.api.ViewerRecipeRegistry;
import net.minecraft.resources.Identifier;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;
import org.patryk3211.powergrid.electricity.electromagnet.recipe.MagnetizingRecipe;
import org.patryk3211.powergrid.equipment.BoostRecipe;

public final class PowerGridViewerPlugin implements CreateViewerPlugin {
    public static final Identifier MAGNETIZING = PowerGrid.asResource("magnetizing");
    public static final Identifier BOOSTING = PowerGrid.asResource("boosting");

    @Override
    public void registerRecipes(ViewerRecipeRegistry registry) {
        registry.add(MAGNETIZING, ModdedRecipeTypes.MAGNETIZATION, MagnetizingRecipe.class, (holder, recipe) -> recipe
            .input(holder.value().ingredient())
            .results(holder.value().results())
            .build());
        registry.add(BOOSTING, ModdedRecipeTypes.BOOSTING, BoostRecipe.class, (holder, recipe) -> recipe
            .input(holder.value().ingredient())
            .input(holder.value().target())
            .results(holder.value().results())
            .keepHeldItem(holder.value().keepHeldItem())
            .build());
        registry.synchronize(ModdedRecipeTypes.MAGNETIZATION_SERIALIZER, ModdedRecipeTypes.BOOSTING_SERIALIZER);
    }
}
