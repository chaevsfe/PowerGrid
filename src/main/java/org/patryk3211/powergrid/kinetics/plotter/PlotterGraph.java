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
package org.patryk3211.powergrid.kinetics.plotter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class PlotterGraph {
    public static final float TIME_SPAN = 9.5f / 16;
    public static final float VOLTAGE_SPAN = 11.5f / 16;
    public static final Vec3 GRAPH_ORIGIN = new Vec3(8.0 / 16.0, 16.2 / 16.0, 13.0 / 16.0);

    public static final Quaternionf GRAPH_ROTATION = new Quaternionf()
            .rotateX(-112.5f / 180f * (float) Math.PI);

    public static Vec3 graphPoint(float t, float v, BlockPos pos, Direction facing) {
        var dest = new Vector3d();
        GRAPH_ROTATION.transform(v * VOLTAGE_SPAN * 0.5f, t * TIME_SPAN, 0, dest);
        var out = new Vec3(
                dest.x + GRAPH_ORIGIN.x,
                dest.y + GRAPH_ORIGIN.y,
                dest.z + GRAPH_ORIGIN.z);
        out = switch(facing) {
            case NORTH -> out;
            case SOUTH -> new Vec3(1 - out.x, out.y, 1 - out.z);
            case EAST -> new Vec3(1 - out.z, out.y, out.x);
            case WEST -> new Vec3(out.z, out.y, 1 - out.x);
            default -> Vec3.ZERO;
        };
        return out.add(pos.getX(), pos.getY(), pos.getZ());
    }
}
