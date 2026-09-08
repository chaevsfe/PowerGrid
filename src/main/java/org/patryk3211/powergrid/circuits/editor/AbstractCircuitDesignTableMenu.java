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

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractCircuitDesignTableMenu<T extends SmartBlockEntity> extends MenuBase<T> {
    protected AbstractCircuitDesignTableMenu(MenuType<T> type, int id, Inventory inv, T contentHolder) {
        super(type, id, inv, contentHolder);
    }

    protected abstract Class<T> clazz();

    @Override
    protected void addPlayerSlots(int xOffset, int yOffset) {
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                addSlot(new Slot(player.getInventory(), col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
            }
        }

        for(int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            addSlot(new Slot(player.getInventory(), hotbarSlot, xOffset + hotbarSlot * 18, yOffset + 18 * 3 + 4));
        }
    }

    @Override
    protected void initAndReadInventory(T contentHolder) {

    }

    @Override
    protected void saveData(T contentHolder) {

    }
}
