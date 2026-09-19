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

public class ViaMappingTest {
    // quarter turns from the source board's facing to the neighbour's: 0 same, 1 clockwise, 2 opposite, 3 counter-clockwise

    @Test
    void testAuditedAlignments() {
        Assertions.assertArrayEquals(new int[]{13, 5}, ViaMapping.across(2, 5, 0));
        Assertions.assertArrayEquals(new int[]{2, 10}, ViaMapping.across(2, 5, 2));
        Assertions.assertArrayEquals(new int[]{10, 13}, ViaMapping.across(2, 5, 1));
        Assertions.assertArrayEquals(new int[]{5, 2}, ViaMapping.across(2, 5, 3));
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
