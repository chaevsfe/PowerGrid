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

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;

public record TallBlockRenderState(Matrix3x2f pose, BlockState state, int x0, int y0, int extraHeight, ScreenRectangle bounds) implements PictureInPictureRenderState {
    public static final int SIZE = 27;

    public TallBlockRenderState(Matrix3x2f pose, BlockState state, int x0, int y0, int extraHeight) {
        this(pose, state, x0, y0, extraHeight, new ScreenRectangle(x0, y0, SIZE, SIZE + extraHeight).transformMaxBounds(pose));
    }

    @Override
    public int x1() {
        return x0 + SIZE;
    }

    @Override
    public int y1() {
        return y0 + SIZE + extraHeight;
    }

    @Override
    public float scale() {
        return 20.0f;
    }

    @Override
    public ScreenRectangle scissorArea() {
        return null;
    }
}
