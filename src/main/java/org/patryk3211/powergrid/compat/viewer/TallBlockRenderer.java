/*
 * Copyright 2026 chaevsfe
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
package org.patryk3211.powergrid.compat.viewer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;

public final class TallBlockRenderer extends PictureInPictureRenderer<TallBlockRenderState> {
    private final BlockBakedQuadOutput output;

    public TallBlockRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
        output = new BlockBakedQuadOutput(bufferSource);
    }

    @Override
    protected void renderToTexture(TallBlockRenderState state, PoseStack matrices) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.translate(0.0f, -state.extraHeight() / state.scale(), 0.0f);
        matrices.scale(1.0f, 1.0f, -1.0f);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1.0f, -1.0f, 1.0f);
        BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(state.state());
        output.setPoseStack(matrices);
        output.updateBuffer(model);
        ModelRenderHelper.getHelper(output).tesselateBlock(0.0f, 0.0f, 0.0f,
            SinglePosVirtualBlockGetter.createFullBright().blockState(state.state()), BlockPos.ZERO, state.state(), model, 42L);
        output.clearBuffer();
    }

    @Override
    protected String getTextureLabel() {
        return "powergrid_tall_block";
    }

    @Override
    public Class<TallBlockRenderState> getRenderStateClass() {
        return TallBlockRenderState.class;
    }
}
