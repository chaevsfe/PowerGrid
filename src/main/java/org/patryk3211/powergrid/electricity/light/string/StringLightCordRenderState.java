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
package org.patryk3211.powergrid.electricity.light.string;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.patryk3211.powergrid.electricity.wire.powercord.CordRenderState;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StringLightCordRenderState extends CordRenderState {
    public float power;
    public boolean anyGlow;
    public final List<Bulb> bulbs = new ArrayList<>();

    public record Bulb(double x, double y, double z, int light, int color, int glowColor) { }

    public void renderBulbs(PoseStack.Pose pose, VertexConsumer buffer) {
        for(var bulb : bulbs)
            StringLightCordRenderer.bulb(pose, buffer, bulb.x(), bulb.y(), bulb.z(), bulb.light(), bulb.color());
    }

    public void renderGlows(PoseStack.Pose pose, VertexConsumer buffer) {
        for(var bulb : bulbs) {
            if(bulb.glowColor() != 0)
                StringLightCordRenderer.glow(pose, buffer, bulb.x(), bulb.y(), bulb.z(), bulb.glowColor());
        }
    }
}
