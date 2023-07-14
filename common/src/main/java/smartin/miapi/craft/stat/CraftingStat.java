package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * {@link CraftingStat}s are simple ways to require certain attributes, of which are provided by
 * items in the player's inventory or nearby blocks, in order for modules to be crafted.
 * Say, for example, I wanted to require the player to have some hammer item in their inventory if
 * they want to craft a specific module. And beyond that, have multiple tiers of hammers, or even
 * have blocks that can act as hammers and provide the stat. {@link CraftingStat}s are the way to
 * go about this.
 *
 * @see RequireCraftingStatProperty
 * @see ProvideCraftingStatProperty
 *
 * @param <T> the type of object this crafting stat represents. For example, if you want your hammers
 *            to save a simple double value, you'd set this type parameter to {@link Double}.
 *            (Or use a premade template, like {@link SimpleCraftingStat}
 */
public interface CraftingStat<T> {
    /**
     * A method used to save crafting stats to json.
     *
     * @param instance the instance of this CraftingStat's object type
     * @return the json representation of the instance
     */
    JsonElement writeToJson(T instance);

    /**
     * A method used to check if a module can be crafted when the current stat instance is the {@code instance} parameter.
     * @see CraftingProperty
     *
     * @param instance  the current instance of this crafting stat
     * @param expected  the expected instance of this crafting stat, defined by the module being crafted
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param bench     the workbench block entity (null on client)
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link CraftingProperty#getSlotPositions()}
     * @param buf       the writen buffer from {@link CraftingProperty#writeCraftingBuffer(PacketByteBuf, InteractAbleWidget)}
     * @return if the crafting can happen
     */
    default boolean canCraft(T instance, T expected, ItemStack old, ItemStack crafting, @Nullable ModularWorkBenchEntity bench, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        return instance.equals(expected);
    }

    /**
     * A method used to define some default stat instance value for this crafting stat.
     *
     * @return the default value of this stat
     */
    T getDefault();

    /**
     * A method used to merge two stat instances of this crafting stat.
     * Imagine you have two hammers in your inventory, this method will determine what to do with the provided
     * stat instances of those two hammers. In this example, as with most cases, you'll likely want to pick the
     * "higher" or "better" stat instance.
     *
     * @param bench   the workbench block entity (null on client)
     * @param old     the old stat instance
     * @param toMerge the stat instance being added on
     * @return
     */
    T merge(@Nullable ModularWorkBenchEntity bench, T old, T toMerge);

    /* crafting stat to stat instance map that enforces the instance is of the correct stat.
    Only create instances of this class with a complete wildcard as the type parameter, otherwise severe issues may arise.*/
    class Map<T> extends HashMap<CraftingStat<T>, T> {
        public static <T> void forEach(Map<?> map, BiConsumer<CraftingStat<T>, T> consumer) {
            map.forEach((k, v) -> {
                consumer.accept((CraftingStat<T>) k, (T) v);
            });
        }

        public <E> E get(CraftingStat<E> key) {
            return (E) super.get(key);
        }

        public <E> Map<T> set(CraftingStat<E> key, E val) {
            this.put((CraftingStat<T>) key, (T) val);
            return this;
        }

        public <E> E getOrDefault(CraftingStat<E> stat) {
            E val = get(stat);
            if (val == null) return stat.getDefault();
            return val;
        }
    }
}
