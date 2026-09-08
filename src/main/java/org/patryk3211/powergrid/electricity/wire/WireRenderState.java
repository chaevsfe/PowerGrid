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
package org.patryk3211.powergrid.electricity.wire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class WireRenderState extends EntityRenderState {
    private static final int INITIAL_CAPACITY = 32;

    public boolean skip = true;
    public Identifier texture;
    public boolean simpleModel;

    public int segmentCount;
    private double[] positions = new double[INITIAL_CAPACITY * 6];
    private Vec3[] crosses = new Vec3[INITIAL_CAPACITY * 2];
    private int[] lights = new int[INITIAL_CAPACITY];
    private int[] colors = new int[INITIAL_CAPACITY];
    private float[] shapes = new float[INITIAL_CAPACITY * 4];

    public void clearSegments() {
        segmentCount = 0;
    }

    public void addSegment(double x1, double y1, double z1, double x2, double y2, double z2,
                           Vec3 cross1, Vec3 cross2, int light, int color,
                           float thickness, float thicknessOffset, float uvLength, float lengthOffset) {
        int index = segmentCount;
        if(index == lights.length)
            grow();
        int p = index * 6;
        positions[p] = x1;
        positions[p + 1] = y1;
        positions[p + 2] = z1;
        positions[p + 3] = x2;
        positions[p + 4] = y2;
        positions[p + 5] = z2;
        crosses[index * 2] = cross1;
        crosses[index * 2 + 1] = cross2;
        lights[index] = light;
        colors[index] = color;
        int s = index * 4;
        shapes[s] = thickness;
        shapes[s + 1] = thicknessOffset;
        shapes[s + 2] = uvLength;
        shapes[s + 3] = lengthOffset;
        segmentCount = index + 1;
    }

    private void grow() {
        int capacity = lights.length * 2;
        var newPositions = new double[capacity * 6];
        System.arraycopy(positions, 0, newPositions, 0, positions.length);
        positions = newPositions;
        var newCrosses = new Vec3[capacity * 2];
        System.arraycopy(crosses, 0, newCrosses, 0, crosses.length);
        crosses = newCrosses;
        var newLights = new int[capacity];
        System.arraycopy(lights, 0, newLights, 0, lights.length);
        lights = newLights;
        var newColors = new int[capacity];
        System.arraycopy(colors, 0, newColors, 0, colors.length);
        colors = newColors;
        var newShapes = new float[capacity * 4];
        System.arraycopy(shapes, 0, newShapes, 0, shapes.length);
        shapes = newShapes;
    }

    public void renderSegments(PoseStack.Pose pose, VertexConsumer buffer) {
        for(int i = 0; i < segmentCount; ++i) {
            int p = i * 6;
            int s = i * 4;
            HangingWireRenderer.renderSegment(pose, buffer,
                    positions[p], positions[p + 1], positions[p + 2],
                    positions[p + 3], positions[p + 4], positions[p + 5],
                    crosses[i * 2], crosses[i * 2 + 1], lights[i], colors[i],
                    shapes[s], shapes[s + 1], shapes[s + 2], shapes[s + 3], simpleModel);
        }
    }
}
