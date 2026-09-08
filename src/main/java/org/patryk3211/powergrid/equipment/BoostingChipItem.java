package org.patryk3211.powergrid.equipment;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedAdvancements;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;

public class BoostingChipItem extends Item {
    public BoostingChipItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack boostChip = player.getItemInHand(usedHand);
        ItemStack stack = player.getItemInHand(usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (ItemBoostUtils.canBoost(level, boostChip, stack)) {
            player.startUsingItem(usedHand);
            return InteractionResult.PASS;
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;
        var boosted = player.getItemInHand(player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        ItemBoostUtils.setBoosted(boosted, true);
        if(!ModdedAdvancements.BOOSTING_CHIP.isAlreadyAwardedTo(player)) {
            ModdedAdvancements.BOOSTING_CHIP.awardTo(player);
        }
        entity.onEquippedItemBroken(stack.getItem(), player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        stack.shrink(1);
        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if(level.isClientSide())
            return;
        if((entity.getTicksUsingItem() - 6) % 7 != 0)
            return;
        var random = entity.getRandom();
        ModdedSoundEvents.BOOSTING.playFrom(entity, 0.9f + 0.2f * random.nextFloat(), random.nextFloat() * 0.2f + 0.9f);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

}
