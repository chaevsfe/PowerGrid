/*
 * Copyright (c) 2022 The Create Team
 * Modified 2026 by chaevsfe for the unofficial Fabric / Create Fly port.
 *
 * Derived from LangBuilder in Create's catnip library
 * (https://github.com/Creators-of-Create/Ponder) as shipped in Create Fly
 * (https://github.com/ZurrTum/Create-Fly).
 *
 * Licensed under the MIT License. The full licence text, including the
 * copyright and permission notice that must be kept with all copies or
 * substantial portions of this file, is included as licenses/Ponder-LICENSE.
 *
 * The software is provided "as is", without warranty of any kind, express or
 * implied. See the licence text for the full terms.
 */
package org.patryk3211.powergrid.utility;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;

public class LangBuilder {
    private static final float DEFAULT_SPACE_WIDTH = 4.0F;

    private final String namespace;
    private MutableComponent component;

    public LangBuilder(String namespace) {
        this.namespace = namespace;
    }

    public LangBuilder translate(String langKey, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof LangBuilder builder)
                args[i] = builder.component();
        }
        return add(Component.translatable(namespace + "." + langKey, args));
    }

    public LangBuilder text(String literalText) {
        return add(Component.literal(literalText));
    }

    public LangBuilder text(int color, String literalText) {
        return add(Component.literal(literalText).withStyle(s -> s.withColor(color)));
    }

    public LangBuilder add(LangBuilder otherBuilder) {
        return add(otherBuilder.component());
    }

    public LangBuilder add(MutableComponent customComponent) {
        component = component == null ? customComponent : component.append(customComponent);
        return this;
    }

    public LangBuilder add(Component component) {
        if (component instanceof MutableComponent mutableComponent)
            return add(mutableComponent);
        return add(component.copy());
    }

    public LangBuilder style(ChatFormatting format) {
        assertComponent();
        component = component.withStyle(format);
        return this;
    }

    public LangBuilder color(int color) {
        assertComponent();
        component = component.withStyle(s -> s.withColor(color));
        return this;
    }

    public MutableComponent component() {
        assertComponent();
        return component;
    }

    public String string() {
        return component().getString();
    }

    public void addTo(List<? super MutableComponent> tooltip) {
        tooltip.add(component());
    }

    public void addTo(Consumer<? super MutableComponent> tooltip) {
        tooltip.accept(component());
    }

    @Environment(EnvType.CLIENT)
    public void forGoggles(List<? super MutableComponent> tooltip) {
        forGoggles(tooltip, 0);
    }

    @Environment(EnvType.CLIENT)
    public void forGoggles(List<? super MutableComponent> tooltip, int indents) {
        int spaceWidth = Minecraft.getInstance().font.width(" ");
        int count = DEFAULT_SPACE_WIDTH == spaceWidth ? 4 + indents : Mth.ceil(DEFAULT_SPACE_WIDTH * (4 + indents) / spaceWidth);
        tooltip.add(new LangBuilder(namespace).text(" ".repeat(count)).add(this).component());
    }

    private void assertComponent() {
        if (component == null)
            throw new IllegalStateException("No components were added to builder");
    }
}
