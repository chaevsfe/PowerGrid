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
package org.patryk3211.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.patryk3211.powergrid.electricity.light.string.StringLightCordRecipe;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StringLightCordRecipeTest {
    private static final String RECIPE_PATH = "/data/powergrid/recipe/crafting/light_cord_patterning.json";

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static JsonElement shippedRecipe() throws Exception {
        try (var stream = StringLightCordRecipeTest.class.getResourceAsStream(RECIPE_PATH)) {
            Assertions.assertNotNull(stream, RECIPE_PATH);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    @Test
    void testShippedRecipeUsesThisSerializer() throws Exception {
        var type = shippedRecipe().getAsJsonObject().get("type").getAsString();
        Assertions.assertEquals("powergrid:crafting_special_string_light_cord", type);
    }

    @Test
    void testDecodedRecipeEncodesForSync() throws Exception {
        var serializer = StringLightCordRecipe.SERIALIZER;
        var recipe = serializer.codec().codec().parse(JsonOps.INSTANCE, shippedRecipe()).getOrThrow();
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        Assertions.assertDoesNotThrow(() -> serializer.streamCodec().encode(buf, recipe));
        Assertions.assertSame(recipe, serializer.streamCodec().decode(buf));
        Assertions.assertEquals(0, buf.readableBytes());
    }
}
