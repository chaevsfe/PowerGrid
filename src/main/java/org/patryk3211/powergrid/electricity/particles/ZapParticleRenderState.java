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
package org.patryk3211.powergrid.electricity.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ZapParticleRenderState implements ParticleGroupRenderState {
    private final List<Segment> segments = new ArrayList<>();

    public record Segment(double x1, double y1, double z1, double x2, double y2, double z2, Vec3 cross1, Vec3 cross2, int color) { }

    public void addSegment(double x1, double y1, double z1, double x2, double y2, double z2, Vec3 cross1, Vec3 cross2, int color) {
        segments.add(new Segment(x1, y1, z1, x2, y2, z2, cross1, cross2, color));
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    @Override
    public void clear() {
        segments.clear();
    }

    @Override
    public void submit(SubmitNodeCollector queue, CameraRenderState cameraState) {
        if(segments.isEmpty())
            return;
        queue.submitCustomGeometry(new PoseStack(), ModdedRenderLayers.getColor(), this::renderSegments);
    }

    private void renderSegments(PoseStack.Pose pose, VertexConsumer buffer) {
        for(var segment : segments) {
            ZapParticle.renderSegment(buffer,
                    segment.x1(), segment.y1(), segment.z1(),
                    segment.x2(), segment.y2(), segment.z2(),
                    segment.cross1(), segment.cross2(), segment.color());
        }
    }
}
