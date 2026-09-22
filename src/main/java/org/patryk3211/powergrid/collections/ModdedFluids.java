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
package org.patryk3211.powergrid.collections;

import com.zurrtum.create.AllFluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.registrate.entry.ItemEntry;

public class ModdedFluids {
    public static final FlowableFluid ACID_FLOWING = new Flowing();
    public static final FlowableFluid ACID = new Still();
    public static FluidBlock ACID_BLOCK;
    public static BucketItem ACID_BUCKET;

    public static Fluid acid() {
        return ACID;
    }

    public static Fluid acidFlowing() {
        return ACID_FLOWING;
    }

    public static void register() {
        Registry.register(BuiltInRegistries.FLUID, PowerGrid.asResource("flowing_acid"), ACID_FLOWING);
        Registry.register(BuiltInRegistries.FLUID, PowerGrid.asResource("acid"), ACID);
    }

    public static void registerBlock() {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, PowerGrid.asResource("acid"));
        ACID_BLOCK = Registry.register(BuiltInRegistries.BLOCK, key,
                new AcidBlock(ACID, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(key)));
    }

    public static void registerItems() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PowerGrid.asResource("acid_bucket"));
        ACID_BUCKET = Registry.register(BuiltInRegistries.ITEM, key,
                new BucketItem(ACID, new Item.Properties().setId(key).craftRemainder(Items.BUCKET).stacksTo(1)));
        AllFluidItemInventory.ALL.put(ACID_BUCKET, new AllFluidItemInventory.Entry(BucketFluidInventory::new));
        PowerGrid.REGISTRATE.track(Registries.ITEM, new ItemEntry<>(key.identifier(), ACID_BUCKET));
    }

    private static class AcidBlock extends FluidBlock {
        public AcidBlock(FlowableFluid fluid, Properties properties) {
            super(fluid, properties);
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean isPrecise) {
            super.entityInside(state, level, pos, entity, applier, isPrecise);
            if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living))
                return;
            if (level.getRandom().nextInt(15) >= 10)
                living.hurtServer(serverLevel, ModdedDamageTypes.ACID.simpleDamageSource(level), 2);
        }
    }

    private static abstract class Acid extends FlowableFluid {
        @Override
        public Item getBucket() {
            return ACID_BUCKET != null ? ACID_BUCKET : Items.AIR;
        }

        @Override
        public BlockState createLegacyBlock(FluidState state) {
            if (ACID_BLOCK == null)
                return Blocks.AIR.defaultBlockState();
            return ACID_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == ACID || fluid == ACID_FLOWING;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 5;
        }

        @Override
        public int getSlopeFindDistance(LevelReader level) {
            return 4;
        }

        @Override
        protected float getExplosionResistance() {
            return 100f;
        }
    }

    private static class Flowing extends Acid {
        @Override
        public Fluid getFlowing() {
            return this;
        }

        @Override
        public Fluid getSource() {
            return ACID;
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    private static class Still extends Acid {
        @Override
        public Fluid getFlowing() {
            return ACID_FLOWING;
        }

        @Override
        public Fluid getSource() {
            return this;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
