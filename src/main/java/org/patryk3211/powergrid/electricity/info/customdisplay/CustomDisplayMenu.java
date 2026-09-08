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
package org.patryk3211.powergrid.electricity.info.customdisplay;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.collections.ModdedMenus;
import org.patryk3211.powergrid.utility.Unit;

public class CustomDisplayMenu extends MenuBase<SmartBlockEntity> {
    public String expression;
    public Unit unit;
    public String unitStr;
    public boolean enablePrefixes;

    public CustomDisplayMenu(int id, Inventory inv, SmartBlockEntity contentHolder) {
        super(ModdedMenus.CUSTOM_DISPLAY, id, inv, contentHolder);
    }

    @Override
    protected void initAndReadInventory(SmartBlockEntity be) {
        var cdb = be.getBehaviour(CustomDisplayBehaviour.TYPE);
        if(cdb == null)
            return;
        expression = cdb.equationStr;
        unit = cdb.unit;
        unitStr = cdb.unitStr;
        enablePrefixes = cdb.prefixes;
    }

    @Override
    protected void addSlots() {
    }

    @Override
    protected void saveData(SmartBlockEntity contentHolder) {
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
