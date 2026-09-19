package org.patryk3211.powergrid.electricity.solarpanel.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;

public class SolarBiomeRegistry {
    public static final ResourceKey<Registry<SolarBiomeEntry>> KEY = ResourceKey.createRegistryKey(PowerGrid.asResource("solar_biome_override"));

    public static SolarBiomeEntry forBiome(@NotNull Level level, BlockPos pos) {
        if (pos == null) return null;
        var registry = level.registryAccess().lookupOrThrow(KEY);
        var biome = level.getBiome(pos).getRegisteredName();
        for (var entry : registry) {
            if (entry.biome().equals(biome))
                return entry;
        }
        return null;
    }
}
