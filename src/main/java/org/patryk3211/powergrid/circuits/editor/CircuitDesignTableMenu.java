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

import com.zurrtum.create.AllItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedMenus;

public class CircuitDesignTableMenu extends AbstractCircuitDesignTableMenu<CircuitDesignTableBlockEntity> {
    public CircuitDesignTableMenu(int id, Inventory inv, CircuitDesignTableBlockEntity contentHolder) {
        super(ModdedMenus.CIRCUIT_DESIGN_TABLE, id, inv, contentHolder);
    }

    @Override
    protected Class<CircuitDesignTableBlockEntity> clazz() {
        return CircuitDesignTableBlockEntity.class;
    }

    @Override
    protected void addSlots() {
        final int X_OFFSET = -11;
        final int Y_OFFSET = 0;

        var beInv = contentHolder.getInventory();
        addSlot(new Slot(beInv, 0, X_OFFSET + 16, Y_OFFSET + 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModdedItems.CIRCUIT_SCHEMATIC.isIn(stack) || ModdedBlocks.CIRCUIT_BOARD.isIn(stack);
            }
        });
        addSlot(new Slot(beInv, 1, X_OFFSET + 117, Y_OFFSET + 21) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(AllItems.EMPTY_SCHEMATIC) || ModdedItems.CIRCUIT_SCHEMATIC.isIn(stack);
            }
        });
        addSlot(new Slot(beInv, 2, X_OFFSET + 145, Y_OFFSET + 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerSlots(-1, 114);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        var clicked = slots.get(slot);
        if(!clicked.hasItem())
            return ItemStack.EMPTY;
        var stack = clicked.getItem();
        if(slot < 3) {
            moveItemStackTo(stack, 3, this.slots.size(), false);
        } else {
            moveItemStackTo(stack, 0, 2, false);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        clearContainer(playerIn, contentHolder.getInventory());
//        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }
}
