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
package org.patryk3211.powergrid.circuits.circuitboard;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.ComponentModels;
import org.patryk3211.powergrid.circuits.components.IRenderedComponent;
import org.patryk3211.powergrid.circuits.components.properties.Orientation;
import org.patryk3211.powergrid.circuits.schematic.Area;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.schematic.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.patryk3211.powergrid.circuits.schematic.CircuitLayer.GRID_TO_GRID_SCALE;

@Environment(EnvType.CLIENT)
public class CircuitBoardModel extends WrapperBlockStateModel {
    private static final ModelDebugName DEBUG_NAME = () -> "powergrid:circuit_board";
    private static final Material TRACE_MATERIAL = new Material(PowerGrid.asResource("block/circuit_board_trace"));
    private static final Material PAD_MATERIAL = new Material(PowerGrid.asResource("block/circuit_board_pad"));

    private static final float TRACE_HEIGHT = 2.05f;

    public static final int DESTROYED_TINT = 0;
    public static final int DESTROYED_COLOR = 0xFF404040;

    private final Map<Identifier, BlockStateModelPart> componentParts = new HashMap<>();

    private ModelBaker.Interner interner;
    private BakedQuad.MaterialInfo traceInfo;
    private BakedQuad.MaterialInfo padInfo;

    public CircuitBoardModel(BlockState state, BlockStateModel.UnbakedRoot wrapped) {
        super(state, wrapped);
    }

    public static BiFunction<BlockState, BlockStateModel.UnbakedRoot, BlockStateModel.UnbakedRoot> of() {
        return CircuitBoardModel::new;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        super.resolveDependencies(resolver);
        for(var id : ComponentModels.collectRawIds())
            resolver.markDependency(id);
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        var baked = super.bake(state, baker);

        interner = baker.interner();
        var materials = baker.materials();
        traceInfo = BakedQuad.MaterialInfo.of(materials.get(TRACE_MATERIAL, DEBUG_NAME), Transparency.NONE, CuboidFace.NO_TINT, false, 0);
        padInfo = BakedQuad.MaterialInfo.of(materials.get(PAD_MATERIAL, DEBUG_NAME), Transparency.NONE, CuboidFace.NO_TINT, false, 0);

        componentParts.clear();
        for(var id : ComponentModels.collectRawIds())
            componentParts.put(id, SimpleModelWrapper.bake(baker, id, BlockModelRotation.IDENTITY));

        return baked;
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        CircuitBoardBlockEntity circuit = null;
        if(level.getBlockEntity(pos) instanceof CircuitBoardBlockEntity be)
            circuit = be;

        CircuitBoardModelQuads cache = null;
        if(circuit != null) {
            cache = circuit.quads;
            if(cache == null) {
                cache = new CircuitBoardModelQuads();
                circuit.quads = cache;
            } else {
                var cached = cache.getParts(state);
                if(cached != null) {
                    parts.addAll(cached);
                    return;
                }
            }
        }

        var collected = new ArrayList<BlockStateModelPart>();
        super.addPartsWithInfo(level, pos, state, random, collected);

        if(circuit != null) {
            var schematic = circuit.getSchematic();
            if(schematic != null) {
                for(var placed : schematic.components())
                    addComponent(collected, placed);
                var overlay = new QuadCollection.Builder();
                for(var area : schematic.calculateAreas(CircuitSchematic.Layer.FRONT))
                    overlay.addUnculledFace(emitTrace(area));
                for(var pad : schematic.pads().calculatePoints())
                    overlay.addUnculledFace(emitPad(pad));
                var quads = overlay.build();
                if(!quads.getAll().isEmpty())
                    collected.add(new SimpleModelWrapper(quads, false, new Material.Baked(traceInfo.sprite(), false)));
            }
        }

        float angleX = CircuitBoardBlock.getAngleX(state);
        float angleY = CircuitBoardBlock.getAngleY(state);

        var rotated = new ArrayList<BlockStateModelPart>(collected.size());
        for(var part : collected)
            rotated.add(rotatePart(part, angleX, angleY));

        if(cache != null)
            cache.putParts(state, rotated);
        parts.addAll(rotated);
    }

    private void addComponent(List<BlockStateModelPart> out, PlacedComponent placed) {
        if(placed.component instanceof IRenderedComponent rendered && !rendered.emitBaked())
            return;
        var part = componentParts.get(ComponentModels.modelIdOf(placed));
        if(part == null)
            return;

        Quaternionf rotation = null;
        Vector3f correction = null;
        if(placed.has(Orientation.PROPERTY)) {
            var orientation = placed.get(Orientation.PROPERTY);
            var footprint = placed.component.footprint(placed);
            rotation = new Quaternionf().rotationY(orientation.ordinal() * (float) Math.PI * 0.5f);
            correction = switch(orientation) {
                case RIGHT -> new Vector3f();
                case DOWN -> new Vector3f(footprint.getOriginalHeight() / 16f, 0, 0);
                case LEFT -> new Vector3f(footprint.getOriginalWidth() / 16f, 0, footprint.getOriginalHeight() / 16f);
                case UP -> new Vector3f(0, 0, footprint.getOriginalWidth() / 16f);
            };
        }
        var offset = new Vector3f(placed.x / 16f, 2 / 16f, placed.y / 16f);

        var builder = new QuadCollection.Builder();
        for(var quad : part.getQuads(null))
            builder.addUnculledFace(placeQuad(quad, rotation, correction, offset, placed.destroyed));
        for(var direction : Direction.values()) {
            for(var quad : part.getQuads(direction))
                builder.addUnculledFace(placeQuad(quad, rotation, correction, offset, placed.destroyed));
        }
        var quads = builder.build();
        if(!quads.getAll().isEmpty())
            out.add(new SimpleModelWrapper(quads, part.useAmbientOcclusion(), part.particleMaterial()));
    }

