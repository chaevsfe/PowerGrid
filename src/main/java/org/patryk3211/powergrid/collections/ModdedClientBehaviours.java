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

import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.patryk3211.powergrid.client.tooltip.ModdedTooltipBehaviours;
import org.patryk3211.powergrid.client.valuebox.ModdedOptionBehaviours;
import org.patryk3211.powergrid.client.valuebox.ModdedScrollBehaviours;
import org.patryk3211.powergrid.client.valuebox.ValueBoxTransforms;
import org.patryk3211.powergrid.utility.Lang;

@Environment(EnvType.CLIENT)
public class ModdedClientBehaviours {
    public static void register() {
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CARBON_PILE.get(), ModdedScrollBehaviours.Trim::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CREATIVE_SOURCE.get(), be -> {
            Component label;
            float multiplier;
            if(be.getBlockState().is(ModdedBlocks.CREATIVE_VOLTAGE_SOURCE.get())) {
                label = Lang.translateDirect("devices.creative.voltage");
                multiplier = 1.0f;
            } else if(be.getBlockState().is(ModdedBlocks.CREATIVE_CURRENT_SOURCE.get())) {
                label = Lang.translateDirect("devices.creative.current");
                multiplier = 0.1f;
            } else {
                label = Component.empty();
                multiplier = 0.0f;
            }
            return new ModdedScrollBehaviours.CreativeSource(label, be, multiplier);
        });
        AllBlockEntityBehaviours.add(ModdedBlockEntities.HV_BREAKER.get(), ModdedScrollBehaviours.HvBreaker::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.FUSE_HOLDER.get(), ModdedScrollBehaviours.Fuse::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.SPARK_GAP.get(), ModdedScrollBehaviours.SparkGap::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CONSTANT_SPEED_MOTOR.get(), ModdedScrollBehaviours.MotorSpeed::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.SOLAR_PANEL_BEARING.get(), ModdedScrollBehaviours.SolarPanelDivisor::new);

        AllBlockEntityBehaviours.add(ModdedBlockEntities.VOLTAGE_METER.get(),
                be -> new ModdedScrollBehaviours.Gauge(Lang.translateDirect("devices.gauge.voltage"), be, new ValueBoxTransforms.Gauge()));
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CURRENT_METER.get(),
                be -> new ModdedScrollBehaviours.Gauge(Lang.translateDirect("devices.gauge.current"), be, new ValueBoxTransforms.Gauge()));
        AllBlockEntityBehaviours.add(ModdedBlockEntities.POWER_METER.get(),
                be -> new ModdedScrollBehaviours.Gauge(Lang.translateDirect("devices.gauge.power"), be, new ValueBoxTransforms.Gauge()));
        AllBlockEntityBehaviours.add(ModdedBlockEntities.PLOTTER.get(),
                be -> new ModdedScrollBehaviours.Gauge(Lang.translateDirect("devices.gauge.voltage"), be, new ValueBoxTransforms.Plotter()));

        AllBlockEntityBehaviours.add(ModdedBlockEntities.RESISTOR.get(),
                be -> new ModdedScrollBehaviours.Resistor(be, new ValueBoxTransforms.Resistor(), 1));
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CREATIVE_RESISTOR.get(),
                be -> new ModdedScrollBehaviours.Resistor(be, new ValueBoxTransforms.Resistor(), 3));
        AllBlockEntityBehaviours.add(ModdedBlockEntities.RHEOSTAT.get(),
                be -> new ModdedScrollBehaviours.Resistor(be, new ValueBoxTransforms.Rheostat(), -2));

        AllBlockEntityBehaviours.add(ModdedBlockEntities.GENERATOR_CLUTCH.get(), ModdedOptionBehaviours.ClutchModeScroll::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.MODULAR_DISPLAY.get(), ModdedOptionBehaviours.ModuleTypeScroll::new);

        AllBlockEntityBehaviours.add(ModdedBlockEntities.ELECTRIC_MOTOR.get(), GeneratingKineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CONSTANT_SPEED_MOTOR.get(), GeneratingKineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.SERVO.get(), GeneratingKineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.GENERATOR_CLUTCH.get(), GeneratingKineticTooltipBehaviour::new);

        AllBlockEntityBehaviours.add(ModdedBlockEntities.HV_SWITCH.get(), KineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.PUNCH_CARD_READER.get(), KineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.SOLAR_PANEL_BEARING.get(), KineticTooltipBehaviour::new);

        AllBlockEntityBehaviours.add(ModdedBlockEntities.PLOTTER.get(), ModdedTooltipBehaviours.Kinetic::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.RHEOSTAT.get(), ModdedTooltipBehaviours.Kinetic::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.VARIAC.get(), ModdedTooltipBehaviours.Kinetic::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.HV_BREAKER.get(), ModdedTooltipBehaviours.Kinetic::new);

        AllBlockEntityBehaviours.add(ModdedBlockEntities.VOLTAGE_METER.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CURRENT_METER.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.POWER_METER.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.HEATING_COIL.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.THERMOMETER.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.SWITCH.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CREATIVE_SOURCE.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CREATIVE_RESISTOR.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.TRANSFORMER_SMALL.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.TRANSFORMER_MEDIUM.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.ELECTRIC_PUMP.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.DEVICE_CONNECTOR.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CARBON_PILE_COIL.get(), ModdedTooltipBehaviours.Device::new);
        AllBlockEntityBehaviours.add(ModdedBlockEntities.CIRCUIT_BOARD.get(), ModdedTooltipBehaviours.Device::new);
    }
}
