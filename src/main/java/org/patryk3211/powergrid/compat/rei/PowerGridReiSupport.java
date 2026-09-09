/*
 * Copyright 2026 chaevsfe
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
package org.patryk3211.powergrid.compat.rei;

import net.fabricmc.loader.api.FabricLoader;

public final class PowerGridReiSupport {
    public static final String VIEWER_MOD_ID = "createreiviewer";
    public static final String REI_MOD_ID = "roughlyenoughitems";

    private PowerGridReiSupport() {
    }

    public static boolean available() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded(VIEWER_MOD_ID) && loader.isModLoaded(REI_MOD_ID);
    }
}
