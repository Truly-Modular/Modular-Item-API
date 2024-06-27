package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.StatProvisionProperty;
import smartin.miapi.modules.properties.StatRequirementProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;

import java.util.List;
import java.util.Map;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
    T createFromJson(JsonElement json, ModuleInstance moduleInstance);

    /**
     * A method used to save stat instances to nbt.
     *
     * @param instance the instance of this CraftingStat's object type
     * @return the nbt representation of the instance
     */
    Tag saveToNbt(T instance);

    /**
     * A method to used to create stat instances from nbt.
     *
     * @param nbt the nbt representation of the stat instance
     * @return the stat instance created with the nbt
     */
    T createFromNbt(Tag nbt);

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
            Player player,
            ModuleInstance newModule,
            ItemModule module,
            List<ItemStack> inventory,
            Map<String,String> data) {
        return instance.equals(getBetter(instance, expected));
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
    Component asText(T instance);

    /**
     * A method used to getVertexConsumer the "better" of two stat instances.
     * If the first and second are equal, typically return the first. (If it matters)
     *
     * @param first  the first stat instance
     * @param second the second stat instance
     * @return the value which is "higher" or "better"
     */
    T getBetter(T first, T second);

    /**
     * Multiply the two parameters, and return the result. <br>
     * Note: in some instances, you may not be able to multiply with this stat, because there simply is no way to do that.
     * In those circumstances, just return the second or first. This stat will likely never be used in multiplication anyway.
     * @param first  the left operand
     * @param second the right operand
     * @return the multiplied result
     */
    T multiply(T first, T second);

    /**
     * Add the two parameters, and return the result. <br>
     * Note: in some instances, you may not be able to add with this stat, because there simply is no way to do that.
     * In those circumstances, just return the second or first. This stat will likely never be used in addition anyway.
     * @param first  the left operand
     * @param second the right operand
     * @return the summed result
     */
    T add(T first, T second);

}
