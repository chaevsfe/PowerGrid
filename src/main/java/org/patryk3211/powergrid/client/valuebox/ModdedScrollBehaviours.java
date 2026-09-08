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
package org.patryk3211.powergrid.client.valuebox;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.patryk3211.powergrid.electricity.gauge.GaugeValueBehaviour;
import org.patryk3211.powergrid.electricity.resistor.ResistorValueBehaviour;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingBlockScrollBehaviour;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.NumberFormats;
import org.patryk3211.powergrid.utility.Unit;

@Environment(EnvType.CLIENT)
public class ModdedScrollBehaviours {
    public static class Trim extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        public Trim(SmartBlockEntity be) {
            super(Lang.translateDirect("gui.carbon_pile.trim"), be, new ValueBoxTransforms.CarbonPile());
            withFormatter(value -> Lang.numberConstant(value * 0.5f).string());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(Component.literal("+"), Component.literal("-"));
            return new ValueSettingsBoard(label, 100, 20, rows, new ValueSettingsFormatter(this::formatSettings));
        }

        private MutableComponent formatSettings(ValueSettings settings) {
            return Lang.numberConstant(settings.value() * 0.5f)
                    .add(Component.literal("%"))
                    .component();
        }
    }

    public static class CreativeSource extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        private final float multiplier;

        public CreativeSource(Component label, SmartBlockEntity be, float multiplier) {
            super(label, be, new ValueBoxTransforms.CreativeSource());
            this.multiplier = multiplier;
            withFormatter(i -> String.format("%.1f", Math.abs(processValue(i))));
        }

        private float processValue(int i) {
            if(i > 250) {
                i = (i - 250) * 100;
            } else if(i < -250) {
                i = (i + 250) * 100;
            }
            return i * multiplier;
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(
                    Component.literal("× +100"),
                    Component.literal("+"),
                    Component.literal("-"),
                    Component.literal("× -100")
            );
            return new ValueSettingsBoard(label, 250, 20, rows, new ValueSettingsFormatter(this::formatSettings));
        }

        private MutableComponent formatSettings(ValueSettings settings) {
            return Lang.number(Math.max(0, Math.abs(settings.value() * multiplier))).component();
        }
    }

    public static class HvBreaker extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        public HvBreaker(SmartBlockEntity be) {
            super(Lang.translateDirect("gui.hv_breaker.setting"), be, new ValueBoxTransforms.HvBreaker());
            withFormatter(i -> i == 0 ? "Off" : Integer.toString(i));
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(label, behaviour.getMax(), 10,
                    ImmutableList.of(Unit.CURRENT.get().component()), new ValueSettingsFormatter());
        }
    }

    public static class Fuse extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        public Fuse(SmartBlockEntity be) {
            super(Lang.translateDirect("devices.fuse.setting"), be, new ValueBoxTransforms.FuseHolder());
            withFormatter(i -> Integer.toString(Math.max(1, i)));
        }
    }

    public static class Gauge extends ScrollValueBehaviour<SmartBlockEntity, GaugeValueBehaviour> {
        public Gauge(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
            super(label, be, slot);
            withFormatter(this::format);
        }

        private String format(int value) {
            return NumberFormats.formatPrecise(behaviour.getValues()[value]) + " " + behaviour.getUnit().getString();
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(behaviour.getUnit());
            return new ValueSettingsBoard(label, behaviour.getMax(), 1, rows,
                    new ValueSettingsFormatter(this::formatSettings));
        }

        private MutableComponent formatSettings(ValueSettings settings) {
            return Lang.number(behaviour.getValues()[settings.value()])
                    .add(Component.literal(" "))
                    .add(behaviour.getUnit())
                    .component();
        }
    }

    public static class Resistor extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        private final int minOffset;

        public Resistor(SmartBlockEntity be, ValueBoxTransform slot, int minOffset) {
            super(Lang.translateDirect("devices.resistor.resistance"), be, slot);
            this.minOffset = minOffset;
            withFormatter(i -> NumberFormats.formatPrecise(ResistorValueBehaviour.exponentialValue(minOffset, i)));
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(Lang.translateDirect("generic.unit.ohm"));
            return new ValueSettingsBoard(label, behaviour.getMax(), 9, rows,
                    new ValueSettingsFormatter(this::formatSettings));
        }

        private MutableComponent formatSettings(ValueSettings settings) {
            return Lang.text(NumberFormats.formatPrecise(
                    ResistorValueBehaviour.exponentialValue(minOffset, settings.value()))).component();
        }
    }

    public static class SparkGap extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        public SparkGap(SmartBlockEntity be) {
            super(Lang.translateDirect("devices.spark_gap.voltage"), be, new ValueBoxTransforms.SparkGap());
            withFormatter(ModdedScrollBehaviours.SparkGap::getStringValue);
        }

        private static String getStringValue(int i) {
            return Integer.toString(i * 500 + 1000);
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(Unit.VOLTAGE.get().component());
            return new ValueSettingsBoard(label, 18, 9, rows, new ValueSettingsFormatter(this::formatSettings));
        }

        private MutableComponent formatSettings(ValueSettings settings) {
            return Lang.text(getStringValue(settings.value())).component();
        }
    }

    public static class MotorSpeed extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
        public MotorSpeed(SmartBlockEntity be) {
            super(Lang.translateDirect("devices.motor.speed"), be, new ValueBoxTransforms.ConstantSpeedMotor());
            withFormatter(String::valueOf);
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(Component.literal("RPM"));
            return new ValueSettingsBoard(label, 256, 32, rows, new ValueSettingsFormatter(this::formatSettings));
        }

        private MutableComponent formatSettings(ValueSettings settings) {
            return Lang.number(Math.max(1, settings.value())).component();
        }
    }

    public static class SolarPanelDivisor extends ScrollValueBehaviour<SmartBlockEntity, SolarPanelBearingBlockScrollBehaviour> {
        public SolarPanelDivisor(SmartBlockEntity be) {
            super(Lang.translateDirect("gui.solar_panel_bearing.slider"), be, new SolarPanelBearingBoxTransform());
            withFormatter(this::formatIndex);
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            DivisorOption[] options = buildOptions();
            ValueSettingsFormatter fmt = new ValueSettingsFormatter.ScrollOptionSettingsFormatter(options);
            return new ValueSettingsBoard(label, Math.max(0, options.length - 1), 1,
                    ImmutableList.of(Component.empty()), fmt);
        }

        private DivisorOption[] buildOptions() {
            var divisors = behaviour.getDivisors();
            DivisorOption[] options = new DivisorOption[divisors.size()];
            for (int i = 0; i < divisors.size(); i++) {
                int divisor = divisors.get(i);
                int perString = behaviour.getPanelCount() / divisor;
                options[i] = new DivisorOption(divisor + " × " + perString);
            }
            return options;
        }

        private String formatIndex(int idx) {
            var divisors = behaviour.getDivisors();
            if (divisors.isEmpty() || behaviour.getPanelCount() == 0)
                return "1 group";
            int divisor = divisors.get(Math.max(0, Math.min(idx, divisors.size() - 1)));
            int perString = behaviour.getPanelCount() / divisor;
            return divisor + " × " + perString;
        }

        private record DivisorOption(String label) implements INamedIconOptions {
            @Override
            public AllIcons getIcon() {
                return AllIcons.I_NONE;
            }

            @Override
            public String getTranslationKey() {
                return label;
            }
        }
    }
}
