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
package org.patryk3211.powergrid.electricity.creative;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class CreativeSourceValueBehaviour extends ServerScrollValueBehaviour {
    private final float multiplier;
    private Consumer<Float> callback;

    public CreativeSourceValueBehaviour(SmartBlockEntity be, float multiplier) {
        super(be);
        this.multiplier = multiplier;
        between(-500, 500);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(0, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        if (value == 0) {
            setValue(0);
            return;
        }
        setValue(switch (valueSetting.row()) {
            case 0 -> value + 250;
            case 1 -> value;
            case 2 -> -value;
            case 3 -> -value - 250;
            default -> throw new IllegalStateException();
        });
    }

    public void withMultipliedCallback(Consumer<Float> callback) {
        this.callback = callback;
    }

    private float processValue(int i) {
        if (i > 250) {
            i = (i - 250) * 100;
        } else if (i < -250) {
            i = (i + 250) * 100;
        }
        return i * multiplier;
    }

    public float getMultipliedValue() {
        return processValue(getValue());
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        if (callback != null)
            callback.accept(getMultipliedValue());
    }

    @Override
    public ValueSettings getValueSettings() {
        int i, row;
        if (value < -250) {
            row = 3;
            i = -value - 250;
        } else if (value < 0) {
            row = 2;
            i = -value;
        } else if (value <= 250) {
            row = 1;
            i = value;
        } else {
            row = 0;
            i = value - 250;
        }
        return new ValueSettings(row, i);
    }
}
