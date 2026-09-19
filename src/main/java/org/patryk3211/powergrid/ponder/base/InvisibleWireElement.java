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
package org.patryk3211.powergrid.ponder.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.electricity.wire.BlockWireEndpoint;
import org.patryk3211.powergrid.electricity.wire.HangingWireEntity;

public class InvisibleWireElement extends WireElement {
    public InvisibleWireElement(BlockPos pos1, int terminal1, BlockPos pos2, int terminal2, float resistance) {
        super(level -> {
            var wire = HangingWireEntity.create(level, new BlockWireEndpoint(pos1, terminal1), new BlockWireEndpoint(pos2, terminal2), ModdedItems.WIRE.asStack(), resistance);
            wire.updateCurveParams();
            return wire;
        });
    }

    @Override
    protected void renderLast(EntityRenderDispatcher dispatcher, ItemModelResolver itemModelResolver, PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector collector, Camera camera, CameraRenderState cameraState, PoseStack ms, float fade, float pt) {
        if(wire == null && isVisible()) {
            wire = wireFactory.apply(world);
        }
    }
}
