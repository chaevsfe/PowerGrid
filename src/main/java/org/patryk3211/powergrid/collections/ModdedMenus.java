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

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.editor.CircuitBoardEditMenu;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlockEntity;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableEditMenu;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableLoadMenu;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableMenu;
import org.patryk3211.powergrid.electricity.gauge.EnergyMeterBlockEntity;
import org.patryk3211.powergrid.electricity.gauge.EnergyMeterMenu;
import org.patryk3211.powergrid.electricity.info.customdisplay.CustomDisplayMenu;
import org.patryk3211.powergrid.kinetics.punchcard.PunchCardMenu;

public class ModdedMenus {
    public static final MenuType<CircuitDesignTableBlockEntity> CIRCUIT_DESIGN_TABLE = register("circuit_design_table", CircuitDesignTableMenu::new);
    public static final MenuType<CircuitDesignTableBlockEntity> CIRCUIT_DESIGN_TABLE_EDIT = register("circuit_design_table_edit", CircuitDesignTableEditMenu::new);
    public static final MenuType<CircuitDesignTableBlockEntity> CIRCUIT_DESIGN_TABLE_LOAD = register("circuit_design_table_load", CircuitDesignTableLoadMenu::new);
    public static final MenuType<CircuitBoardBlockEntity> CIRCUIT_BOARD_EDIT = register("circuit_board_edit", CircuitBoardEditMenu::new);
    public static final MenuType<ItemStack> PUNCH_CARD = register("punch_card", PunchCardMenu::new);
    public static final MenuType<SmartBlockEntity> CUSTOM_DISPLAY = register("custom_display", CustomDisplayMenu::new);
    public static final MenuType<EnergyMeterBlockEntity> ENERGY_METER = register("energy_meter", EnergyMeterMenu::new);

    private static <T> MenuType<T> register(String name, MenuType<T> type) {
        return Registry.register(CreateRegistries.MENU_TYPE, PowerGrid.asResource(name), type);
    }

    public static void register() {
    }
}
