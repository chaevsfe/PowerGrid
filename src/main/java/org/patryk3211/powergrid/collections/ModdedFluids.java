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
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.registrate.entry.ItemEntry;

public class ModdedFluids {
    private static final FluidEntry ACID_ENTRY = createEntry();
    public static final FlowableFluid ACID_FLOWING = ACID_ENTRY.flowing;
    public static final FlowableFluid ACID = ACID_ENTRY.still;
    public static FluidBlock ACID_BLOCK;
    public static BucketItem ACID_BUCKET;

    private static FluidEntry createEntry() {
        FluidEntry entry = new FluidEntry();
        entry.still = new Still(entry);
        entry.flowing = new Flowing(entry);
        return entry;
    }

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
        ACID_ENTRY.block = ACID_BLOCK;
    }

    public static void registerItems() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PowerGrid.asResource("acid_bucket"));
        ACID_BUCKET = Registry.register(BuiltInRegistries.ITEM, key,
                new BucketItem(ACID, new Item.Properties().setId(key).craftRemainder(Items.BUCKET).stacksTo(1)));
        ACID_ENTRY.bucket = ACID_BUCKET;
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

    private static class Flowing extends FlowableFluid.Flowing {
        public Flowing(FluidEntry entry) {
            super(entry);
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

    private static class Still extends FlowableFluid.Still {
        public Still(FluidEntry entry) {
            super(entry);
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
}
