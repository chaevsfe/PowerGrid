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
package org.patryk3211.powergrid.circuits.circuitboard;

public final class ViaMapping {
    private ViaMapping() {
    }

    public static int[] behind(int x, int y, int facing, int neighborFacing, boolean fromFloorBoard) {
        return across(x, y, fromFloorBoard ? neighborFacing - facing : facing - neighborFacing);
    }

    public static int[] across(int x, int y, int quarterTurns) {
        return switch (((quarterTurns % 4) + 4) % 4) {
            case 0 -> new int[]{15 - x, y};
            case 1 -> new int[]{15 - y, 15 - x};
            case 2 -> new int[]{x, 15 - y};
            default -> new int[]{y, x};
        };
    }
}
