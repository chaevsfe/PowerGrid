package org.patryk3211.powergrid.fabric;

import com.zurrtum.create.api.registry.CreateRegisterPlugin;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedFluids;

public final class PowerGridCreatePlugin implements CreateRegisterPlugin {
    private static boolean blocksRegistered;
    private static boolean fluidsRegistered;

    @Override
    public void onBlockRegister() {
        if (blocksRegistered) {
            throw new IllegalStateException("Create Fly invoked Power Grid block registration more than once");
        }
        PowerGrid.registerBlocksEarly();
        blocksRegistered = true;
    }

    @Override
    public void onFluidRegister() {
        if (fluidsRegistered) {
            throw new IllegalStateException("Create Fly invoked Power Grid fluid registration more than once");
        }
        ModdedFluids.register();
        fluidsRegistered = true;
    }

    public static void verifyEarlyRegistrationComplete() {
        if (!blocksRegistered || !fluidsRegistered) {
            throw new IllegalStateException("Create Fly did not invoke Power Grid early registration (blocks=" + blocksRegistered + ", fluids=" + fluidsRegistered + ")");
        }
    }
}
