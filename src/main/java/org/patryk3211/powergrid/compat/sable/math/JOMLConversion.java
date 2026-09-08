/*
 * Copyright 2026 chaevsfe
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
 *
 * Inert API compatible stub; contains no code from Sable Companion
 * (dev.ryanhcode.sable) - see NOTICE.
 */
package org.patryk3211.powergrid.compat.sable.math;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class JOMLConversion {
    private JOMLConversion() { }

    public static Vec3 toMojang(Vector3dc v) {
        return new Vec3(v.x(), v.y(), v.z());
    }

    public static Vector3d toJOML(Vec3 v) {
        return new Vector3d(v.x, v.y, v.z);
    }
}
