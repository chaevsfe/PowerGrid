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
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;

import java.util.function.Function;

public class WireElement extends AnimatedSceneElementBase {
    protected BaseWireEntity wire;
    protected Function<Level, BaseWireEntity> wireFactory;

    public WireElement(Function<Level, BaseWireEntity> factory) {
        this.wireFactory = factory;
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        if(wire != null) {
            wire.discard();
            wire = null;
        }
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if(wire != null) {
            wire.tick();
        }
    }

    @Override
    protected void renderLast(EntityRenderDispatcher dispatcher, ItemModelResolver itemModelResolver, PonderLevel world, SubmitNodeCollector collector, CameraRenderState cameraState, PoseStack ms, float fade, float pt) {
        if(wire == null && isVisible()) {
            wire = wireFactory.apply(world);
        }
        if(wire == null)
            return;

        ms.pushPose();
        ms.translate(
                Mth.lerp(pt, wire.xo, wire.getX()),
                Mth.lerp(pt, wire.yo, wire.getY()),
                Mth.lerp(pt, wire.zo, wire.getZ())
        );

        var state = dispatcher.extractEntity(wire, pt);
        state.shadowPieces.clear();
        dispatcher.submit(state, cameraState, 0, 0, 0, ms, collector);
        ms.popPose();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(!visible) {
            if(wire != null) {
                wire.discard();
                wire = null;
            }
        }
    }
}
