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
package org.patryk3211.powergrid.circuits.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ComponentModels {
    public static Identifier rawModelId(Identifier componentId) {
        return Identifier.fromNamespaceAndPath(componentId.getNamespace(), "component/" + componentId.getPath());
    }

    public static Set<Identifier> collectRawIds() {
        var ids = new HashSet<Identifier>();
        for(var component : ComponentRegistry.entries()) {
            for(var id : component.requestedModels()) {
                ids.add(rawModelId(id));
            }
        }
        return ids;
    }

    public static Identifier modelIdOf(PlacedComponent placed) {
        return rawModelId(placed.component.getModelId(placed));
    }
}
