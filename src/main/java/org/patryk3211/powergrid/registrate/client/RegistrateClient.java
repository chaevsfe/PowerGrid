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
package org.patryk3211.powergrid.registrate.client;

import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.patryk3211.powergrid.registrate.ClientHookSink;
import org.patryk3211.powergrid.registrate.Registrate;
import org.patryk3211.powergrid.registrate.fn.NonNullFunction;
import org.patryk3211.powergrid.utility.SimpleBlockEntityVisualFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class RegistrateClient implements ClientHookSink {
    private RegistrateClient() {
    }

    public static void flush(Registrate registrate) {
        RegistrateClient sink = new RegistrateClient();
        for (Consumer<ClientHookSink> hook : Registrate.drainClientHooks())
            hook.accept(sink);
    }

    @Override
    public <T extends BlockEntity> void renderer(BlockEntityType<T> type, Supplier<?> renderer) {
        AllBlockEntityRenders.render(type, provider(renderer));
    }

    @Override
    public <T extends BlockEntity> void visual(BlockEntityType<T> type, Supplier<?> renderer, Supplier<?> visual, Predicate<T> renderNormally) {
        SimpleBlockEntityVisualizer.Factory<T> factory = visualizer(visual);
        Predicate<T> skipVanillaRender = entity -> !renderNormally.test(entity);
        if (renderer == null) {
            new SimpleBlockEntityVisualizer.Builder<>(type).factory(factory).skipVanillaRender(skipVanillaRender).apply();
            return;
        }
        AllBlockEntityRenders.visual(type, provider(renderer), factory, skipVanillaRender);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityRendererProvider<T, ?> provider(Supplier<?> renderer) {
        NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<T, ?>> function =
                (NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<T, ?>>) renderer.get();
        return context -> castRenderer(function.apply(context));
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> BlockEntityRenderer<T, S> castRenderer(BlockEntityRenderer<T, ?> renderer) {
        return (BlockEntityRenderer<T, S>) renderer;
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> SimpleBlockEntityVisualizer.Factory<T> visualizer(Supplier<?> visual) {
        SimpleBlockEntityVisualFactory<T> recorded = (SimpleBlockEntityVisualFactory<T>) visual.get();
        return recorded::create;
    }
}
