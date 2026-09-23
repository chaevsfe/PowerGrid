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

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import dev.chaevsfe.createreiviewer.api.ViewerIngredient;
import dev.chaevsfe.createreiviewer.api.ViewerRecipe;
import dev.chaevsfe.createreiviewer.api.client.CreateViewerClientPlugin;
import dev.chaevsfe.createreiviewer.api.client.ViewerCanvas;
import dev.chaevsfe.createreiviewer.api.client.ViewerCategory;
import dev.chaevsfe.createreiviewer.api.client.ViewerCategoryRegistry;
import dev.chaevsfe.createreiviewer.api.client.ViewerLayouts;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedItems;

import java.util.List;

public final class PowerGridViewerClientPlugin implements CreateViewerClientPlugin {
    @Override
    public void registerCategories(ViewerCategoryRegistry registry) {
        registry.add(ViewerCategory.builder(PowerGridViewerPlugin.MAGNETIZING)
            .title("powergrid.recipe.magnetizing")
            .icon(ModdedBlocks.ELECTROMAGNET)
            .height(77)
            .overhangTop(11)
            .workstations(ModdedBlocks.ELECTROMAGNET.asStack())
            .layout(PowerGridViewerClientPlugin::magnetizing)
            .build());
        registry.add(ViewerCategory.builder(PowerGridViewerPlugin.BOOSTING)
            .title("powergrid.recipe.boosting")
            .icon(AllItems.DEPLOYER, ModdedItems.MAGNET)
            .height(70)
            .overhangTop(10)
            .workstations(AllItems.DEPLOYER, AllItems.DEPOT, AllItems.BELT_CONNECTOR)
            .layout(PowerGridViewerClientPlugin::boosting)
            .build());
    }

    private static void magnetizing(ViewerRecipe recipe, ViewerCanvas canvas) {
        canvas.texture(AllGuiTextures.JEI_SHADOW, 61, 41);
        canvas.texture(AllGuiTextures.JEI_LONG_ARROW, 52, 54);
        canvas.pip(71, 22, (pose, x, y) -> new TallBlockRenderState(pose, ModdedBlocks.ELECTROMAGNET.getDefaultState(), x, y, 6));
        canvas.slot(27, 51, recipe.input(0));
        List<ViewerIngredient> outputs = recipe.outputs();
        for (int i = 0; i < outputs.size(); i++) {
            canvas.output(132 + 19 * i, 51, outputs.get(i), recipe.chance(i));
        }
    }

    private static void boosting(ViewerRecipe recipe, ViewerCanvas canvas) {
        List<ViewerIngredient> outputs = recipe.outputs();
        canvas.texture(AllGuiTextures.JEI_SHADOW, 62, 57);
        canvas.texture(AllGuiTextures.JEI_DOWN_ARROW, 126, outputs.size() > 2 ? 10 : 29);
        canvas.pip(75, -10, DeployerRenderState::new);
        ViewerLayouts.heldItem(canvas, recipe, 51, 5);
        canvas.slot(27, 51, recipe.input(1));
        if (outputs.size() == 1) {
            canvas.output(132, 51, outputs.get(0), recipe.chance(0));
            return;
        }
        ViewerLayouts.outputGrid(canvas, recipe, 142, 51);
    }
}
