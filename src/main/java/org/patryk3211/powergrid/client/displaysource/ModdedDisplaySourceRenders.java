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
package org.patryk3211.powergrid.client.displaysource;

import com.zurrtum.create.client.AllDisplaySourceRenders;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.patryk3211.powergrid.collections.ModdedDisplaySources;

@Environment(EnvType.CLIENT)
public class ModdedDisplaySourceRenders {
    public static void register() {
        AllDisplaySourceRenders.register(ModdedDisplaySources.ELECTRIC_GAUGE.get(), ElectricGaugeDisplaySourceRender::new);
        AllDisplaySourceRenders.register(ModdedDisplaySources.BATTERY.get(), BatteryDisplaySourceRender::new);
        AllDisplaySourceRenders.register(ModdedDisplaySources.THERMOMETER.get(), ThermometerDisplaySourceRender::new);
        AllDisplaySourceRenders.register(ModdedDisplaySources.CLUTCH.get(), ClutchDisplaySourceRender::new);
    }
}
