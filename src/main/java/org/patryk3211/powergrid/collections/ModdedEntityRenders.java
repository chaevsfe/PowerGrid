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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.patryk3211.powergrid.electricity.light.string.StringLightCordRenderer;
import org.patryk3211.powergrid.electricity.wire.BlockWireRenderer;
import org.patryk3211.powergrid.electricity.wire.HangingWireRenderer;
import org.patryk3211.powergrid.electricity.wire.powercord.CordRenderer;
import org.patryk3211.powergrid.equipment.zapper.ZapProjectileRenderer;

@Environment(EnvType.CLIENT)
public class ModdedEntityRenders {
    public static void register() {
        EntityRendererRegistry.register(ModdedEntities.HANGING_WIRE.get(), HangingWireRenderer::new);
        EntityRendererRegistry.register(ModdedEntities.BLOCK_WIRE.get(), BlockWireRenderer::new);
        EntityRendererRegistry.register(ModdedEntities.CORD_ENTITY.get(), CordRenderer::new);
        EntityRendererRegistry.register(ModdedEntities.STRING_LIGHT_CORD.get(), StringLightCordRenderer::new);
        EntityRendererRegistry.register(ModdedEntities.ZAP_PROJECTILE.get(), ZapProjectileRenderer::new);
    }
}
