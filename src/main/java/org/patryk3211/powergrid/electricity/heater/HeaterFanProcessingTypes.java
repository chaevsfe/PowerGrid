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
package org.patryk3211.powergrid.electricity.heater;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.Registry;
import org.patryk3211.powergrid.PowerGrid;

public class HeaterFanProcessingTypes {
    public static final FanProcessingType HEATER_BLASTING = register("heater_blasting", new HeaterBlastingType());
    public static final FanProcessingType HEATER_SMOKING = register("heater_smoking", new HeaterSmokingType());

    private static FanProcessingType register(String name, FanProcessingType type) {
        return Registry.register(CreateRegistries.FAN_PROCESSING_TYPE, PowerGrid.asResource(name), type);
    }

    public static void register() {
    }
}
