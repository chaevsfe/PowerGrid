package org.patryk3211.powergrid.electricity.modulardisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.patryk3211.powergrid.PowerGrid;

import java.util.ArrayList;
import java.util.List;

public class ModularDisplayBlockEntityRenderer extends SmartBlockEntityRenderer<ModularDisplayBlockEntity, ModularDisplayBlockEntityRenderer.ModularDisplayRenderState> {
    private static final float SHEET_HEIGHT = 16f;

    private static final float FRAME_WIDTH = 5f;
    private static final float FRAME_HEIGHT = 7f;
    private static final float FRAME_PADDING = 1f;

    private static final int GRID_COLS = 2;
    private static final int GRID_ROWS = 2;

    private static final float PIXEL = 1f / 16f;
    private static final float INNER_UD_SIZE = 5f * PIXEL;
    private static final float INNER_RL_SIZE = 4.25f * PIXEL;

    private static final float Z_NUDGE = 0.001f;

    public ModularDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ModularDisplayRenderState createRenderState() {
        return new ModularDisplayRenderState();
    }

    @Override
    public void extractRenderState(ModularDisplayBlockEntity be, ModularDisplayRenderState state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.quads.clear();

        Direction facing = be.getBlockState().getValue(ModularDisplayBlock.HORIZONTAL_FACING);
        state.yRot = switch (facing) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> 270f;
            default -> 0f;
        };

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int slotIndex = row * GRID_COLS + col;
                SlotData slot = be.getSlot(slotIndex);

                if (slot.isEmpty())
                    continue;

                var halfClick = slot.getModule().getHalfClick();
                float innerX = 0, innerY = 0;
                switch (slotIndex) {
                    case 0 -> { innerX = 2 * PIXEL + (.375f / 16); innerY = 9 * PIXEL; }
                    case 1 -> { innerX = 9 * PIXEL + (.375f / 16); innerY = 9 * PIXEL; }
                    case 2 -> { innerX = 2 * PIXEL + (.375f / 16); innerY = 2 * PIXEL; }
                    case 3 -> { innerX = 9 * PIXEL + (.375f / 16); innerY = 2 * PIXEL; }
                }

                float frameIndex = slot.getIndex();
                if (halfClick)
                    frameIndex -= .5f;
                var sheetWidth = slot.getModule().getDisplayTextureSize();

                float uMin = (frameIndex * (FRAME_WIDTH + FRAME_PADDING)) / sheetWidth;
                float uMax = (frameIndex * (FRAME_WIDTH + FRAME_PADDING) + FRAME_WIDTH) / sheetWidth;
                float vMin = 0f;
                float vMax = FRAME_HEIGHT / SHEET_HEIGHT;

                int rgb = slot.getModule().getColor().getTextureDiffuseColor();
                var texture = be.getRemovedBlankingPage(slotIndex)
                        ? slot.getModule().getDisplayTexture() + "noblank"
                        : slot.getModule().getDisplayTexture();

                state.quads.add(new DisplayQuad(PowerGrid.texture(texture), innerX, innerY,
                        INNER_RL_SIZE, INNER_UD_SIZE, uMin, vMin, uMax, vMax, rgb));
            }
        }
    }

    @Override
    public void submit(ModularDisplayRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.quads.isEmpty())
            return;

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(state.yRot));
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0f, 0f, -Z_NUDGE);

        int light = state.lightCoords;
        for (DisplayQuad quad : state.quads)
            queue.submitCustomGeometry(matrices, RenderTypes.text(quad.texture()), (pose, consumer) -> renderQuad(pose.pose(), consumer, quad, light));

        matrices.popPose();
    }

    private static void renderQuad(Matrix4f matrix, VertexConsumer vc, DisplayQuad quad, int packedLight) {
        int r = (quad.rgb() >> 16) & 0xFF;
        int g = (quad.rgb() >> 8) & 0xFF;
        int b = quad.rgb() & 0xFF;
        int packedOverlay = OverlayTexture.NO_OVERLAY;
        float x = quad.x(), y = quad.y(), width = quad.width(), height = quad.height();

        vc.addVertex(matrix, x + width, y, 0f).setColor(r, g, b, 255)
                .setUv(quad.uMin(), quad.vMax()).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, -1f);

        vc.addVertex(matrix, x, y, 0f).setColor(r, g, b, 255)
                .setUv(quad.uMax(), quad.vMax()).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, -1f);

        vc.addVertex(matrix, x, y + height, 0f).setColor(r, g, b, 255)
                .setUv(quad.uMax(), quad.vMin()).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, -1f);

        vc.addVertex(matrix, x + width, y + height, 0f).setColor(r, g, b, 255)
                .setUv(quad.uMin(), quad.vMin()).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, -1f);
    }

    public record DisplayQuad(Identifier texture, float x, float y, float width, float height,
                              float uMin, float vMin, float uMax, float vMax, int rgb) {
    }

    public static class ModularDisplayRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public final List<DisplayQuad> quads = new ArrayList<>();
        public float yRot;
    }
}
