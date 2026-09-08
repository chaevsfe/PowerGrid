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

import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public record ItemModelParts(List<BakedQuad> quads, ModelRenderProperties properties) {
    public static ItemModelParts of(ModelBaker baker, Identifier model) {
        var resolved = baker.getModel(model);
        var slots = resolved.getTopTextureSlots();
        var quads = resolved.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY).getAll();
        return new ItemModelParts(quads, ModelRenderProperties.fromResolvedModel(baker, resolved, slots));
    }

    public static List<BakedQuad> quads(ModelBaker baker, Identifier model) {
        var resolved = baker.getModel(model);
        return resolved.bakeTopGeometry(resolved.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
    }

    public static Supplier<Vector3fc[]> extents(List<BakedQuad> quads) {
        return Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads));
    }
}
