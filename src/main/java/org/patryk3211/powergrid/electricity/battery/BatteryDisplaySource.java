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
package org.patryk3211.powergrid.electricity.battery;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.utility.Lang;

public class BatteryDisplaySource extends PercentOrProgressBarDisplaySource {
    @Override
    protected @Nullable Float getProgress(DisplayLinkContext context) {
        if(context.getSourceBlockEntity() instanceof MultiBlockBatteryEntity mbe) {
            var controller = mbe.getControllerBE();
            var progress = controller.getEnergy() / controller.getCapacity();
            return (float) progress;
        }
        return 0f;
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return getMode(context) == 0;
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    public int getMode(DisplayLinkContext context) {
        return context.sourceConfig().getIntOr("Mode", 0);
    }

}
