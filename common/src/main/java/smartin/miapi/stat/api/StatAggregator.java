package smartin.miapi.stat.api;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StatAggregator {
    public static final PrioritizedEvent<AggregateStats> AGGREGATE_STATS_EVENT = PrioritizedEvent.createLoop();

    private StatAggregator() {

    }

    public static void register(
            StatGroup group,
            AggregateStats listener
    ) {
        AGGREGATE_STATS_EVENT.register((currentGroup, helper, baseItem, compareItem) -> {
            if (currentGroup == group) {
                listener.findStats(currentGroup, helper, baseItem, compareItem);
            }
        });
    }

    public static List<StatValue.StatWithValues<?, ?>> findAllStats(StatGroup group, ItemStack baseItem, ItemStack compareItem) {
        StatAggregateHelper helper = new StatAggregateHelper(group);
        AGGREGATE_STATS_EVENT.invoker().findStats(group, helper, baseItem, compareItem);
        return helper.asSortedCopy();
    }

    public static List<Tuple<StatGroup, List<StatValue.StatWithValues<?, ?>>>> findAllStats(ItemStack baseItem, ItemStack compareItem) {
        return StatGroup.STAT_GROUP_REGISTRY.getFlatMap().values().stream()
                .sorted(Comparator.comparingDouble(g -> g.priority))
                .map(group -> new Tuple<>(group, findAllStats(group, baseItem, compareItem)))
                .filter(tuple -> !tuple.getB().isEmpty())
                .toList();
    }

    public interface AggregateStats {
        void findStats(StatGroup group, StatAggregateHelper helper, ItemStack baseItem, ItemStack compareItem);
    }

    public static class StatAggregateHelper {
        private final List<StatValue.StatWithValues<?, ?>> stats = new ArrayList<>();
        public final StatGroup currentGroup;

        protected StatAggregateHelper(StatGroup group) {
            this.currentGroup = group;
        }

        public void addStat(StatValue.StatWithValues<?, ?> stat) {
            stats.add(stat);
        }

        public List<StatValue.StatWithValues<?, ?>> asCopy() {
            return List.copyOf(stats);
        }

        public List<StatValue.StatWithValues<?, ?>> asSortedCopy() {
            return stats.stream().sorted(Comparator.comparingDouble(a -> a.stat().metaData().getPriority())).toList();
        }

        public boolean remove(StatValue.StatWithValues<?, ?> stat) {
            return stats.remove(stat);
        }
    }
}
