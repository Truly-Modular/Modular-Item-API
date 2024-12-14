package smartin.miapi.craft;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @header Blueprint Component
 * @path /components/blueprint
 * @description_start This Component allows the attachment of additional Craft Options for the crafting UI.
 * The Cost can be configured via the Ingredient in the data. Setting it to false will use the default module cost of the root module to be added.
 * Setting it to true will require whatever item this component is attached to.
 * It can also be set to an ingredient with count, in this case the inner data is a number, the amount of the ingredient needed and
 * the ingredient itself, following minecrafts default ingredient logic, like for recipes.
 * @description_end
 * @data module:a Module Instance, not a module, see the Module Component for more details
 * @data ingredient: This can either be a boolean or an ingredient with Count.
 * @data name: (Optional) This allows for a custom name in the Crafting UI.
 */
public class BlueprintComponent {
    public static Codec<BlueprintComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ModuleInstance.CODEC
                            .fieldOf("module")
                            .forGetter((blueprint) -> blueprint.toMerge),
                    Codec.either(Codec.BOOL, IngredientWithCount.INGREDIENT_WITH_COUNT)
                            .fieldOf("ingredient")
                            .forGetter((blueprint) -> blueprint.ingredient),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("name")
                            .forGetter(blueprintComponent -> blueprintComponent.name)
            ).apply(instance, BlueprintComponent::new
            ));
    public static ResourceLocation ID = Miapi.id("blueprint_slot_id");

    public static DataComponentType<BlueprintComponent> BLUEPRINT_COMPONENT = DataComponentType.<BlueprintComponent>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();

    public ModuleInstance toMerge;
    public Either<Boolean, IngredientWithCount> ingredient;
    public Optional<Component> name;

    public BlueprintComponent(ModuleInstance moduleInstance, Either<Boolean, IngredientWithCount> ingredient, Optional<Component> name) {
        this.toMerge = moduleInstance;
        this.ingredient = ingredient;
        this.name = name;
        //trigger property load to parse property data
        moduleInstance.getProperty(SlotProperty.getInstance());
    }

    public boolean useMaterialCrafting() {
        if (ingredient.left().isPresent()) {
            return !ingredient.left().get();
        } else {
            return false;
        }
    }

    public int getCost() {
        if (ingredient.left().isPresent() && ingredient.left().get()) {
            return 1;
        }
        if (ingredient.right().isPresent()) {
            IngredientWithCount countIngredient = ingredient.right().get();
            return (int) countIngredient.count;
        }
        return 0;
    }

    public boolean isValid(ItemStack itemStack, ItemStack blueprintItem) {
        if (ingredient.left().isPresent() && ingredient.left().get()) {
            return ItemStack.isSameItemSameComponents(itemStack, blueprintItem);
        }
        if (ingredient.right().isPresent()) {
            IngredientWithCount countIngredient = ingredient.right().get();
            return countIngredient.ingredient.test(itemStack) && countIngredient.count <= itemStack.getCount();
        }
        return false;
    }

    public boolean isValidCorrectType(ItemStack itemStack, ItemStack blueprintItem) {
        if (ingredient.left().isPresent() && ingredient.left().get()) {
            return ItemStack.isSameItemSameComponents(itemStack, blueprintItem);
        }
        if (ingredient.right().isPresent()) {
            IngredientWithCount countIngredient = ingredient.right().get();
            return countIngredient.ingredient.test(itemStack);
        }
        return false;
    }

    public ItemStack retrieve(CraftingScreenHandler screenHandler) {
        var optional = screenHandler.slots.stream()
                .filter(a -> a.getItem().has(BLUEPRINT_COMPONENT))
                .filter((a -> this.equals(a.getItem().get(BLUEPRINT_COMPONENT)))).findAny();
        return optional.map(Slot::getItem).orElse(ItemStack.EMPTY);
    }

    public void apply(ModuleInstance old) {
        old.module = this.toMerge.module;
        old.moduleID = this.toMerge.moduleID;
        old.moduleData = new HashMap<>(this.toMerge.moduleData);
        this.toMerge.getSubModuleMap().forEach(old::setSubModule);
    }

    public ItemStack adjustCost(ItemStack itemStack) {
        if (ingredient.left().isPresent()) {
            if (ingredient.left().get()) {
                int size = itemStack.getCount() - 1;
                ItemStack adjustedStack = itemStack.copy();
                adjustedStack.setCount(size);
                return adjustedStack;
            }
        } else {
            IngredientWithCount countIngredient = ingredient.right().get();
            if (countIngredient.ingredient.test(itemStack) && countIngredient.count <= itemStack.getCount()) {
                int size = itemStack.getCount() - (int) countIngredient.count;
                ItemStack adjustedStack = itemStack.copy();
                adjustedStack.setCount(size);
                return adjustedStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static BlueprintComponent getBlueprint(Map<ResourceLocation, JsonElement> dataMap, CraftingScreenHandler screenHandler) {
        JsonElement json = dataMap.get(ID);
        if (json != null) {
            var decodeResult = Codec.INT.decode(JsonOps.INSTANCE, json);
            int id = -1;
            if (decodeResult.isError()) {
                id = Integer.decode(Codec.STRING.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
            } else {
                id = decodeResult.getOrThrow().getFirst();
            }
            if (id == -1) {
                //check cursor stack somehow for blueprint
            }
            if (id >= 0) {
                ItemStack itemStack = screenHandler.slots.get(id).getItem();
                if (itemStack.has(BLUEPRINT_COMPONENT)) {
                    return itemStack.get(BLUEPRINT_COMPONENT);
                }
            } else {
                Miapi.LOGGER.error("could not correctly read blueprint data!");
            }
        }
        return BlueprintManager.getBlueprint(dataMap, screenHandler);
    }

    public Component getName() {
        return name.orElse(toMerge.getModuleName());
    }

    public CraftOption asCraftOption(CraftingScreenHandler screenHandler) {
        return new CraftOption(
                toMerge.module,
                () -> {
                    int i = -1;
                    var optional = screenHandler.slots.stream()
                            .filter(a -> a.getItem().has(BLUEPRINT_COMPONENT))
                            .filter((a -> this.equals(a.getItem().get(BLUEPRINT_COMPONENT)))).findAny();
                    if (optional.isPresent()) {
                        i = optional.get().index;
                    }
                    JsonElement element = Codec.INT.encodeStart(JsonOps.INSTANCE, i).getOrThrow();
                    JsonElement booleanElement = Codec.BOOL.encodeStart(JsonOps.INSTANCE, useMaterialCrafting()).getOrThrow();
                    return Map.of(
                            ID, element,
                            AllowedMaterial.KEY, booleanElement);
                },
                -200,
                getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlueprintComponent blueprintComponent) {
            String first = CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
            String other = CODEC.encodeStart(JsonOps.INSTANCE, blueprintComponent).getOrThrow().toString();
            return first.equals(other);
        }
        return super.equals(obj);
    }
}
