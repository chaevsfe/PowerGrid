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

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.network.C2SPacket;
import org.patryk3211.powergrid.network.S2CPacket;

public class ModdedPackets {
    public static void sendToServer(C2SPacket packet) {
        ModPackets.PACKETS.send(packet);
    }

    public static void sendToClient(S2CPacket packet, ServerPlayer player) {
        ModPackets.PACKETS.sendTo(player, packet);
    }

    public static void sendToClientsTracking(S2CPacket packet, Entity e) {
        for (ServerPlayer player : PlayerLookup.tracking(e)) {
            ModPackets.PACKETS.sendTo(player, packet);
        }
    }

    public static void sendToClientsAround(S2CPacket packet, ServerLevel world, Vec3 position, double radius) {
        for (ServerPlayer player : PlayerLookup.around(world, position, radius)) {
            ModPackets.PACKETS.sendTo(player, packet);
        }
    }
}
