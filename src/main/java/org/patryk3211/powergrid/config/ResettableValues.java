package org.patryk3211.powergrid.config;

import com.zurrtum.create.catnip.config.DoubleRawValue;

public interface ResettableValues {
    void resetToDefaults();

    static void reset(DoubleRawValue value) {
        value.set(value.getDefault());
        value.save();
    }
}
