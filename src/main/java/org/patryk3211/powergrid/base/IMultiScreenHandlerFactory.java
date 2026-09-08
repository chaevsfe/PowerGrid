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
package org.patryk3211.powergrid.base;

import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IMultiScreenHandlerFactory extends MenuProvider {
    @Override
    @Nullable
    default MenuBase<?> createMenu(int syncId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        return createMenu(syncId, playerInventory, player, extraData, 0);
    }

    @Nullable
    MenuBase<?> createMenu(int syncId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData, int menuIndex);

    static void openScreen(ServerPlayer player, IMultiScreenHandlerFactory factory, int menuIndex) {
        MenuProvider.openHandledScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return factory.getDisplayName();
            }

            @Override
            @Nullable
            public MenuBase<?> createMenu(int syncId, Inventory playerInventory, Player menuPlayer, RegistryFriendlyByteBuf extraData) {
                return factory.createMenu(syncId, playerInventory, menuPlayer, extraData, menuIndex);
            }
        });
    }
}
