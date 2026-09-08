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
package org.patryk3211.powergrid.circuits.editor;

import net.minecraft.world.entity.player.Inventory;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.collections.ModdedMenus;

public class CircuitBoardEditMenu extends CircuitEditMenu<CircuitBoardBlockEntity> {
    public CircuitBoardEditMenu(int id, Inventory inv, CircuitBoardBlockEntity contentHolder) {
        super(ModdedMenus.CIRCUIT_BOARD_EDIT, id, inv, contentHolder);
    }

    @Override
    protected Class<CircuitBoardBlockEntity> clazz() {
        return CircuitBoardBlockEntity.class;
    }
}
