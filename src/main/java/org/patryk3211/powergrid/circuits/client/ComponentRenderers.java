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
package org.patryk3211.powergrid.circuits.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.joml.Matrix4f;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.BarretterTubeComponent;
import org.patryk3211.powergrid.circuits.components.ElectronTubeComponent;
import org.patryk3211.powergrid.circuits.components.GaugeComponent;
import org.patryk3211.powergrid.circuits.components.LabelComponent;
import org.patryk3211.powergrid.circuits.components.LightBulbComponent;
import org.patryk3211.powergrid.circuits.components.ModularDisplayComponent;
import org.patryk3211.powergrid.circuits.components.NeonBulbComponent;
import org.patryk3211.powergrid.circuits.components.OrientableComponent;
import org.patryk3211.powergrid.circuits.components.PotentiometerComponent;
import org.patryk3211.powergrid.circuits.components.RegulatorTubeComponent;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.collections.ModdedRenderLayers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ComponentRenderers {
    private static final Map<Class<?>, ComponentRenderer> RENDERERS = new HashMap<>();

    static {
        RENDERERS.put(BarretterTubeComponent.class, ComponentRenderers::barretterTube);
        RENDERERS.put(ElectronTubeComponent.class, ComponentRenderers::electronTube);
        RENDERERS.put(RegulatorTubeComponent.class, ComponentRenderers::regulatorTube);
        RENDERERS.put(GaugeComponent.class, ComponentRenderers::gauge);
        RENDERERS.put(PotentiometerComponent.class, ComponentRenderers::potentiometer);
        RENDERERS.put(LabelComponent.class, ComponentRenderers::label);
        RENDERERS.put(LightBulbComponent.class, ComponentRenderers::lightBulb);
        RENDERERS.put(NeonBulbComponent.class, ComponentRenderers::neonBulb);
        RENDERERS.put(ModularDisplayComponent.class, ComponentRenderers::modularDisplay);
    }

    public static ComponentRenderer get(org.patryk3211.powergrid.circuits.components.Component component) {
        for(Class<?> clazz = component.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            var renderer = RENDERERS.get(clazz);
            if(renderer != null)
                return renderer;
        }
        return null;
    }

    private static void glow(List<ComponentDrawCall> out, SuperByteBufferRenderState state) {
        out.add((ms, queue) -> state.submit(ModdedRenderLayers.getAdditive(), ms, queue.order(1)));
    }

    private static void barretterTube(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        int a = 0;
        if(placed.customData instanceof org.patryk3211.powergrid.circuits.components.Component.FloatPair data) {
            a = (int) (data.lerped(partialTicks) * 64);
        }
        if(a == 0)
            return;
        glow(out, CachedBuffers.partial(ModdedPartialModels.BARRETTER_GLOW, be.getBlockState())
                .disableDiffuse()
                .color(a, a, a, 255)
                .light(LightCoordsUtil.FULL_BRIGHT)
                .extractRenderState());
    }

    private static void electronTube(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        int a = 0;
        if(placed.customData instanceof ElectronTubeComponent.RenderData data) {
            a = (int) (Mth.lerp(partialTicks, data.prev, data.current) * 64);
        }
        if(a == 0)
            return;
        glow(out, CachedBuffers.partial(ModdedPartialModels.ELECTRON_TUBE_GLOW, be.getBlockState())
                .disableDiffuse()
                .color(a, a, a, 255)
                .light(LightCoordsUtil.FULL_BRIGHT)
                .extractRenderState());
    }

    private static void regulatorTube(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        int a = 0;
        if(placed.customData instanceof LerpedFloat lerped) {
            a = (int) (lerped.getValue(partialTicks) * 128);
        }
        if(a == 0)
            return;
        glow(out, CachedBuffers.partial(ModdedPartialModels.REGULATOR_TUBE_GLOW, be.getBlockState())
                .disableDiffuse()
                .color(a, a, a, 255)
                .light(LightCoordsUtil.FULL_BRIGHT)
                .extractRenderState());
    }

    private static void gauge(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        if(!(placed.customData instanceof GaugeComponent.RenderData data))
            return;
        var value = Mth.lerp(partialTicks, data.prevState, data.state);
        var angle = -90 * value;
        var needle = CachedBuffers.partial(ModdedPartialModels.COMPONENT_GAUGE_NEEDLE, be.getBlockState())
                .translate(2.5f / 16, 0, 2.5f / 16)
                .rotateYDegrees(placed.get(OrientableComponent.ORIENTATION).ordinal() * 90)
                .translate(-1 / 16f, 0, 1 / 16f)
                .rotateYDegrees(angle)
                .translate(-1.5f / 16, 0, -3.5f / 16)
                .light(light)
                .extractRenderState();
        out.add(needle::submit);
    }

    private static void potentiometer(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        var angle = 135 - 135 * 2 * (placed.get(PotentiometerComponent.VALUE) / 100.0f);
        var knob = CachedBuffers.partial(ModdedPartialModels.POTENTIOMETER_KNOB, be.getBlockState())
                .translate(2.5f / 16, 0, 2.5f / 16)
                .rotateYDegrees(angle)
                .translate(-2.5f / 16, 0, -2.5f / 16)
                .light(light)
                .extractRenderState();
        out.add(knob::submit);
    }

    private static void label(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        var segments = new ArrayList<SuperByteBufferRenderState>();
        for (int i = 1; i < placed.get(LabelComponent.SIZE); i++) {
            var tint = LabelComponent.SEGMENT_TINT[i];
            segments.add(CachedBuffers.partial(ModdedPartialModels.LABEL, be.getBlockState())
                    .translate((1 / 16f) * (float) i, 0, 0)
                    .light(light)
                    .color(tint, tint, tint, 255)
                    .extractRenderState());
        }

        Font fontRenderer = Minecraft.getInstance().font;
        var text = Component
                .literal(fontRenderer.plainSubstrByWidth(placed.get(org.patryk3211.powergrid.circuits.components.Component.LABEL), 8 * placed.get(LabelComponent.SIZE)))
                .getVisualOrderText();
        int color = placed.get(LabelComponent.COLOR).getTextColor();

        out.add((ms, queue) -> {
            for (var segment : segments)
                segment.submit(ms, queue);

            ms.pushPose();
            var msr = TransformStack.of(ms);
            msr.center();
            msr.rotateXDegrees(90);
            msr.uncenter();
            ms.translate(0, 0, 1f - (1 / 512f));
            ms.scale(1 / 128f, 1 / 128f, 1 / 128f);
            queue.submitText(ms, 0, 0, text, false, Font.DisplayMode.NORMAL, light, color, 0, 0);
            ms.popPose();
        });
    }

    private static void lightBulb(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        var glowModel = ModdedPartialModels.LIGHT_BULB_GLOW_DYED;
        var bulbModel = ModdedPartialModels.LIGHT_BULB_BULB_DYED;

        if (placed.get(LightBulbComponent.COLOR) == DyeColor.WHITE) {
            glowModel = ModdedPartialModels.LIGHT_BULB_GLOW;
            bulbModel = ModdedPartialModels.LIGHT_BULB_BULB;
        }

        var color = placed.get(LightBulbComponent.COLOR).getTextureDiffuseColor();

        var red = (color >> 16) & 0xFF;
        var green = (color >> 8) & 0xFF;
        var blue = color & 0xFF;

        var bulb = CachedBuffers.partial(bulbModel, be.getBlockState())
                .color(red, green, blue, 255)
                .light(light)
                .extractRenderState();
        out.add((ms, queue) -> bulb.submit(RenderTypes.cutoutMovingBlock(), ms, queue));

        int a = 0, r = 0, g = 0, b = 0;
        if(placed.customData instanceof org.patryk3211.powergrid.circuits.components.Component.FloatPair temps) {
            a = (int) (temps.lerped(partialTicks) * 128);
            r = (int) (red * temps.lerped(partialTicks) * 128 / 256);
            g = (int) (green * temps.lerped(partialTicks) * 128 / 256);
            b = (int) (blue * temps.lerped(partialTicks) * 128 / 256);
        }
        var center = 1.5f / 16f;
        var orientation = placed.get(OrientableComponent.ORIENTATION);
        if(a != 0) {
            glow(out, CachedBuffers.partial(glowModel, be.getBlockState())
                    .disableDiffuse()
                    .color(r, g, b, 255)
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .translate(center, center, center)
                    .rotateYDegrees(orientation.ordinal() * 90)
                    .translateBack(center, center, center)
                    .extractRenderState());
        }
    }

    private static void neonBulb(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        var bulb = CachedBuffers.partial(ModdedPartialModels.NEON_TUBE_BULB, be.getBlockState())
                .light(light)
                .extractRenderState();
        out.add((ms, queue) -> bulb.submit(CreateRenderTypes.translucent(), ms, queue));

        boolean vertical = placed.get(NeonBulbComponent.VERTICAL);
        var glowPartial = vertical ? ModdedPartialModels.NEON_TUBE_VERTICAL_GLOW : ModdedPartialModels.NEON_TUBE_GLOW;
        var yRotation = vertical ? 0 : 1;
        var lowerOffset = vertical ? -1 / 16f : 0f;

        var color = placed.get(NeonBulbComponent.COLOR).getTextureDiffuseColor();
        var red = (color >> 16) & 0xFF;
        var green = (color >> 8) & 0xFF;
        var blue = color & 0xFF;

        int a1 = 0, r1 = 0, g1 = 0, b1 = 0, a2 = 0, r2 = 0, g2 = 0, b2 = 0;
        if(placed.customData instanceof NeonBulbComponent.LerpPair pair) {
            a1 = (int) (pair.first.getValue(partialTicks) * 128);
            r1 = (int) (red * pair.first.getValue(partialTicks) * 128 / 256);
            g1 = (int) (green * pair.first.getValue(partialTicks) * 128 / 256);
            b1 = (int) (blue * pair.first.getValue(partialTicks) * 128 / 256);

            a2 = (int) (pair.second.getValue(partialTicks) * 128);
            r2 = (int) (red * pair.second.getValue(partialTicks) * 128 / 256);
            g2 = (int) (green * pair.second.getValue(partialTicks) * 128 / 256);
            b2 = (int) (blue * pair.second.getValue(partialTicks) * 128 / 256);
        }

        var center = 1 / 16f;
        var orientation = placed.get(OrientableComponent.ORIENTATION);

        if(a1 != 0) {
            glow(out, CachedBuffers.partial(glowPartial, be.getBlockState())
                    .disableDiffuse()
                    .color(r1, g1, b1, 255)
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .translate(center, center, center)
                    .rotateYDegrees(yRotation * orientation.ordinal() * 90)
                    .translateBack(center, center, center)
                    .extractRenderState());
        }
        if(a2 != 0) {
            glow(out, CachedBuffers.partial(glowPartial, be.getBlockState())
                    .disableDiffuse()
                    .color(r2, g2, b2, 255)
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .translate(center, center, center)
                    .rotateYDegrees(yRotation * (180 + orientation.ordinal() * 90))
                    .translateBack(center, center, center)
                    .translate(0, lowerOffset, 0)
                    .extractRenderState());
        }
    }

    private static void modularDisplay(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, int light, int overlay, List<ComponentDrawCall> out) {
        var module = placed.get(ModularDisplayComponent.CURRENT_MODULE);

        boolean halfClick = placed.has(ModularDisplayComponent.HALF_CLICK) && placed.get(ModularDisplayComponent.HALF_CLICK);

        float frameIndex = placed.get(ModularDisplayComponent.INDEX);
        if (halfClick)
            frameIndex -= .5f;
        var displayTexture = "block/modular_display/" + (placed.get(ModularDisplayComponent.REMOVE_BLANKING_PAGE) ?
                module.getDisplayTexture() + "noblank" : module.getDisplayTexture());

        float innerX = ModularDisplayComponent.INNER_OFFSET;
        float innerY = ModularDisplayComponent.INNER_UD_OFFSET;

        float sheetWidth = module.getSpriteWidth();
        float uMin = (frameIndex * (ModularDisplayComponent.FRAME_WIDTH + ModularDisplayComponent.FRAME_PADDING)) / sheetWidth;
        float uMax = (frameIndex * (ModularDisplayComponent.FRAME_WIDTH + ModularDisplayComponent.FRAME_PADDING) + ModularDisplayComponent.FRAME_WIDTH) / sheetWidth;
        float vMin = 0f;
        float vMax = ModularDisplayComponent.FRAME_HEIGHT / ModularDisplayComponent.SHEET_HEIGHT;

        int rgb = placed.get(ModularDisplayComponent.CURRENT_COLOR).getTextureDiffuseColor();
        Identifier texture = PowerGrid.texture(displayTexture);

        out.add((ms, queue) -> {
            ms.pushPose();
            ms.translate(0, 6f / 16f + ModularDisplayComponent.Y_NUDGE, 0);
            queue.submitCustomGeometry(ms, RenderTypes.text(texture), (pose, consumer) -> renderQuad(
                    pose.pose(), consumer, innerX, innerY,
                    ModularDisplayComponent.INNER_RL_SIZE, ModularDisplayComponent.INNER_UD_SIZE,
                    uMin, vMin, uMax, vMax, light, overlay, rgb));
            ms.popPose();
        });
    }

    private static void renderQuad(Matrix4f matrix, VertexConsumer vc,
                                   float x, float z, float width, float height, float uMin, float vMin, float uMax, float vMax,
                                   int packedLight, int packedOverlay, int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        vc.addVertex(matrix, x + width, 0f, z).setColor(r, g, b, 255)
                .setUv(uMax, vMin).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x, 0f, z).setColor(r, g, b, 255)
                .setUv(uMin, vMin).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x, 0f, z + height).setColor(r, g, b, 255)
                .setUv(uMin, vMax).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x + width, 0f, z + height).setColor(r, g, b, 255)
                .setUv(uMax, vMax).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);
    }
}
