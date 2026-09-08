package org.patryk3211.powergrid.electricity.gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class EnergyMeterRenderer extends SmartBlockEntityRenderer<EnergyMeterBlockEntity, EnergyMeterRenderer.EnergyMeterRenderState> {
    public EnergyMeterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EnergyMeterRenderState createRenderState() {
        return new EnergyMeterRenderState();
    }

    @Override
    public void extractRenderState(EnergyMeterBlockEntity be, EnergyMeterRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.needles.clear();

        Level level = be.getLevel();
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(EnergyMeterBlock.HORIZONTAL_FACING);
        int index = 4;
        for (int i = 10000; i >= 1; i /= 10) {
            float angle = getDialAngle(be.energy, be.lastEnergy, i, tickProgress);

            double y = index % 2 == 0 ? 11.5 / 16 : 7.5 / 16;
            double x = (12.0 - index * 2) / 16;

            state.needles.add(CachedBuffers.partial(ModdedPartialModels.ENERGY_METER_NEEDLE, blockState)
                    .rotateYCenteredDegrees(-facing.toYRot() - 180)
                    .translate(x, y, 0)
                    .rotateZ(-angle)
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .extractRenderState());
            --index;
        }
    }

    @Override
    public void submit(EnergyMeterRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        for (SuperByteBufferRenderState needle : state.needles)
            needle.submit(matrices, queue);
    }

    public static float getDialAngle(double energy, double lastEnergy, double multiplier, double partialTick) {
        float rotation = (float) ((energy / multiplier / 10) % 1);
        float prevRotation = (float) ((lastEnergy / multiplier / 10) % 1);
        if (energy > lastEnergy && rotation < prevRotation) {
            rotation += 1;
        }
        if (energy < lastEnergy && rotation > prevRotation) {
            rotation -= 1;
        }
        return (float) Mth.lerp(partialTick, prevRotation * Math.PI * 2, rotation * Math.PI * 2);
    }

    public static class EnergyMeterRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public final List<SuperByteBufferRenderState> needles = new ArrayList<>();
    }
}
