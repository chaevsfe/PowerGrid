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
package org.patryk3211.powergrid.electricity.resistor;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class ResistorValueBehaviour extends ServerScrollValueBehaviour {
    private final int minOffset;

    public ResistorValueBehaviour(SmartBlockEntity be, int minOffset, int max) {
        super(be);
        this.minOffset = minOffset;
        between(0, max);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(0, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(value);
    }

    public ServerScrollValueBehaviour withResistanceCallback(Consumer<Float> resistanceCallback) {
        return withCallback(i -> resistanceCallback.accept(exponentialValue(minOffset, i)));
    }

    public static float exponentialValue(int min, int i) {
        var number = i % 9 + 1;
        var mult = Math.pow(10, i / 9 - min);
        return (float) (number * mult);
    }

    public float getResistance() {
        return exponentialValue(minOffset, value);
    }
}
