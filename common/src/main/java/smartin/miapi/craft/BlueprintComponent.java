package smartin.miapi.craft;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.AllowedMaterial;

import java.awt.*;
import java.util.Map;

public class BlueprintComponent {
    public static Codec<BlueprintComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ModuleInstance.CODEC
                            .fieldOf("module")
                            .forGetter((blueprint) -> blueprint.toMerge),
                    Codec.either(Codec.BOOL, IngredientWithCount.INGREDIENT_WITH_COUNT)
                            .fieldOf("ingredient")
                            .forGetter((blueprint) -> blueprint.ingredient)
            ).apply(instance, BlueprintComponent::new
            ));
    public static ResourceLocation ID = Miapi.id("blueprint_slot_id");

    public static DataComponentType<BlueprintComponent> BLUEPRINT_COMPONENT = DataComponentType.<BlueprintComponent>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();

    static {
        ReplaceView.optionSuppliers.add(option ->
                option.getScreenHandler().slots
                        .stream()
                        .filter(a -> a.getItem().has(BLUEPRINT_COMPONENT))
                        .map(a -> a.getItem().get(BLUEPRINT_COMPONENT).asCraftOption(option.getScreenHandler())).toList());
    }

    public ModuleInstance toMerge;
    public Either<Boolean, IngredientWithCount> ingredient;

    public BlueprintComponent(ModuleInstance moduleInstance, Either<Boolean, IngredientWithCount> ingredient) {
        this.toMerge = moduleInstance;
        this.ingredient = ingredient;
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
            return countIngredient.count;
        }
        return 0;
    }

    public boolean isValid(ItemStack itemStack, ItemStack blueprintItem) {
        if (ingredient.left().isPresent() && ingredient.left().get()) {
            return ItemStack.isSameItemSameComponents(itemStack, blueprintItem);
        }
        if (ingredient.right().isPresent()) {
            IngredientWithCount countIngredient = ingredient.right().get();
            return countIngredient.ingredient.test(itemStack) && countIngredient.count >= itemStack.getCount();
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
        old.moduleData = this.toMerge.moduleData;
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
            if (countIngredient.ingredient.test(itemStack) && countIngredient.count >= itemStack.getCount()) {
                int size = itemStack.getCount() - countIngredient.count;
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
            if(id==-1){
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
        return null;
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
                -100,
                Component.literal("testing").withColor(Color.CYAN.getRGB()));
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
