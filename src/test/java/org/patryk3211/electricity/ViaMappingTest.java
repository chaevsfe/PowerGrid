/*
 * Copyright 2026 patryk3211
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
package org.patryk3211.electricity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.patryk3211.powergrid.circuits.circuitboard.ViaMapping;

import java.util.Arrays;

public class ViaMappingTest {
    private static final int SOUTH = 0;
    private static final int WEST = 1;
    private static final int NORTH = 2;
    private static final int EAST = 3;

    private static int[] cosSin(int degrees) {
        return switch (Math.floorMod(degrees / 90, 4)) {
            case 0 -> new int[]{1, 0};
            case 1 -> new int[]{0, 1};
            case 2 -> new int[]{-1, 0};
            default -> new int[]{0, -1};
        };
    }

    private static int[] horizontalPosition(int x, int y, int facing, int rotation) {
        int px = 2 * x - 15;
        int py = -12;
        int pz = 2 * y - 15;
        var tilt = cosSin(rotation * 90);
        int rz = pz * tilt[0] + py * tilt[1];
        var turn = cosSin(180 - 90 * facing - (rotation == 2 ? 180 : 0));
        return new int[]{px * turn[0] + rz * turn[1], rz * turn[0] - px * turn[1]};
    }

    @Test
    void testAuditedAlignments() {
        Assertions.assertArrayEquals(new int[]{13, 5}, ViaMapping.behind(2, 5, NORTH, NORTH, true));
        Assertions.assertArrayEquals(new int[]{2, 10}, ViaMapping.behind(2, 5, NORTH, SOUTH, true));
        Assertions.assertArrayEquals(new int[]{10, 13}, ViaMapping.behind(2, 5, NORTH, EAST, true));
        Assertions.assertArrayEquals(new int[]{5, 2}, ViaMapping.behind(2, 5, NORTH, WEST, true));

        Assertions.assertArrayEquals(new int[]{2, 5}, ViaMapping.behind(13, 5, NORTH, NORTH, false));
        Assertions.assertArrayEquals(new int[]{2, 5}, ViaMapping.behind(2, 10, SOUTH, NORTH, false));
        Assertions.assertArrayEquals(new int[]{2, 5}, ViaMapping.behind(10, 13, EAST, NORTH, false));
        Assertions.assertArrayEquals(new int[]{2, 5}, ViaMapping.behind(5, 2, WEST, NORTH, false));
        Assertions.assertFalse(Arrays.equals(new int[]{2, 5}, ViaMapping.behind(13, 10, EAST, NORTH, false)));
    }

    @Test
    void testBackToBackViasLineUpFromBothBoards() {
        for (int floor = 0; floor < 4; floor++) {
            for (int ceiling = 0; ceiling < 4; ceiling++) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        var down = ViaMapping.behind(x, y, floor, ceiling, true);
                        Assertions.assertArrayEquals(horizontalPosition(x, y, floor, 0), horizontalPosition(down[0], down[1], ceiling, 2),
                                "floor " + floor + " to ceiling " + ceiling + " at " + x + "," + y);
                        var up = ViaMapping.behind(x, y, ceiling, floor, false);
                        Assertions.assertArrayEquals(horizontalPosition(x, y, ceiling, 2), horizontalPosition(up[0], up[1], floor, 0),
                                "ceiling " + ceiling + " to floor " + floor + " at " + x + "," + y);
                    }
                }
            }
        }
    }

    @Test
    void testRoundTripReturnsToTheSameVia() {
        for (int floor = 0; floor < 4; floor++) {
            for (int ceiling = 0; ceiling < 4; ceiling++) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        var down = ViaMapping.behind(x, y, floor, ceiling, true);
                        Assertions.assertArrayEquals(new int[]{x, y}, ViaMapping.behind(down[0], down[1], ceiling, floor, false));
                    }
                }
            }
        }
    }

    @Test
    void testMappingStaysOnTheBoard() {
        for (int turns = 0; turns < 4; turns++) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    var mapped = ViaMapping.across(x, y, turns);
                    Assertions.assertTrue(mapped[0] >= 0 && mapped[0] < 16, "x in range");
                    Assertions.assertTrue(mapped[1] >= 0 && mapped[1] < 16, "y in range");
                }
            }
        }
    }
}
