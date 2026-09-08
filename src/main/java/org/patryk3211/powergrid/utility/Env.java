package org.patryk3211.powergrid.utility;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.function.Supplier;

public enum Env {
    CLIENT, SERVER;

    public static final Env CURRENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? CLIENT : SERVER;

    public boolean isCurrent() {
        return this == CURRENT;
    }

    public void runIfCurrent(Supplier<Runnable> run) {
        if (isCurrent())
            run.get().run();
    }

    public <T> Optional<T> getIfCurrent(Supplier<Supplier<T>> supplier) {
        if (isCurrent())
            return Optional.ofNullable(supplier.get().get());
        return Optional.empty();
    }

    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return switch (CURRENT) {
            case CLIENT -> clientTarget.get().get();
            case SERVER -> serverTarget.get().get();
        };
    }
}
