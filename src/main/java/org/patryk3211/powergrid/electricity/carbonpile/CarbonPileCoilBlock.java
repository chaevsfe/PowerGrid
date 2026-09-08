package org.patryk3211.powergrid.electricity.carbonpile;

import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.collections.ModdedTags;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.info.IHaveElectricProperties;
import org.patryk3211.powergrid.electricity.info.Power;
import org.patryk3211.powergrid.electricity.info.Resistance;

import java.util.List;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;

public class CarbonPileCoilBlock extends HorizontalElectricBlock implements IBE<CarbonPileCoilBlockEntity>, IHaveElectricProperties, NeighborUpdateListeningBlock {
    private static final TerminalBoundingBox[] TERMINALS = new TerminalBoundingBox[] {
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 4, 1, 1, 7, 3, 2),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 9, 1, 1, 12, 3, 2),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 0, 13, 6, 1, 15, 10),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 15, 13, 6, 16, 15, 10)
    };

    private static final VoxelShape SHAPE = Shapes.or(
            box(2, 0, 2, 14, 12, 14),
            box(1, 12, 1, 15, 16, 15)
    );

    public CarbonPileCoilBlock(Properties settings) {
        super(settings);
        setTerminalCollection(horizontalNorthTerminals(this, TERMINALS, SHAPE));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var player = ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown() ? ctx.getHorizontalDirection().getOpposite() : ctx.getHorizontalDirection();
        return defaultBlockState().setValue(HORIZONTAL_FACING, player);
    }

    @Override
    public Class<CarbonPileCoilBlockEntity> getBlockEntityClass() {
        return CarbonPileCoilBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CarbonPileCoilBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.CARBON_PILE_COIL.get();
    }

    public static int maxSize() {
        return ModdedConfigs.server().electricity.carbonPileMaxHeight.get();
    }

    @Override
    public void neighborUpdate(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        var abovePos = pos.above();
        if(neighborPos.equals(abovePos)) {
            if(level.getBlockState(abovePos).is(ModdedTags.Block.CARBON_PILE_BLOCK.tag)) {
                // Make stack into pile
                var topPos = abovePos;
                int size = 1;
                while(level.getBlockState(topPos).is(ModdedTags.Block.CARBON_PILE_BLOCK.tag) && size <= maxSize()) {
                    topPos = topPos.above();
                    ++size;
                }
                topPos = topPos.below();
                var baseState = ModdedBlocks.CARBON_PILE.getDefaultState();
                CarbonPileBlock.setPileState(level, topPos, baseState.setValue(CarbonPileBlock.TOP, true));
                for(var current = topPos.below(); !current.equals(pos); current = current.below()) {
                    CarbonPileBlock.setPileState(level, current, baseState.setValue(CarbonPileBlock.TOP, false));
                }
            }
            withBlockEntityDo(level, pos, CarbonPileCoilBlockEntity::pileChanged);
        }
    }


    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        Resistance.coil(resistance(), player, tooltip);
        Power.max(stack, player, tooltip);
    }
}
