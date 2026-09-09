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

import com.zurrtum.create.AllItems;
import dev.chaevsfe.createreiviewer.api.CreateReiClientReport;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.compat.rei.category.BoostingCategory;
import org.patryk3211.powergrid.compat.rei.category.MagnetizingCategory;

public final class PowerGridReiClientCategories {
    private PowerGridReiClientCategories() {
    }

    public static void register(CategoryRegistry registry) {
        registry.add(new MagnetizingCategory(), new BoostingCategory());
        registry.addWorkstations(PowerGridReiCategories.MAGNETIZING,
            EntryStacks.of(ModdedBlocks.ELECTROMAGNET.asStack()));
        registry.addWorkstations(PowerGridReiCategories.BOOSTING,
            EntryStacks.of(AllItems.DEPLOYER), EntryStacks.of(AllItems.DEPOT), EntryStacks.of(AllItems.BELT_CONNECTOR));
        CreateReiClientReport.register("Power Grid", PowerGridReiCategories.ALL);
        PowerGrid.LOGGER.info("Registered {} Power Grid REI categories", PowerGridReiCategories.ALL.size());
    }
}
