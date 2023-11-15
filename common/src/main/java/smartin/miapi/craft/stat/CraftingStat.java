package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.StatProvisionProperty;
import smartin.miapi.modules.properties.StatRequirementProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * {@link CraftingStat}s are simple ways to require certain attributes, of which are provided by
 * items in the player's inventory or nearby blocks, in order for modules to be crafted.
 * Say, for example, I wanted to require the player to have some hammer item in their inventory if
 * they want to craft a specific module. And beyond that, have multiple tiers of hammers, or even
 * have blocks that can act as hammers and provide the stat. {@link CraftingStat}s are the way to
 * go about this.
 *
 * @see StatRequirementProperty
 * @see StatProvisionProperty
 *
 * @param <T> the type of object this crafting stat represents. For example, if you want your hammers
 *            to save a simple double value, you'd set this type parameter to {@link Double}.
 *            (Or use a premade template, like {@link SimpleCraftingStat}
 */
public interface CraftingStat<T> {
    /**
     * A method used to save stat instances to json.
     *
     * @param instance the instance of this CraftingStat's object type
     * @return the json representation of the instance
     */
    JsonElement saveToJson(T instance);

    /**
     * A method to used to create stat instances from json.
     *
     * @param json           the json representation of the stat instance
     * @param moduleInstance the module instance of the model this stat is being applied to. Usually used for {@link StatResolver}
     * @return the stat instance created with the json
     */
    T createFromJson(JsonElement json, ItemModule.ModuleInstance moduleInstance);

    /**
     * A method used to save stat instances to nbt.
     *
     * @param instance the instance of this CraftingStat's object type
     * @return the nbt representation of the instance
     */
    NbtElement saveToNbt(T instance);

    /**
     * A method to used to create stat instances from nbt.
     *
     * @param nbt the nbt representation of the stat instance
     * @return the stat instance created with the nbt
     */
    T createFromNbt(NbtElement nbt);

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
     * @param data      A map for properties to send addtional Data
     * @return if the crafting can happen
     */
    default boolean canCraft(
            T instance,
            T expected,
            ItemStack old,
            ItemStack crafting,
            @Nullable ModularWorkBenchEntity bench,
            PlayerEntity player,
            ItemModule.ModuleInstance newModule,
            ItemModule module,
            List<ItemStack> inventory,
            Map<String,String> data) {
        return instance.equals(expected);
    }

    /**
     * A method used to define some default stat instance value for this crafting stat.
     *
     * @return the default value of this stat
     */
    T getDefault();

    /**
     * A method used to display a text representation of stat requirements.
     *
     * @param instance the stat instance that is being displayed
     * @return a text representation of the stat instance
     */
    Text asText(T instance);

    /**
     * A method used to merge two stat instances of this crafting stat.
     * Imagine you have two hammers in your inventory, this method will determine what to do with the provided
     * stat instances of those two hammers. In this example, as with most cases, you'll likely want to pick the
     * "higher" or "better" stat instance.
     *
     * @param bench   the workbench block entity (null on client)
     * @param old     the old stat instance
     * @param toMerge the stat instance being added on
     * @return the merged result
     */
    T merge(@Nullable ModularWorkBenchEntity bench, T old, T toMerge);

    /* crafting stat to stat instance map that enforces the instance is of the correct stat.
    Only create instances of this class with a complete wildcard as the type parameter, otherwise severe issues may arise.*/
    class StatMap<T> extends HashMap<CraftingStat<T>, T> {
        public static <T> void forEach(StatMap<?> map, BiConsumer<CraftingStat<T>, T> consumer) {
            if (map == null) return;
            map.forEach((k, v) -> consumer.accept((CraftingStat<T>) k, (T) v));
        }

        public <E> E get(CraftingStat<E> key) {
            return (E) super.get(key);
        }

        public <E> StatMap<T> set(CraftingStat<E> key, E val) {
            this.put((CraftingStat<T>) key, (T) val);
            return this;
        }

        public <E> E getOrDefault(CraftingStat<E> stat) {
            E val = get(stat);
            if (val == null) return stat.getDefault();
            return val;
        }

        public static class StatMapCodec implements Codec<StatMap<?>> {
            private final ItemModule.ModuleInstance modules;

            public StatMapCodec(ItemModule.ModuleInstance modules) {
                this.modules = modules;
            }

            @Override
            public <T> DataResult<Pair<StatMap<?>, T>> decode(DynamicOps<T> ops, T input) {
                StatMap<?> stats = new StatMap<>();
                MapLike<T> map = ops.getMap(input).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to create map in StatMapCodec! -> {}", s));
                map.entries().forEach(p -> {
                    String str = ops.getStringValue(p.getFirst()).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to getRaw string in StatMapCodec! -> {}", s));
                    CraftingStat stat = RegistryInventory.craftingStats.get(str);
                    T element = p.getSecond();
                    if (stat != null) {
                        if (element instanceof JsonElement json)
                            stats.set(stat, stat.createFromJson(json, modules));
                        else if (element instanceof NbtElement nbt)
                            stats.set(stat, stat.createFromNbt(nbt)); // nbt doesn't getRaw the modules cuz nbt is usually for syncing not parsing
                        else // hardcoding this codec nbt and json probably isn't the best thing to do, but smartin would prob getRaw mad if I forced the use of codecs in crafting stats
                            Miapi.LOGGER.warn("Only Json and Nbt deserialization is supported with the StatMapCodec!");
                    } // I could warn if the stat id is invalid, but optional compat is a thing and spamming console with invalid id warns is obnoxious af
                });

                return DataResult.success(Pair.of(stats, input));
            }

            @Override // i probably don't even need to define encoding cuz it will likely never be used, but i will anyways
            public <T> DataResult<T> encode(StatMap<?> input, DynamicOps<T> ops, T prefix) {
                Map<T, T> map = new HashMap<>();

                input.forEach((stat, inst) -> {
                    T obj;

                    if (ops instanceof JsonOps)
                        obj = (T) stat.saveToJson(inst);
                    else if (ops instanceof NbtOps)
                        obj = (T) stat.saveToNbt(inst);
                    else {
                        Miapi.LOGGER.warn("Only Json and Nbt serialization is supported with the StatMapCodec!");
                        throw new IllegalArgumentException();
                    }

                    map.put(ops.createString(RegistryInventory.craftingStats.findKey(stat)), obj);
                });

                return DataResult.success(ops.createMap(map));
            }
        }
    }
}
