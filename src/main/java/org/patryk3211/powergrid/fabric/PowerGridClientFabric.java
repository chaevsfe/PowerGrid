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
package org.patryk3211.powergrid.fabric;

import org.patryk3211.powergrid.registrate.client.RegistrateClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.PowerGridClient;

@Environment(EnvType.CLIENT)
public final class PowerGridClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RegistrateClient.flush(PowerGrid.REGISTRATE);
        PowerGridClient.initClient();
    }
}
