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
package org.patryk3211.powergrid.equipment.portablebattery;

import com.zurrtum.create.AllEnchantments;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.EnergyStorage;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.electricity.info.IHaveElectricProperties;
import org.patryk3211.powergrid.electricity.info.Power;
import org.patryk3211.powergrid.electricity.info.Resistance;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public class PortableBatteryItem extends Item implements IHaveElectricProperties {
    public static final int BAR_COLOR = 0xEFEFDE;
    private Supplier<PortableBatteryPlaceableItem> blockItem;

    public PortableBatteryItem(ArmorMaterial material, Properties settings, Supplier<PortableBatteryPlaceableItem> placeable) {
        super(settings.stacksTo(1)
                .attributes(material.createAttributes(ArmorType.CHESTPLATE))
                .enchantable(material.enchantmentValue())
                .repairable(material.repairIngredient())
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                        .setEquipSound(material.equipSound())
                        .setAsset(material.assetId())
                        .build()));
        this.blockItem = placeable;
    }

    @Override
    public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
        return enchantment.is(AllEnchantments.CAPACITY);
    }

    public static PortableBatteryItem getWornBy(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity))
            return null;
        if(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PortableBatteryItem battery)
            return battery;
        return null;
    }

    public Block getBlock() {
        return blockItem.get().getBlock();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float current = Mth.clamp((float) BatteryUtils.getCurrentCharge(stack) / BatteryUtils.getMaxCharge(stack), 0, 1);
        return Math.round(13.0f * current);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return blockItem.get()
                .useOn(ctx);
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        Resistance.series(ModdedBlocks.PORTABLE_BATTERY.get().resistance(), player, tooltip);
        Power.max(ModdedBlocks.PORTABLE_BATTERY.asStack(), player, tooltip);
        float maxCharge = BatteryUtils.getMaxCharge(stack);
        float charge = BatteryUtils.getCurrentCharge(stack) / maxCharge;
        Lang.translate("tooltip.charge.current")
                .style(ChatFormatting.GRAY).addTo(tooltip);
        Lang.builder()
                .add(Component.literal(" "))
                .add(Lang.numberConstant(charge * 100))
                .add(Component.literal("%"))
                .style(ChatFormatting.AQUA).addTo(tooltip);
    }

    public void onWornTick(ItemStack batteryStack, Player player) {
        ItemStack heldStack = player.getMainHandItem();
        if (heldStack.isEmpty()) return;
        tryTransferPower(heldStack, batteryStack, player);
    }
    public static void tryTransferPower(ItemStack heldStack, ItemStack batteryStack, Player player) {
        var heldEnergy = EnergyStorage.ITEM.find(heldStack, ContainerItemContext.ofPlayerHand(player, InteractionHand.MAIN_HAND));
        if (heldEnergy == null || !heldEnergy.supportsInsertion()) return;

        long maxEnergyDrain = heldEnergy.getCapacity() - heldEnergy.getAmount();
        if (maxEnergyDrain < 1) return;

        long energyDrain;
        try (Transaction simulation = Transaction.openOuter()) {
            energyDrain = heldEnergy.insert(maxEnergyDrain, simulation);
        }
        if (energyDrain <= 0) return;

        float available = BatteryUtils.drawEnergy(player, (int) Math.min(energyDrain, Integer.MAX_VALUE));
        if (available <= 0) return;

        try (Transaction transaction = Transaction.openOuter()) {
            heldEnergy.insert((long) (available * energyDrain), transaction);
            transaction.commit();
        }
    }
}