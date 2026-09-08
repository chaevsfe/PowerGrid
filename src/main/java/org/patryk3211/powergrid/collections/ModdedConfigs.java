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

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.catnip.config.Builder;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.config.*;
import org.patryk3211.powergrid.utility.Env;

public class ModdedConfigs {
    private static CServer server;
    private static CCommon common;
    private static CClient client;

    public static CServer server() {
        return server;
    }

    public static CCommon common() {
        return common;
    }

    public static CClient client() {
        return client;
    }

    public static void register() {
        server = Builder.create(CServer::new, PowerGrid.MOD_ID, "server", true);
        common = Builder.create(CCommon::new, PowerGrid.MOD_ID, "common", true);
        if (Env.CLIENT.isCurrent())
            client = Builder.create(CClient::new, PowerGrid.MOD_ID, "client", true);

        if (!server.isUpToDate())
            PowerGrid.LOGGER.warn("Detected outdated configs, consider resetting your server configs if you experience issues.");

        CStress stress = server().kinetics.stressValues;
        BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);

        ResistanceValues.register(server.electricity.resistance);
        ThermalValues.register(server.electricity.thermal);
    }

    public static boolean logsEnabled() {
        return common != null && common.lotsOfLogs.get();
    }
}
