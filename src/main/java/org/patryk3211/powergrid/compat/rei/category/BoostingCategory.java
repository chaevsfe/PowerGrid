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
package org.patryk3211.powergrid.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import dev.chaevsfe.createreiviewer.client.category.CreateReiCategory;
import dev.chaevsfe.createreiviewer.client.widget.CreateReiLayout;
import dev.chaevsfe.createreiviewer.client.widget.TwoItemRenderer;
import dev.chaevsfe.createreiviewer.client.widget.Panel;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.compat.rei.PowerGridReiCategories;

import java.util.List;

public class BoostingCategory extends CreateReiCategory<CreateReiDisplay> {
    public BoostingCategory() {
        super(PowerGridReiCategories.titleKey("boosting"));
    }

    @Override
    public CategoryIdentifier<? extends CreateReiDisplay> getCategoryIdentifier() {
        return PowerGridReiCategories.BOOSTING;
    }

    @Override
    public Renderer getIcon() {
        return new TwoItemRenderer(AllItems.DEPLOYER, ModdedItems.MAGNET);
    }

    @Override
    protected int contentHeight() {
        return 70;
    }

    @Override
    protected int contentOverhangTop() {
        return 10;
    }

    @Override
    protected void build(CreateReiDisplay display, Panel panel) {
        List<EntryIngredient> outputs = display.outputs();
        panel.texture(AllGuiTextures.JEI_SHADOW, 62, 57);
        panel.texture(AllGuiTextures.JEI_DOWN_ARROW, 126, outputs.size() > 2 ? 10 : 29);
        panel.pip(75, -10, DeployerRenderState::new);
        panel.slot(51, 5, CreateReiLayout.heldItem(display));
        panel.slot(27, 51, display.inputs().get(1));
        if (outputs.size() == 1) {
            panel.output(132, 51, outputs.get(0), display.chance(0));
            return;
        }
        CreateReiLayout.outputGrid(panel, display, 142, 51);
    }
}