    private static BakedQuad placeQuad(BakedQuad quad, Quaternionf rotation, Vector3f correction, Vector3f offset, boolean destroyed) {
        Direction direction = quad.direction();
        if(rotation != null) {
            var normal = new Vector3f(direction.getUnitVec3i().getX(), direction.getUnitVec3i().getY(), direction.getUnitVec3i().getZ());
            rotation.transform(normal);
            direction = Direction.getNearest(Math.round(normal.x), Math.round(normal.y), Math.round(normal.z), direction);
        }
        return new BakedQuad(
                placeVertex(quad.position0(), rotation, correction, offset),
                placeVertex(quad.position1(), rotation, correction, offset),
                placeVertex(quad.position2(), rotation, correction, offset),
                placeVertex(quad.position3(), rotation, correction, offset),
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                direction, destroyed ? destroyedInfo(quad.materialInfo()) : quad.materialInfo()
        );
    }

    private static BakedQuad.MaterialInfo destroyedInfo(BakedQuad.MaterialInfo info) {
        return new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), DESTROYED_TINT, info.shade(), info.lightEmission());
    }

    private static Vector3fc placeVertex(Vector3fc position, Quaternionf rotation, Vector3f correction, Vector3f offset) {
        var vertex = new Vector3f(position);
        if(rotation != null) {
            rotation.transform(vertex);
            vertex.add(correction);
        }
        vertex.add(offset);
        return vertex;
    }

    private static BlockStateModelPart rotatePart(BlockStateModelPart part, float angleX, float angleY) {
        if(angleX == 0 && angleY == 0)
            return part;
        var builder = new QuadCollection.Builder();
        for(var quad : part.getQuads(null))
            builder.addUnculledFace(rotateQuad(quad, angleX, angleY));
        for(var direction : Direction.values()) {
            for(var quad : part.getQuads(direction))
                builder.addUnculledFace(rotateQuad(quad, angleX, angleY));
        }
        return new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial());
    }

    private static BakedQuad rotateQuad(BakedQuad quad, float angleX, float angleY) {
        var normal = Vec3.atLowerCornerOf(quad.direction().getUnitVec3i());
        normal = VecHelper.rotate(normal, angleX, Direction.Axis.X);
        normal = VecHelper.rotate(normal, angleY, Direction.Axis.Y);
        var direction = Direction.getNearest((int) Math.round(normal.x), (int) Math.round(normal.y), (int) Math.round(normal.z), quad.direction());
        return new BakedQuad(
                rotateVertex(quad.position0(), angleX, angleY),
                rotateVertex(quad.position1(), angleX, angleY),
                rotateVertex(quad.position2(), angleX, angleY),
                rotateVertex(quad.position3(), angleX, angleY),
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                direction, quad.materialInfo()
        );
    }

    private static Vector3fc rotateVertex(Vector3fc position, float angleX, float angleY) {
        var vertex = new Vec3(position);
        vertex = VecHelper.rotateCentered(vertex, angleX, Direction.Axis.X);
        vertex = VecHelper.rotateCentered(vertex, angleY, Direction.Axis.Y);
        return vertex.toVector3f();
    }

    private BakedQuad emitTrace(Area area) {
        float x1 = (float) area.x1() / GRID_TO_GRID_SCALE;
        float y1 = (float) area.y1() / GRID_TO_GRID_SCALE;
        float x2 = (float) area.x2() / GRID_TO_GRID_SCALE;
        float y2 = (float) area.y2() / GRID_TO_GRID_SCALE;
        return bakeOverlay(x1, y1, x2, y2, traceInfo);
    }

    private BakedQuad emitPad(Point point) {
        float x1 = point.x(), y1 = point.y(), x2 = x1 + 1, y2 = y1 + 1;
        x1 /= GRID_TO_GRID_SCALE;
        x2 /= GRID_TO_GRID_SCALE;
        y1 /= GRID_TO_GRID_SCALE;
        y2 /= GRID_TO_GRID_SCALE;
        return bakeOverlay(x1, y1, x2, y2, padInfo);
    }

    private BakedQuad bakeOverlay(float x1, float y1, float x2, float y2, BakedQuad.MaterialInfo info) {
        return FaceBakery.bakeQuad(
                interner,
                new Vector3f(x1, TRACE_HEIGHT, y1),
                new Vector3f(x2, TRACE_HEIGHT, y2),
                new CuboidFace.UVs(x1, y1, x2, y2),
                Quadrant.R0,
                info,
                Direction.UP,
                BlockModelRotation.IDENTITY,
                null
        );
    }
}
