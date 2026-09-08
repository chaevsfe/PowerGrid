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

import com.zurrtum.create.client.AllModels;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.patryk3211.powergrid.client.model.BoostingChipModel;
import org.patryk3211.powergrid.client.model.DrillModel;
import org.patryk3211.powergrid.client.model.ElectroZapperModel;
import org.patryk3211.powergrid.client.model.MultimeterModel;
import org.patryk3211.powergrid.client.model.ThermometerModel;

@Environment(EnvType.CLIENT)
public class ModdedItemModels {
    public static void register() {
        AllModels.register(BoostingChipModel.ID, BoostingChipModel.Unbaked.CODEC);
        AllModels.register(ElectroZapperModel.ID, ElectroZapperModel.Unbaked.CODEC);
        AllModels.register(DrillModel.ID, DrillModel.Unbaked.CODEC);
        AllModels.register(ThermometerModel.ID, ThermometerModel.Unbaked.CODEC);
        AllModels.register(MultimeterModel.ID, MultimeterModel.Unbaked.CODEC);
    }
}
