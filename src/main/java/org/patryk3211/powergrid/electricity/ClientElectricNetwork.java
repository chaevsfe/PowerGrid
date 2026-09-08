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
package org.patryk3211.powergrid.electricity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.patryk3211.powergrid.electricity.wire.JunctionWireEndpoint;
import org.patryk3211.powergrid.utility.ClientSideAccess;

@Environment(EnvType.CLIENT)
public class ClientElectricNetwork extends GlobalElectricNetworks {
    public static ClientWorldNetworks getWorldNetworks() {
        return (ClientWorldNetworks) getWorldNetworks(ClientSideAccess.world());
    }

    private static ClientLevel lastLevel;

    public static void levelChanged(Minecraft minecraftClient, ClientLevel world) {
        if(lastLevel != null && lastLevel != world)
            unloadWorld(minecraftClient, lastLevel);
        lastLevel = world;
    }

    public static void unloadWorld(Minecraft minecraftClient, ClientLevel world) {
        if(world == null)
            return;
        if(lastLevel == world)
            lastLevel = null;
        worldNetworks.remove(world);
        JunctionWireEndpoint.unloadWorld(world);
    }
}
