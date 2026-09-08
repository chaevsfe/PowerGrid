package org.patryk3211.powergrid.advancements;

import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;

import java.util.List;
import java.util.function.Supplier;

public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance> extends SimpleCriterionTrigger<T> {
    private final Identifier id;

    public CriterionTriggerBase(String id) {
        this.id = PowerGrid.asResource(id);
    }

    public Identifier getId() {
        return id;
    }

    protected void triggerFor(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
        trigger(player, instance -> instance.test(suppliers));
    }

    public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
    }
}
