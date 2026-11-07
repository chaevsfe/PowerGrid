package org.patryk3211.powergrid.datafix;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

public class V4539 extends NamespacedSchema {
    public V4539(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public static void register(DataFixerBuilder builder) {
        Schema schema = builder.addSchema(4539, V4539::new);
        builder.addFixer(new AddNewChoices(schema, "Added Power Grid entities", References.ENTITY));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.registerSimple(map, "powergrid:hanging_wire");
        schema.registerSimple(map, "powergrid:block_wire");
        schema.registerSimple(map, "powergrid:cord");
        schema.registerSimple(map, "powergrid:string_light_cord");
        schema.registerSimple(map, "powergrid:zap_projectile");
        return map;
    }
}
