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
package org.patryk3211.powergrid.kinetics.base;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import net.minecraft.core.Direction;

public class TunedBlockVisual<T extends TunedBlockEntity> extends SingleAxisRotatingVisual<T> {
    private static Model getModel(TunedBlockEntity be) {
        return be.getBlockState().getValue(TunedBlock.BASE)
                ? Models.partial(AllPartialModels.SHAFT_HALF, Direction.UP)
                : Models.partial(AllPartialModels.SHAFT, Direction.NORTH);
    }

    public TunedBlockVisual(VisualizationContext context, T blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, getModel(blockEntity));
    }
}
