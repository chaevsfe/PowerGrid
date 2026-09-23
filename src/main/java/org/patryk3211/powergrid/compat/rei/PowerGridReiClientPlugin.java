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
package org.patryk3211.powergrid.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableEditScreen;

@Environment(EnvType.CLIENT)
public final class PowerGridReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDecider(new CircuitEditorBounds());
    }

    private static Rectangle rectangle(Rect2i area) {
        return new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    private static final class CircuitEditorBounds implements DisplayBoundsProvider<CircuitDesignTableEditScreen<?>> {
        @Override
        public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
            return CircuitDesignTableEditScreen.class.isAssignableFrom(screen);
        }

        @Override
        public Rectangle getScreenBounds(CircuitDesignTableEditScreen<?> screen) {
            Rectangle bounds = rectangle(screen.getWindowArea());
            for(Rect2i area : screen.getExtraAreas())
                bounds = bounds.union(rectangle(area));
            return bounds;
        }
    }
}
