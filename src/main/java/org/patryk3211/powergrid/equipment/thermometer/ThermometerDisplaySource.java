package org.patryk3211.powergrid.equipment.thermometer;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.utility.Lang;
import org.patryk3211.powergrid.utility.Unit;

public class ThermometerDisplaySource extends PercentOrProgressBarDisplaySource {
    @Override
    protected @Nullable Float getProgress(DisplayLinkContext context) {
        if (context.getSourceBlockEntity() instanceof ThermometerBlockEntity thermometer) {
            var temperature = thermometer.temperature();
            var comparisonTarget = getMode(context) % 2 == 0 ? 175 : Math.min(thermometer.maxTemperature, 175);
            return Mth.clamp(temperature / comparisonTarget, 0, 1);
        }
        return 0f;
    }

    @Override
    protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
        if (context.getSourceBlockEntity() instanceof ThermometerBlockEntity thermometer) {
            if (getMode(context) >= 4) {
                var temperature = getMode(context) == 4 ? thermometer.temperature() : thermometer.maxTemperature;
                var temperatureText = Lang.numberConstant(temperature);
                if (temperature > 175) {
                    temperatureText = Lang.text(">175");
                }
                return temperatureText
                        .add(Component.literal(" "))
                        .add(Unit.TEMPERATURE.get())
                        .component();
            }
        }
        return super.formatNumeric(context, currentLevel);
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return getMode(context) <= 1;
    }

    private int getMode(DisplayLinkContext context) {
        return context.sourceConfig().getIntOr("Mode", 0);
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "thermometer";
    }

}
