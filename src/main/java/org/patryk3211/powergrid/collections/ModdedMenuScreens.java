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
package org.patryk3211.powergrid.collections;

import com.zurrtum.create.client.AllMenuScreens;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableEditScreen;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableLoadScreen;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableScreen;
import org.patryk3211.powergrid.electricity.gauge.EnergyMeterScreen;
import org.patryk3211.powergrid.electricity.info.customdisplay.CustomDisplayScreen;
import org.patryk3211.powergrid.kinetics.punchcard.PunchCardScreen;

@Environment(EnvType.CLIENT)
public class ModdedMenuScreens {
    public static void register() {
        AllMenuScreens.register(ModdedMenus.PUNCH_CARD, PunchCardScreen::create);
        AllMenuScreens.register(ModdedMenus.CUSTOM_DISPLAY, CustomDisplayScreen::create);
        AllMenuScreens.register(ModdedMenus.ENERGY_METER, EnergyMeterScreen::create);
        AllMenuScreens.register(ModdedMenus.CIRCUIT_DESIGN_TABLE, CircuitDesignTableScreen::create);
        AllMenuScreens.register(ModdedMenus.CIRCUIT_DESIGN_TABLE_EDIT, CircuitDesignTableEditScreen::createTable);
        AllMenuScreens.register(ModdedMenus.CIRCUIT_DESIGN_TABLE_LOAD, CircuitDesignTableLoadScreen::create);
        AllMenuScreens.register(ModdedMenus.CIRCUIT_BOARD_EDIT, CircuitDesignTableEditScreen::createBoard);
    }
}
