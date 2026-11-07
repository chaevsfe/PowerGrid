package org.patryk3211.powergrid.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SimplePowerGridTrigger extends CriterionTriggerBase<SimplePowerGridTrigger.Instance> {
    public SimplePowerGridTrigger(String id) {
        super(id);
    }

    public void trigger(ServerPlayer player) {
        triggerFor(player, null);
    }

    public Instance instance() {
        return new Instance();
    }

    @NotNull
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public static class Instance extends CriterionTriggerBase.Instance {
        private static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(instance, player -> new Instance(player.orElse(null))));

        private final Holder<LootItemCondition> player;

        public Instance() {
            player = null;
        }

        public Instance(Holder<LootItemCondition> player) {
            this.player = player;
        }

        @Override
        protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
            return true;
        }

        @Override
        public Optional<Holder<LootItemCondition>> player() {
            return Optional.ofNullable(player);
        }
    }
}
