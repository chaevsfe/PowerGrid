package org.patryk3211.powergrid.equipment;

import com.zurrtum.create.content.kinetics.deployer.ItemApplicationInput;
import net.minecraft.server.level.ServerLevel;
import org.patryk3211.powergrid.collections.ModdedRecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedDataComponents;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;

public class ItemBoostUtils {
    public static boolean isBoosted(ItemStack stack) {
        var data = stack.get(ModdedDataComponents.BOOST);
        return data != null && data.durability() > 0;
    }

    public static void setBoosted(ItemStack stack, boolean boosted) {
        if(boosted) {
            stack.set(ModdedDataComponents.BOOST, BoostData.of((int) (stack.getMaxDamage() * 0.3f)));
        } else {
            stack.remove(ModdedDataComponents.BOOST);
        }
    }

    public static void addTooltip(ItemStack stack, java.util.function.Consumer<Component> tooltip) {
        if(isBoosted(stack)) {
            Lang.translate("tooltip.boosted")
                    .style(ChatFormatting.BLUE).style(ChatFormatting.ITALIC)
                    .addTo(tooltip);
        }
    }

    public static void damageBoost(ItemStack stack, Runnable breakCallback) {
        var data = stack.get(ModdedDataComponents.BOOST);
        if(data == null)
            return;
        var dmg = data.durability() - 1;
        if(dmg <= 0) {
            stack.remove(ModdedDataComponents.BOOST);
            if(dmg == 0)
                breakCallback.run();
            return;
        }
        stack.set(ModdedDataComponents.BOOST, BoostData.of(dmg));
    }

    public static boolean useBoost(ItemStack stack, LivingEntity entity) {
        if(!isBoosted(stack))
            return false;
        ItemBoostUtils.damageBoost(stack, () -> entity.onEquippedItemBroken(stack.getItem(), EquipmentSlot.MAINHAND));
        return true;
    }

    public static boolean canBoost(Level level, ItemStack chip, ItemStack toBoost) {
        if (isBoosted(toBoost))
            return false;
        if (level instanceof ServerLevel serverLevel)
            return serverLevel.recipeAccess().getRecipeFor(ModdedRecipeTypes.BOOSTING, new ItemApplicationInput(toBoost, chip), level).isPresent();
        var access = level.recipeAccess();
        return access.propertySet(ModdedRecipeTypes.BOOST_TARGET).test(toBoost)
                && access.propertySet(ModdedRecipeTypes.BOOST_INGREDIENT).test(chip);
    }
}
