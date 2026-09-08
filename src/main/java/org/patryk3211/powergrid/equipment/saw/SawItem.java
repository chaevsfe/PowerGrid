package org.patryk3211.powergrid.equipment.saw;

import com.zurrtum.create.content.kinetics.saw.TreeCutter;
import com.zurrtum.create.foundation.utility.AbstractBlockBreakQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.equipment.ItemBoostUtils;
import org.patryk3211.powergrid.equipment.portablebattery.BatteryUtils;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class SawItem extends AxeItem {
    public SawItem(Properties properties) {
        super(ToolMaterial.IRON, -ToolMaterial.IRON.attackDamageBonus(), 0.0f, properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return BatteryUtils.isBarVisible(stack, energyPerUse());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return BatteryUtils.getBarWidth(stack, energyPerUse());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BatteryUtils.getBarColor(stack, energyPerUse());
    }

    public static int energyPerUse() {
        return ModdedConfigs.server().equipment.sawEnergyPerUse.get();
    }

    public static float baseSpeed() {
        return ModdedConfigs.server().equipment.sawMineSpeed.getF();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return (isCorrectToolForDrops(stack, state) ? baseSpeed() : 1.0F) * (ItemBoostUtils.isBoosted(stack) ? 2 : 1);
    }

    private static void dropTreeItem(Level level, BlockPos pos, ItemStack stack) {
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if(miningEntity instanceof Player player) {
            boolean boosted = ItemBoostUtils.useBoost(stack, player);
            float power = BatteryUtils.drawEnergy(player, energyPerUse() * (boosted ? 2 : 1));
            if(!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F && power == 0) {
                stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
            }
            if(player.isShiftKeyDown() && power >= 0.3f && !level.isClientSide()) {
                TreeCutter.findTree(level, pos, state).destroyBlocks(level, null, (pos1, stack1) -> dropTreeItem(level, pos1, stack1));
            }
            return true;
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        ItemBoostUtils.addTooltip(stack, tooltipComponents);
    }
}
