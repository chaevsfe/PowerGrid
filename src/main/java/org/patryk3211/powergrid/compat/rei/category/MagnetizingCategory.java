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

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import dev.chaevsfe.createreiviewer.client.category.CreateReiCategory;
import dev.chaevsfe.createreiviewer.client.widget.OneItemRenderer;
import dev.chaevsfe.createreiviewer.client.widget.Panel;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.compat.rei.PowerGridReiCategories;

import java.util.List;

public class MagnetizingCategory extends CreateReiCategory<CreateReiDisplay> {
    public MagnetizingCategory() {
        super(PowerGridReiCategories.titleKey("magnetizing"));
    }

    @Override
    public CategoryIdentifier<? extends CreateReiDisplay> getCategoryIdentifier() {
        return PowerGridReiCategories.MAGNETIZING;
    }

    @Override
    public Renderer getIcon() {
        return new OneItemRenderer(ModdedBlocks.ELECTROMAGNET);
    }

    @Override
    protected int contentHeight() {
        return 77;
    }

    @Override
    protected int contentOverhangTop() {
        return 11;
    }

    @Override
    protected void build(CreateReiDisplay display, Panel panel) {
        panel.texture(AllGuiTextures.JEI_SHADOW, 61, 41);
        panel.texture(AllGuiTextures.JEI_LONG_ARROW, 52, 54);
        panel.blockPip(71, 22, ModdedBlocks.ELECTROMAGNET.getDefaultState());
        panel.slot(27, 51, display.inputs().get(0));

        List<EntryIngredient> outputs = display.outputs();
        for (int i = 0; i < outputs.size(); i++) {
            panel.output(132 + 19 * i, 51, outputs.get(i), display.chance(i));
        }
    }
}
