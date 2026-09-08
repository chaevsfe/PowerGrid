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
package org.patryk3211.powergrid.client.model;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItemRenderer;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class MultimeterModel implements ItemModel {
    public static final Identifier ID = PowerGrid.asResource("model/multimeter");
    public static final Identifier ITEM_ID = PowerGrid.asResource("item/multimeter");
    public static final Identifier NEEDLE_ID = PowerGrid.asResource("item/multimeter/indicator");

    private static final float NEEDLE_PIVOT = 0.5f + (5.75f - 8) / 16;

    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> itemExtents;
    private final List<BakedQuad> needleQuads;
    private final Supplier<Vector3fc[]> needleExtents;

    public MultimeterModel(List<BakedQuad> itemQuads, ModelRenderProperties settings, List<BakedQuad> needleQuads) {
        this.itemQuads = itemQuads;
        this.settings = settings;
        this.itemExtents = ItemModelParts.extents(itemQuads);
        this.needleQuads = needleQuads;
        this.needleExtents = ItemModelParts.extents(needleQuads);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext transformType, ClientLevel level, ItemOwner owner, int seed) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        var itemLayer = ItemModelRenderHelper.submitQuads(state, settings, transformType, itemQuads);
        itemLayer.setExtents(itemExtents);
        var needleLayer = ItemModelRenderHelper.submitQuads(state, settings, transformType, needleQuads);
        needleLayer.setExtents(needleExtents);

        if(!transformType.firstPerson())
            return;
        var angle = -Math.PI / 4 + Math.PI / 2 * MultimeterItemRenderer.getDialState(stack);
        needleLayer.setLocalTransform(new Matrix4f().rotateAround(new Quaternionf().rotateZ((float) angle),
                0.5f, NEEDLE_PIVOT, 0.5f));
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(NEEDLE_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            var baker = context.blockModelBaker();
            var parts = ItemModelParts.of(baker, ITEM_ID);
            return new MultimeterModel(parts.quads(), parts.properties(), ItemModelParts.quads(baker, NEEDLE_ID));
        }
    }
}
