package org.patryk3211.powergrid.collections;


import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingContraption;

public class ModdedContraptions {
    public static final ContraptionType SOLAR_PANEL = Registry.register(CreateRegistries.CONTRAPTION_TYPE,
            PowerGrid.asResource("solar_panel"), new ContraptionType(SolarPanelBearingContraption::new));

    public static void register() {
    }
}
