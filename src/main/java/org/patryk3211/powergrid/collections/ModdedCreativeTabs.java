package org.patryk3211.powergrid.collections;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGrid;

public class ModdedCreativeTabs {
    public static final ResourceKey<CreativeModeTab> MAIN = ResourceKey.create(Registries.CREATIVE_MODE_TAB, PowerGrid.asResource("main"));

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN, FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.powergrid.main"))
                .icon(() -> new ItemStack(ModdedBlocks.ELECTRIC_MOTOR))
                .displayItems(new ItemDisplay.BaseItemDisplay(true))
                .build());
    }
}
