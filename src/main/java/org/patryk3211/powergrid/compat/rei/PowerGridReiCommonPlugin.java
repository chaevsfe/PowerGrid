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
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;

public class PowerGridReiCommonPlugin implements REICommonPlugin {
    @Override
    public double getPriority() {
        return CreateReiApi.PLUGIN_PRIORITY;
    }

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        if (!PowerGridReiSupport.available()) {
            return;
        }
        PowerGridReiDisplays.register(registry);
    }

    @Override
    public void postStage(PluginManager<REICommonPlugin> manager, ReloadStage stage) {
        if (!PowerGridReiSupport.available()) {
            return;
        }
        PowerGridReiDisplays.report(manager, stage);
    }
}
