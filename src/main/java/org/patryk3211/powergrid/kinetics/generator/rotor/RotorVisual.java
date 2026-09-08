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
package org.patryk3211.powergrid.kinetics.generator.rotor;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.utility.SimpleBlockEntityVisualFactory;

import java.util.function.Consumer;

import static org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer.getRotorAngle;

public class RotorVisual<T extends RotorBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
    protected TransformedInstance assembly;

    public static <T extends RotorBlockEntity> SimpleBlockEntityVisualFactory<T> of(PartialModel model) {
        return (ctx, be, partialTick) -> {
            var state = be.getBlockState();
            var axis = ((AbstractRotorBlock) state.getBlock()).getAssemblyRotationAxis(state);
            var facing = Direction.get(Direction.AxisDirection.POSITIVE, axis);
            var model1 = Models.partial(model, facing);
            return new RotorVisual<>(ctx, be, partialTick, model1);
        };
    }

    public RotorVisual(VisualizationContext ctx, T blockEntity, float partialTick, Model model) {
        super(ctx, blockEntity, partialTick);
        assembly = instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, model)
                .createInstance();

        transformAssembly();
    }

    public Direction.Axis getRotationAxis() {
        return ((AbstractRotorBlock) blockState.getBlock()).getAssemblyRotationAxis(blockState);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(assembly);
    }

    @Override
    public void updateLight(float v) {
        relight(assembly);
    }

    @Override
    protected void _delete() {
        assembly.delete();
    }

    @Override
    public void beginFrame(Context context) {
        transformAssembly();
    }

    public void transformAssembly() {
        var partial = AnimationTickHolder.getPartialTicks();
        var rotorAngle = getRotorAngle(blockEntity, partial);

        var dir = Direction.fromAxisAndDirection(getRotationAxis(), Direction.AxisDirection.POSITIVE);
        assembly.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotate(rotorAngle, dir)
                .uncenter();
        assembly.setChanged();
    }
}
