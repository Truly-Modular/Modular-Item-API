package smartin.miapi.craft.stat;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import com.redpxnda.nucleus.util.PriorityMap;
import com.redpxnda.nucleus.util.PriorityMultiMap;
import dev.architectury.event.events.common.LifecycleEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Stat actor types are used for stat instances, controlling the way your stat instance behaves with other stat instances.
 */
public interface StatActorType {
    PriorityMap<StatActorType> TYPES = new PriorityMap<>();
    BiMap<String, StatActorType> REGISTERED = HashBiMap.create();

    /**
     * Use this event to register your own StatActorTypes.
     */
    PrioritizedEvent<ActorTypeCollectionEvent> EVENT = PrioritizedEvent.createLoop();

    /**
     * ANCHOR chooses the "best" stat instance in the list. (And if there's a previous step, it will just add it)
     */
    StatActorType ANCHOR = new StatActorType() {
        @Override
        public <T> T perform(CraftingStat<T> stat, Collection<T> actors, @Nullable T previous) {
            T current = null;
            for (T newInstance : actors) {
                if (current == null) current = newInstance;
                else current = stat.getBetter(current, newInstance);
            }

            return previous == null ? current : stat.add(previous, current);
        }
    };

    /**
     * ADD adds all the actors in the list. (And if there's a previous step, it will add that value on)
     */
    StatActorType ADD = new StatActorType() {
        @Override
        public <T> T perform(CraftingStat<T> stat, Collection<T> actors, @Nullable T previous) {
            T current = null;
            for (T newInstance : actors) {
                if (current == null) current = newInstance;
                else current = stat.add(current, newInstance);
            }

            return previous == null ? current : stat.add(previous, current);
        }
    };

    /**
     * MULTIPLY uses the sum of all the actors in the list and multiplies the previous value with it. (If there is no previous value, it will return that sum)
     */
    StatActorType MULTIPLY = new StatActorType() {
        @Override
        public <T> T perform(CraftingStat<T> stat, Collection<T> actors, @Nullable T previous) {
            if (previous == null) return null; // dont do anything if no previous

            T current = null;
            for (T newInstance : actors) {
                if (current == null) current = newInstance;
                else current = stat.add(current, newInstance);
            }

            return stat.multiply(previous, current);
        }
    };

    static void setup() {
        EVENT.register(rg -> {
            rg.register("anchor", ANCHOR, 0); // anchor is the first step
            rg.register("add", ADD, 5); // add is the second step
            rg.register("multiply", MULTIPLY, 10); // multiply is the third
        });

        LifecycleEvent.SETUP.register(() -> EVENT.invoker().registerAll((str, type, prio) -> {
            REGISTERED.put(str, type);
            TYPES.put(type, prio);
            TYPES.sort();
        }));
    }

    static <T> T evaluate(CraftingStat<T> stat, Map<StatActorType, PriorityMultiMap<T>> instances) {
        T previous = null;
        for (StatActorType type : TYPES.keySet()) {
            PriorityMultiMap<T> map = instances.get(type);
            if (map == null) continue;

            previous = type.perform(stat, map.keys(), previous);
        }

        return previous == null ? stat.getDefault() : previous;
    }

    /**
     * Merge the list of stat instances into one stat instance. (And accounting for the previous value.)
     *
     * @param stat     the crafting stat of the actors
     * @param actors   the actors to "merge"
     * @param previous the result of the previous step, null if this step is the first
     * @return the merged stat instance
     */
    <T> T perform(CraftingStat<T> stat, Collection<T> actors, @Nullable T previous);

    interface ActorTypeCollectionEvent {
        void registerAll(Registerer registerer);
    }
    interface Registerer {
        default void register(String key, StatActorType type) {
            register(key, type, 0);
        }
        void register(String key, StatActorType type, float prio);
    }
}
