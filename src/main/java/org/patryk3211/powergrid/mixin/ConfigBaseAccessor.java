package org.patryk3211.powergrid.mixin;

import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.ConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ConfigBase.class)
public interface ConfigBaseAccessor {
    @Accessor
    List<ConfigBase.CValue<?>> getAllValues();

    @Accessor
    List<ConfigBase> getChildren();

    @Mixin(ConfigBase.CValue.class)
    interface CValueAccessor<V> {
        @Accessor
        ConfigValue<V> getValue();
    }
}
