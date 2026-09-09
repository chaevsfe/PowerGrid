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
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import org.patryk3211.powergrid.PowerGrid;

import java.util.List;

public final class PowerGridReiCategories {
    public static final CategoryIdentifier<CreateReiDisplay> MAGNETIZING = of("magnetizing");
    public static final CategoryIdentifier<CreateReiDisplay> BOOSTING = of("boosting");

    public static final List<CategoryIdentifier<? extends CreateReiDisplay>> ALL = List.of(MAGNETIZING, BOOSTING);

    private PowerGridReiCategories() {
    }

    private static CategoryIdentifier<CreateReiDisplay> of(String path) {
        return CreateReiApi.category(PowerGrid.MOD_ID, path);
    }

    public static String titleKey(String path) {
        return PowerGrid.MOD_ID + ".recipe." + path;
    }
}
