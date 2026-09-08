package org.patryk3211.powergrid.electricity.modulardisplay;

import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ModuleTypeBehaviour extends ServerScrollOptionBehaviour<DisplayModuleType> {
    private final ModularDisplayBlockEntity display;

    public ModuleTypeBehaviour(ModularDisplayBlockEntity be) {
        super(DisplayModuleType.class, be);
        this.display = be;
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        var pos = display.getBlockPos();
        var localHit = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos));
        display.lastHitSlot = ModularDisplayBlockEntity.nearestSlot(display.getBlockState(), localHit);
        display.syncBehaviourToSlot(display.lastHitSlot);
    }

    public void setValueWithoutCallback(int value) {
        this.value = value;
    }
}
