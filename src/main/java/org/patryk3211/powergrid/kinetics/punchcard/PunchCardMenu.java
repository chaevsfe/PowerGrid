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
package org.patryk3211.powergrid.kinetics.punchcard;

import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.patryk3211.powergrid.collections.ModdedMenus;

public class PunchCardMenu extends GhostItemMenu<ItemStack> {
    public byte[] data;

    public PunchCardMenu(int id, Inventory inv, ItemStack contentHolder) {
        super(ModdedMenus.PUNCH_CARD, id, inv, contentHolder);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(0);
    }

    @Override
    protected void init(Inventory inv, ItemStack stack) {
        super.init(inv, stack);
        if(data == null)
            data = new byte[16];
        if(!stack.has(DataComponents.CUSTOM_DATA))
            return;
        var stored = stack.get(DataComponents.CUSTOM_DATA).copyTag().getByteArray("Data");
        if(stored.isEmpty())
            return;
        var bytes = stored.get();
        System.arraycopy(bytes, 0, data, 0, Math.min(data.length, bytes.length));
    }

    public boolean isLocked() {
        if(!contentHolder.has(DataComponents.CUSTOM_DATA))
            return false;
        return contentHolder.get(DataComponents.CUSTOM_DATA).copyTag().getBooleanOr("Locked", false);
    }

    public String getAuthor() {
        if(!contentHolder.has(DataComponents.CUSTOM_DATA))
            return "";
        return contentHolder.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("Author", "");
    }

    @Override
    protected boolean allowRepeats() {
        return false;
    }

    @Override
    protected void addSlots() {
    }

    @Override
    protected void saveData(ItemStack contentHolder) {
    }

    public void lock() {
        var tag = contentHolder.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("Locked", true);
        tag.putString("Author", player.getDisplayName().getString());
        contentHolder.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
