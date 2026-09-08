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

import org.patryk3211.powergrid.registrate.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;
import org.patryk3211.powergrid.electricity.wire.BlockWireEntity;
import org.patryk3211.powergrid.electricity.wire.HangingWireEntity;
import org.patryk3211.powergrid.electricity.wire.powercord.CordEntity;
import org.patryk3211.powergrid.electricity.light.string.StringLightCordEntity;
import org.patryk3211.powergrid.equipment.zapper.ZapProjectileEntity;
import org.patryk3211.powergrid.utility.EntityProperties;

import static org.patryk3211.powergrid.PowerGrid.REGISTRATE;

public class ModdedEntities {
    public static final EntityEntry<HangingWireEntity> HANGING_WIRE =
            REGISTRATE.entity("hanging_wire", HangingWireEntity::new, MobCategory.MISC)
                    .register();

    public static final EntityEntry<BlockWireEntity> BLOCK_WIRE =
            REGISTRATE.entity("block_wire", BlockWireEntity::new, MobCategory.MISC)
                    .register();

    public static final EntityEntry<CordEntity> CORD_ENTITY =
            REGISTRATE.entity("cord", CordEntity::new, MobCategory.MISC)
                    .register();

    public static final EntityEntry<StringLightCordEntity> STRING_LIGHT_CORD =
            REGISTRATE.entity("string_light_cord", StringLightCordEntity::new, MobCategory.MISC)
                    .register();

    public static final EntityEntry<ZapProjectileEntity> ZAP_PROJECTILE =
            REGISTRATE.entity("zap_projectile", ZapProjectileEntity::new, MobCategory.MISC)
                    .transform(EntityProperties.apply(b -> b
                            .dimensions(0.25f, 0.25f)
                            .trackRangeChunks(4)
                            .trackedUpdateRate(20)
                            .forceTrackedVelocityUpdates(true)
                    ))
                    .register();

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }
}
