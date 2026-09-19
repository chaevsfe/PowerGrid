/*
 * Copyright 2026 patryk3211
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
import org.patryk3211.powergrid.electricity.sim.special.PentodeWire;

public class PentodeTest extends TestHelper {
    @Test
    void testPentodeConducts() {
        var Net = new Network();

        var V1 = Net.V(50f);
        var V2 = Net.V(0);
        var VScreen = Net.V(50f);
        var GND = Net.V(0);

        var Anode = Net.N();
        var Cathode = Net.N();
        var Grid = Net.N();
        var Screen = Net.N();

        var Tube = new PentodeWire(8, 6_000f, 4_500f, 48, 12, 1.35f, 10f, Cathode, Anode, Grid, Screen);
        Net.network.addWire(Tube);

        final float R = 0.001f;
        Net.W(10f, V1, Anode);
        Net.W(R, V2, Grid);
        Net.W(R, VScreen, Screen);
        Net.W(R, GND, Cathode);

        for (int i = 0; i < 5; ++i)
            Net.calculate();

        Assertions.assertTrue(V1.getCurrent() > 1e-4f, "Anode should conduct with screen biased");
        Assertions.assertTrue(Anode.getVoltage() > 0, "Anode voltage should be positive");
        Assertions.assertTrue(VScreen.getCurrent() > 0, "Screen should draw current");
    }

    @Test
    void testPentodeCutoff() {
        var Net = new Network();

        var V1 = Net.V(50f);
        var V2 = Net.V(-20f);
        var VScreen = Net.V(50f);
        var GND = Net.V(0);

        var Anode = Net.N();
        var Cathode = Net.N();
        var Grid = Net.N();
        var Screen = Net.N();

        var Tube = new PentodeWire(8, 60_000f, 4_500f, 48, 12, 1.35f, 10f, Cathode, Anode, Grid, Screen);
        Net.network.addWire(Tube);

        final float R = 0.001f;
        Net.W(10f, V1, Anode);
        Net.W(R, V2, Grid);
        Net.W(R, VScreen, Screen);
        Net.W(R, GND, Cathode);

        for (int i = 0; i < 5; ++i)
            Net.calculate();

        Assertions.assertEquals(0, V1.getCurrent(), 1e-3f, "Anode current should be near zero when cut off");
    }

    @Test
    void testPentodeNeedsScreen() {
        var Net = new Network();

        var V1 = Net.V(50f);
        var V2 = Net.V(0);
        var VScreen = Net.V(0f);
        var GND = Net.V(0);

        var Anode = Net.N();
        var Cathode = Net.N();
        var Grid = Net.N();
        var Screen = Net.N();

        var Tube = new PentodeWire(8, 6_000f, 4_500f, 48, 12, 1.35f, 10f, Cathode, Anode, Grid, Screen);
        Net.network.addWire(Tube);

        final float R = 0.001f;
        Net.W(10f, V1, Anode);
        Net.W(R, V2, Grid);
        Net.W(R, VScreen, Screen);
        Net.W(R, GND, Cathode);

        for (int i = 0; i < 5; ++i)
            Net.calculate();

        Assertions.assertEquals(0, V1.getCurrent(), 1e-3f, "Anode should not conduct without screen voltage");
    }

    @Test
    void testPentodeReverse() {
        var Net = new Network();

        var V1 = Net.V(-10f);
        var V2 = Net.V(0);
        var VScreen = Net.V(50f);
        var GND = Net.V(0);

        var Anode = Net.N();
        var Cathode = Net.N();
        var Grid = Net.N();
        var Screen = Net.N();

        var Tube = new PentodeWire(8, 6_000f, 4_500f, 48, 12, 1.35f, 10f, Cathode, Anode, Grid, Screen);
        Net.network.addWire(Tube);

        final float R = 0.001f;
        Net.W(10f, V1, Anode);
        Net.W(R, V2, Grid);
        Net.W(R, VScreen, Screen);
        Net.W(R, GND, Cathode);

        Net.calculate();

        Assertions.assertEquals(0, V1.getCurrent(), 1e-3f, "Anode should not conduct at reverse voltage");
    }

    @Test
    void testPentodeSaturation() {
        var Net = new Network();

        var V1 = Net.V(50f);
        var V2 = Net.V(0);
        var VScreen = Net.V(50f);
        var GND = Net.V(0);

        var Anode = Net.N();
        var Cathode = Net.N();
        var Grid = Net.N();
        var Screen = Net.N();

        var Tube = new PentodeWire(8, 6_000f, 4_500f, 48, 12, 1.35f, 0.1f, Cathode, Anode, Grid, Screen);
        Net.network.addWire(Tube);

        final float R = 0.001f;
        Net.W(10f, V1, Anode);
        Net.W(R, V2, Grid);
        Net.W(R, VScreen, Screen);
        Net.W(R, GND, Cathode);

        for (int i = 0; i < 5; ++i)
            Net.calculate();

        Assertions.assertTrue(0.1f >= V1.getCurrent(), "Anode current should not exceed saturation");
        Assertions.assertTrue(49.0f <= Anode.getVoltage(), "Anode voltage should stay high under saturation");
    }

    @Test
    void testScreenDerivativeMatchesFiniteDifference() throws Exception {
        var tube = new PentodeWire(8, 6_000f, 4_500f, 48, 12, 1.35f, 10f, null, null, null, null);
        var method = PentodeWire.class.getDeclaredMethod("evaluatePlate", double.class, double.class, double.class);
        method.setAccessible(true);

        final double vAnode = 100;
        final double vScreen = 100;
        final double h = 1e-4;

        for (double vGrid : new double[]{0, -2, -10, 5}) {
            var state = method.invoke(tube, vAnode, vGrid, vScreen);
            var dScreen = state.getClass().getDeclaredMethod("dCurrent_dScreen");
            var current = state.getClass().getDeclaredMethod("current");
            dScreen.setAccessible(true);
            current.setAccessible(true);
            var analytic = (double) dScreen.invoke(state);

            var up = method.invoke(tube, vAnode, vGrid, vScreen + h);
            var down = method.invoke(tube, vAnode, vGrid, vScreen - h);
            var iUp = (double) current.invoke(up);
            var iDown = (double) current.invoke(down);
            var numeric = (iUp - iDown) / (2 * h);

            var scale = Math.max(1e-9, Math.abs(numeric));
            Assertions.assertTrue(Math.abs(analytic - numeric) / scale < 1e-3,
                    "screen derivative at vGrid=" + vGrid + ": analytic " + analytic + " vs numeric " + numeric);
        }
    }
}
