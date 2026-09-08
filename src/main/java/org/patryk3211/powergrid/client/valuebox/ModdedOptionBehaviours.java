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

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.collections.ModIcons;
import org.patryk3211.powergrid.electricity.modulardisplay.DisplayModuleType;
import org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchBlockEntity.ClutchMode;
import org.patryk3211.powergrid.utility.Lang;

@Environment(EnvType.CLIENT)
public class ModdedOptionBehaviours {
    public enum ClutchModeIcon implements INamedIconOptions {
        GENERATOR,
        MOTOR;

        @Override
        public AllIcons getIcon() {
            return switch(this) {
                case GENERATOR -> ModIcons.I_GENERATOR;
                case MOTOR -> ModIcons.I_MOTOR;
            };
        }

        @Override
        public String getTranslationKey() {
            return switch(this) {
                case GENERATOR -> "powergrid.gui.clutch_mode.generator";
                case MOTOR -> "powergrid.gui.clutch_mode.motor";
            };
        }
    }

    public enum DisplayModuleIcon implements INamedIconOptions {
        ZERO_TO_NINE,
        NINE_TO_ZERO,
        ONE_TO_ZERO,
        HEXADECIMAL,
        SYMBOLS,
        ALPHABET;

        @Override
        public AllIcons getIcon() {
            return AllIcons.I_NONE;
        }

        @Override
        public String getTranslationKey() {
            return DisplayModuleType.values()[ordinal()].getTranslationKey();
        }
    }

    public static class ClutchModeScroll extends ScrollOptionBehaviour<ClutchMode> {
        public ClutchModeScroll(GeneratorClutchBlockEntity be) {
            super(ClutchModeIcon.class, mode -> ClutchModeIcon.values()[mode.ordinal()],
                    Lang.translateDirect("gui.clutch_mode"), be, new ValueBoxTransforms.GeneratorClutch());
        }
    }

    public static class ModuleTypeScroll extends ScrollOptionBehaviour<DisplayModuleType> {
        public ModuleTypeScroll(ModularDisplayBlockEntity be) {
            super(DisplayModuleIcon.class, type -> DisplayModuleIcon.values()[type.ordinal()],
                    Lang.translateDirect("devices.modular_display.module_type"), be,
                    new ModularDisplayBoxTransform(be));
        }

        @Override
        public boolean bypassesInput(ItemStack mainhandItem) {
            return mainhandItem.getItem() instanceof DyeItem || mainhandItem.is(AllItems.WRENCH);
        }
    }
}
