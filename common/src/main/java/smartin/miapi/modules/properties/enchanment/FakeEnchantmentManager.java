package smartin.miapi.modules.properties.enchanment;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import smartin.miapi.item.modular.VisualModularItem;

import java.util.*;

public class FakeEnchantmentManager {
    public static Map<ItemEnchantments, ItemStack> lookupMap = new WeakHashMap<>();
    public static List<LevelTransformer> transformerList = new ArrayList<>();
    public static final PrioritizedEvent<AddEnchantmentEvent> ADD_ENCHANTMENT = PrioritizedEvent.createEventResult();

    public static Set<Object2IntMap.Entry<Holder<Enchantment>>> adjustEnchantments(Set<Object2IntMap.Entry<Holder<Enchantment>>> immutableMap, ItemStack itemStack) {
        EnchantmentMap enchantmentMap = new EnchantmentMap(immutableMap, itemStack);
        ADD_ENCHANTMENT.invoker().adjust(enchantmentMap);
        Map<Holder<Enchantment>, Integer> map = new HashMap<>();
        enchantmentMap.immutableMap.forEach(holderEntry -> {
            map.put(holderEntry.getKey(), holderEntry.getIntValue());
        });
        enchantmentMap.enchantments.forEach(enchantmentHolder -> {
            if (!map.containsKey(enchantmentHolder)) {
                map.put(enchantmentHolder, 0);
            }
        });
        Object2IntOpenHashMap<Holder<Enchantment>> resultMap = new Object2IntOpenHashMap<>();
        map.forEach((enchantmentHolder, integer) -> {
            int level = integer;
            for (LevelTransformer levelTransformer : transformerList) {
                level = levelTransformer.adjust(enchantmentHolder, itemStack, level);
            }
            resultMap.put(enchantmentHolder, level);
        });
        return resultMap.object2IntEntrySet();
    }

    public static int adjustLevel(Holder<Enchantment> enchantmentHolder, int prevLevel, ItemStack itemStack) {
        for (LevelTransformer levelTransformer : transformerList) {
            prevLevel = levelTransformer.adjust(enchantmentHolder, itemStack, prevLevel);
        }
        return prevLevel;
    }

    public static void initOnItemStack(ItemStack stack) {
        ItemEnchantments enchantments = stack.getComponents().get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && (stack.getItem() instanceof VisualModularItem)) {
            lookupMap.put(enchantments, stack);
        }
    }

    public static class EnchantmentMap {
        public Set<Object2IntMap.Entry<Holder<Enchantment>>> immutableMap;
        public List<Holder<Enchantment>> enchantments = new ArrayList<>();
        public ItemStack referenceStack;

        public EnchantmentMap(Set<Object2IntMap.Entry<Holder<Enchantment>>> immutableMap, ItemStack itemStack) {
            this.immutableMap = immutableMap;
            referenceStack = itemStack;
            immutableMap.forEach(holderEntry -> enchantments.add(holderEntry.getKey()));
        }
    }

    public interface AddEnchantmentEvent {
        EventResult adjust(EnchantmentMap enchantmentMap);
    }

    public interface LevelTransformer {
        int adjust(Holder<Enchantment> enchantmentHolder, ItemStack itemStack, int oldLevel);
    }
}
