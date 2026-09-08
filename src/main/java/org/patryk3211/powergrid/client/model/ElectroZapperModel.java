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
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.PowerGridClient;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ElectroZapperModel implements ItemModel {
    public static final Identifier ID = PowerGrid.asResource("model/electrozapper");
    public static final Identifier ITEM_ID = PowerGrid.asResource("item/electrozapper");
    public static final Identifier COG_ID = PowerGrid.asResource("item/electrozapper/cog");

    private static final float COG_OFFSET = -2.5f / 16;

    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> itemExtents;
    private final List<BakedQuad> cogQuads;
    private final Supplier<Vector3fc[]> cogExtents;

    public ElectroZapperModel(List<BakedQuad> itemQuads, ModelRenderProperties settings, List<BakedQuad> cogQuads) {
        this.itemQuads = itemQuads;
        this.settings = settings;
        this.itemExtents = ItemModelParts.extents(itemQuads);
        this.cogQuads = cogQuads;
        this.cogExtents = ItemModelParts.extents(cogQuads);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext transformType, ClientLevel level, ItemOwner owner, int seed) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        var itemLayer = ItemModelRenderHelper.submitQuads(state, settings, transformType, itemQuads);
        itemLayer.setExtents(itemExtents);
        var cogLayer = ItemModelRenderHelper.submitQuads(state, settings, transformType, cogQuads);
        cogLayer.setExtents(cogExtents);

        float angle = AnimationTickHolder.getRenderTime() / 10 * -25;
        var player = Minecraft.getInstance().player;
        if(player != null) {
            boolean mainHand = player.getMainHandItem() == stack;
            boolean offHand = player.getOffhandItem() == stack;
            boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
            if(mainHand || offHand) {
                float speed = PowerGridClient.ELECTRO_ZAPPER_RENDER_HANDLER.getAnimation(mainHand ^ leftHanded,
                        AnimationTickHolder.getPartialTicks());
                angle += 360 * Mth.clamp(speed * 5, 0, 1);
            }
        }

        cogLayer.setLocalTransform(new Matrix4f().rotateAround(Axis.ZP.rotationDegrees(angle % 360),
                0.5f, 0.5f + COG_OFFSET, 0.5f));
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
            resolver.markDependency(COG_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            var baker = context.blockModelBaker();
            var parts = ItemModelParts.of(baker, ITEM_ID);
            return new ElectroZapperModel(parts.quads(), parts.properties(), ItemModelParts.quads(baker, COG_ID));
        }
    }
}
