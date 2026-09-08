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
package org.patryk3211.powergrid.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.ConfigValue;
import net.minecraft.commands.CommandSourceStack;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.config.CServer;
import org.patryk3211.powergrid.config.ResettableValues;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.network.PlayerSelection;
import org.patryk3211.powergrid.collections.ModPackets;
import org.patryk3211.powergrid.network.packets.ServerConfigS2CPacket;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.mixin.ConfigBaseAccessor;
import org.patryk3211.powergrid.utility.Lang;

import static net.minecraft.commands.Commands.literal;
import net.minecraft.server.permissions.Permissions;

public class ConfigCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> reset() {
        return literal("reset_configs")
                .requires(cs -> cs.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(ctx -> {
                    resetConfig(ModdedConfigs.server());
                    serverConfigReloaded();
                    ctx.getSource().sendSystemMessage(Lang.translateDirect("message.config_reset_ok"));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static ArgumentBuilder<CommandSourceStack, ?> ignore() {
        return literal("ignore_configs")
                .requires(cs -> cs.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .executes(ctx -> {
                    ModdedConfigs.server().version.set(CServer.CONFIG_VERSION);
                    valueOf(ModdedConfigs.server().version).save();
                    ctx.getSource().sendSystemMessage(Lang.translateDirect("message.config_ignore_ok"));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static void serverConfigReloaded() {
        PowerGrid.LOGGER.warn("Server config reloaded, this can cause unexpected behaviour if done during gameplay!");
        GlobalElectricNetworks.configsReloaded();
        ModPackets.PACKETS.sendTo(PlayerSelection.all(), new ServerConfigS2CPacket());
    }

    private static void resetConfig(ConfigBase config) {
        var accessor = (ConfigBaseAccessor) config;
        for (ConfigBase.CValue<?> value : accessor.getAllValues()) {
            resetValue(valueOf(value));
        }
        if (config instanceof ResettableValues resettable)
            resettable.resetToDefaults();
        for (ConfigBase child : accessor.getChildren())
            resetConfig(child);
    }

    private static ConfigValue<?> valueOf(ConfigBase.CValue<?> cValue) {
        return ((ConfigBaseAccessor.CValueAccessor<?>) cValue).getValue();
    }

    private static <T> void resetValue(ConfigValue<T> value) {
        value.set(value.getDefault());
        value.save();
    }
}
