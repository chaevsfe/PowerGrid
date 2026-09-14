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
package org.patryk3211.powergrid.client.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ModdedTooltipBehaviours {
    public static class Device<T extends SmartBlockEntity & IHaveGoggleInformation> extends TooltipBehaviour<T> implements IHaveGoggleInformation {
        public Device(T blockEntity) {
            super(blockEntity);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            int before = tooltip.size();
            return blockEntity.addToGoggleTooltip(tooltip, isPlayerSneaking) && tooltip.size() > before;
        }
    }

    public static class Kinetic<T extends KineticBlockEntity & IHaveGoggleInformation> extends KineticTooltipBehaviour<T> {
        public Kinetic(T blockEntity) {
            super(blockEntity);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            int before = tooltip.size();
            boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
            added |= blockEntity.addToGoggleTooltip(tooltip, isPlayerSneaking);
            return added && tooltip.size() > before;
        }
    }
}
