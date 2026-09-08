package org.patryk3211.powergrid.general.ceilingtile.lamp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

public class CeilingTileLampRenderer extends SmartBlockEntityRenderer<CeilingTileLampBlockEntity, CeilingTileLampRenderer.CeilingTileLampRenderState> {
    public CeilingTileLampRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CeilingTileLampRenderState createRenderState() {
        return new CeilingTileLampRenderState();
    }

    @Override
    public void extractRenderState(CeilingTileLampBlockEntity be, CeilingTileLampRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.glow = null;

        var bulbState = be.getBulbState();
        if (bulbState == null || bulbState.isBurned())
            return;

        Level level = be.getLevel();
        int a = (int) (bulbState.getAlpha() * 255);
        if (a > 0) {
            state.glow = CachedBuffers.partial(ModdedPartialModels.CEILING_LIGHT, be.getBlockState())
                    .cardinalLighting(level)
                    .light(state.lightCoords)
                    .color(a, a, a, 255)
                    .extractRenderState();
        }
    }

    @Override
    public void submit(CeilingTileLampRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.glow != null)
            state.glow.submit(ModdedRenderLayers.getAdditive(), matrices, queue.order(1));
    }

    public static class CeilingTileLampRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState glow;
    }
}
