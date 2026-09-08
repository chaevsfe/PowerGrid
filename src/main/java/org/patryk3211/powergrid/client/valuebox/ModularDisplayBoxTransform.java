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
package org.patryk3211.powergrid.client.valuebox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlock;
import org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntity;

@Environment(EnvType.CLIENT)
public class ModularDisplayBoxTransform extends ValueBoxTransform {
    private final ModularDisplayBlockEntity blockEntity;

    public ModularDisplayBoxTransform(ModularDisplayBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        int slot = blockEntity.lastHitSlot;
        int col = slot % 2;
        int row = slot / 2;

        double pixel = 1.0 / 16.0;

        double x = 1.0 - (col * 8 + 4) / 16.0;
        double y = ((1 - row) * 8 + 4) / 16.0;

        x += (x > 0.5) ? -pixel / 2.0 : pixel / 2.0;
        y += (y > 0.5) ? -pixel / 2.0 : pixel / 2.0;

        double z = 1.0;

        return rotateHorizontally(state, new Vec3(x, y, z - 0.015));
    }

    @Override
    public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        int bestSlot = ModularDisplayBlockEntity.nearestSlot(state, localHit);
        double bestDist = ModularDisplayBlockEntity.slotDistance(state, localHit, bestSlot);

        blockEntity.lastHitSlot = bestSlot;
        blockEntity.syncBehaviourToSlot(bestSlot);

        if (level.isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            ItemStack held = mc.player.getMainHandItem();
            if (held.getItem() instanceof DyeItem)
                return false;
            if (mc.player.getItemInHand(InteractionHand.MAIN_HAND).is(AllItems.WRENCH))
                return false;
        }

        if (blockEntity.modules[bestSlot] == null)
            return false;

        return bestDist < 0.2;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        float yRot = AngleHelper.horizontalAngle(state.getValue(ModularDisplayBlock.FACING)) + 180;
        TransformStack.of(ms).rotateYDegrees(yRot);
    }

    @Override
    public float getScale() {
        return 0.65f;
    }
}
