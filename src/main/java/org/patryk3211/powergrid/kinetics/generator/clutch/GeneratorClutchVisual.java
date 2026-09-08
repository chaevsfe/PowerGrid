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
package org.patryk3211.powergrid.kinetics.generator.clutch;

import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.core.Direction;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer.getRotorAngle;

public class GeneratorClutchVisual extends SingleAxisRotatingVisual<GeneratorClutchBlockEntity> implements SimpleDynamicVisual {
    protected TransformedInstance assembly;

    public GeneratorClutchVisual(VisualizationContext context, GeneratorClutchBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick,
                Direction.get(blockEntity.getBlockState().getValue(GeneratorClutchBlock.FACING).getAxisDirection(), Direction.Axis.Z),
                        Models.partial(ModdedPartialModels.SHAFT_BIT));
        assembly = instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, Models.partial(ModdedPartialModels.CLUTCH_SHAFT, blockState.getValue(FACING).getOpposite()))
                .createInstance();
        transformAssembly();
    }

    public void transformAssembly() {
        var partial = AnimationTickHolder.getPartialTicks();
        var rotorAngle = getRotorAngle(blockEntity, partial);

        var dir = Direction.fromAxisAndDirection(blockState.getValue(GeneratorClutchBlock.FACING).getAxis(), Direction.AxisDirection.POSITIVE);
        assembly.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotate(rotorAngle, dir)
                .uncenter();
        assembly.setChanged();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context) {
        transformAssembly();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(assembly);
    }

    @Override
    protected void _delete() {
        super._delete();
        assembly.delete();
    }
}
