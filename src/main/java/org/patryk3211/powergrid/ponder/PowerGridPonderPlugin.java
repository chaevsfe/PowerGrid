/*
 * Copyright 2025 patryk3211
 * Modified 2026 by chaevsfe for the unofficial Fabric / Create Fly 26.2 port.
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
package org.patryk3211.powergrid.ponder;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.Identifier;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;

public class PowerGridPonderPlugin implements PonderPlugin {
    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        PowerGridPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
        PowerGridPonderTags.register(helper);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        helper.registerSharedText("gauge_range", "You can change the gauge's range by clicking on top of it");
        helper.registerSharedText("gauge_goggles", "When wearing Engineers' Goggles, the player can get more detailed information from the Gauge");
        helper.registerSharedText("gauge_customize", "You can change how the measured value is displayed by right-clicking the gauge with a name tag");
    }

    @Override
    public String getModId() {
        return PowerGrid.MOD_ID;
    }

    @Override
    public void onPonderLevelRestore(PonderLevel ponderLevel) {
        for(var be : ponderLevel.getBlockEntities()) {
            var electric = BlockEntityBehaviour.get(be, ElectricBehaviour.TYPE);
            if(electric != null)
                electric.unpause();
        }
    }
}
