package org.patryk3211.powergrid.fabric;

import net.fabricmc.api.ModInitializer;
import org.patryk3211.powergrid.PowerGrid;

public final class PowerGridFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PowerGridCreatePlugin.verifyEarlyRegistrationComplete();
        PowerGrid.init();
    }
}
