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

import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
import org.joml.Vector3fc;
import org.patryk3211.powergrid.PowerGrid;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BoostingChipModel implements ItemModel {
    public static final Identifier ID = PowerGrid.asResource("model/integrated_circuit");
    public static final Identifier ITEM_ID = PowerGrid.asResource("item/integrated_circuit");

    private final List<BakedQuad> quads;
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> extents;

    public BoostingChipModel(List<BakedQuad> quads, ModelRenderProperties settings) {
        this.quads = quads;
        this.settings = settings;
        this.extents = ItemModelParts.extents(quads);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext transformType, ClientLevel level, ItemOwner owner, int seed) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        var layer = ItemModelRenderHelper.submitQuads(state, settings, transformType, quads);
        layer.setExtents(extents);

        boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if(!leftHand && transformType != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            return;
        var player = Minecraft.getInstance().player;
        if(player == null || player.getUseItemRemainingTicks() <= 0)
            return;

        int modifier = leftHand ? -1 : 1;
        var transform = new Matrix4f();
        transform.translate(0.5f, 0.5f, 0.5f)
                .translate(modifier * -0.4f, 0.0f, -0.25f)
                .rotate(Axis.ZP.rotationDegrees(modifier * 40))
                .rotate(Axis.XP.rotationDegrees(modifier * 10))
                .rotate(Axis.YP.rotationDegrees(modifier * 90))
                .translate(-0.5f, -0.5f, -0.5f);
        layer.setLocalTransform(transform);
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
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            var parts = ItemModelParts.of(context.blockModelBaker(), ITEM_ID);
            return new BoostingChipModel(parts.quads(), parts.properties());
        }
    }
}
