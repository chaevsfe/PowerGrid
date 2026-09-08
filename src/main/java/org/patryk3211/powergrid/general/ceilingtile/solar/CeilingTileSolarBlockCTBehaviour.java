package org.patryk3211.powergrid.general.ceilingtile.solar;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShifter;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;

@Environment(EnvType.CLIENT)
public class CeilingTileSolarBlockCTBehaviour extends ConnectedTextureBehaviour.Base {
    private static final CTSpriteShiftEntry TOP = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            PowerGrid.asResource("block/solar_panel/solar_panel"),
            PowerGrid.asResource("block/solar_panel/solar_panel_connected")
    );

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState blockState, Direction direction, @Nullable TextureAtlasSprite textureAtlasSprite) {
        var facing = Direction.DOWN; //imitate facing on normal horizontal panel
        if(facing == direction.getOpposite())
            return TOP;
        return null;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        if(state != other)
            return false;
        var be1 = reader.getBlockEntity(pos);
        var be2 = reader.getBlockEntity(otherPos);
        if(be1 instanceof CeilingTileSolarBlockEntity sbe1 && be2 instanceof CeilingTileSolarBlockEntity sbe2) {
            return CeilingTileSolarBlockEntity.areConnected(sbe1, sbe2);
        }
        return false;
    }
}
