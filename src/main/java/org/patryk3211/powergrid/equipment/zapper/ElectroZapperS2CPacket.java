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
package org.patryk3211.powergrid.equipment.zapper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.PowerGridClient;
import org.patryk3211.powergrid.network.S2CPacket;

public class ElectroZapperS2CPacket implements S2CPacket {
    private final Vec3 location;
    private final InteractionHand hand;
    private final boolean self;

    public ElectroZapperS2CPacket(Vec3 location, InteractionHand hand, boolean self) {
        this.location = location;
        this.hand = hand;
        this.self = self;
    }

    public ElectroZapperS2CPacket(FriendlyByteBuf buf) {
        this(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readEnum(InteractionHand.class),
                buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(location.x);
        buf.writeDouble(location.y);
        buf.writeDouble(location.z);
        buf.writeEnum(hand);
        buf.writeBoolean(self);
    }

    @Override
    public void handle(Minecraft mc) {
        if (mc.player == null || mc.player.position().distanceTo(location) > 100)
            return;

        var handler = PowerGridClient.ELECTRO_ZAPPER_RENDER_HANDLER;
        if (self)
            handler.shoot(hand, location);
        else
            handler.playSound(hand, location);
    }
}
