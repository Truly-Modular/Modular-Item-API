package smartin.miapi.modules.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public record Action(List<ActionPredicate> predicates, List<ActionEffect> effects) {
    public static Map<ResourceLocation, MapCodec<? extends ActionEffect>> ACTION_EFFECT_REGISTRY = new ConcurrentHashMap<>();
    public static Codec<ActionEffect> ACTION_CODEC = ResourceLocation.CODEC.dispatch("type",
            ActionEffect::getType,
            (id) -> ACTION_EFFECT_REGISTRY.get(id));

    public static Map<ResourceLocation, MapCodec<? extends ActionPredicate>> ACTION_PREDICATE_REGISTRY = new ConcurrentHashMap<>();
    public static Codec<ActionPredicate> ACTION_PREDICATE = ResourceLocation.CODEC.dispatch("type",
            ActionPredicate::getType,
            (id) -> ACTION_PREDICATE_REGISTRY.get(id));


    public Action initialize(ModuleInstance moduleInstance) {
        return new Action(
                predicates().stream().map(a -> a.initialize(moduleInstance)).toList(),
                effects().stream().map(a -> a.initialize(moduleInstance)).toList());
    }

    public void execute(ActionContext context) {
        List<ActionPredicate> pendingEffects = new ArrayList<>(effects);
        pendingEffects.addAll(predicates());

        boolean progressMade;
        do {
            progressMade = false;
            List<ActionPredicate> resolvedEffects = new ArrayList<>();

            for (ActionPredicate effect : pendingEffects) {
                boolean dependenciesResolved = effect.dependencyMet(context);

                if (dependenciesResolved) {
                    if (!effect.setup(context)) {
                        return;
                    }
                    resolvedEffects.add(effect);
                    progressMade = true;
                }
            }
            pendingEffects.removeAll(resolvedEffects);

        } while (progressMade && !pendingEffects.isEmpty());
        if (!pendingEffects.isEmpty()) {
            Miapi.LOGGER.error("COULD NOT EXECUTE ACTION, dependencies were not fullfilled " + pendingEffects.getFirst().dependency(context));
            return;
        }
        for (ActionEffect effect : effects) {
            effect.execute(context);
        }
    }
}
