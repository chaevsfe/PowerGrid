package org.patryk3211.powergrid.electricity.solarpanel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class SolarPanelBearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity> extends KineticBlockEntityRenderer<T, SolarPanelBearingRenderer.SolarPanelBearingRenderState> {
    public SolarPanelBearingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SolarPanelBearingRenderState createRenderState() {
        return new SolarPanelBearingRenderState();
    }

    @Override
    public void extractRenderState(T be, SolarPanelBearingRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level level = be.getLevel();
        if (state.support)
            SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(SolarPanelBearingBlock.FACING);
        float interpolatedAngle = be.getInterpolatedAngle(tickProgress - 1);

        SuperByteBuffer superBuffer = CachedBuffers.partial(ModdedPartialModels.SOLAR_PANEL_BEARING_ROTOR, blockState)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .rotateCentered((float) (interpolatedAngle / 180 * Math.PI), Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()));

        if (facing.getAxis().isHorizontal())
            superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
        if (facing.getAxis().isVertical()) {
            if (facing == Direction.UP)
                superBuffer.rotateCentered(AngleHelper.rad(-90 + AngleHelper.verticalAngle(facing)), Direction.EAST);
            else
                superBuffer.rotateCentered(AngleHelper.rad(90 + AngleHelper.verticalAngle(facing)), Direction.EAST);
        } else {
            superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.verticalAngle(facing)), Direction.EAST);
        }
        superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.verticalAngle(facing)), Direction.EAST);
        state.rotor = superBuffer.extractRenderState();
    }

    @Override
    public void submit(SolarPanelBearingRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.rotor != null)
            state.rotor.submit(matrices, queue);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(T be, SolarPanelBearingRenderState state) {
        BlockState blockState = be.getBlockState();
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, blockState.getValue(SolarPanelBearingBlock.FACING).getOpposite());
    }

    public static class SolarPanelBearingRenderState extends KineticBlockEntityRenderer.KineticRenderState {
        public @Nullable SuperByteBufferRenderState rotor;
    }
}
