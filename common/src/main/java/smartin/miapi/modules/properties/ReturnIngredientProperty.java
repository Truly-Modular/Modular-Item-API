package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.List;
import java.util.Map;

/**
 * Saves exactly one ingredient when applied and returns it on removal/replacement (if player != null).
 * Only the "given" ItemStack is persisted. Fallbacks and metadata are logic-only.
 */
public class ReturnIngredientProperty extends CodecProperty<ReturnIngredientProperty.StoredIngredientData> implements CraftingProperty {
    public static final String KEY = "return_ingredient_on_remove";
    public static ReturnIngredientProperty property;
    public static final Codec<StoredIngredientData> CODEC = AutoCodec.of(StoredIngredientData.class).codec();

    public ReturnIngredientProperty() {
        super(CODEC);
        property = this;
    }

    /**
     * The only saved data: a single consumed item.
     * Any fallbacks or metadata are runtime values only.
     */
    public static class StoredIngredientData {
        public List<ItemStack> given = List.of();
        public List<ItemStack> fallback = List.of();
        public @CodecBehavior.Optional boolean ingredient = true;
    }

    @Override
    public float getPriority() {
        return -10;
    }

    @Override
    public boolean shouldExecuteOnCraft(@Nullable ModuleInstance module, ModuleInstance root, ItemStack stack, CraftAction action) {
        return module != null && module.getProperty(this) != null;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(
            ItemStack old,
            ItemStack crafting,
            Player player,
            @Nullable ModularWorkBenchEntity bench,
            CraftAction action,
            ItemModule module,
            List<ItemStack> inventory,
            java.util.Map<net.minecraft.resources.ResourceLocation, com.google.gson.JsonElement> data
    ) {
        ModuleInstance removedModule = action.getModifyingModuleInstance(old);
        refund(removedModule, player);
        store(action.getModifyingModuleInstance(crafting), crafting, inventory);
        inventory.set(0, crafting);
        return CraftingProperty.super.performCraftAction(old, crafting, player, bench, action, module, inventory, data);
    }

    private void store(ModuleInstance instance, ItemStack crafted, List<ItemStack> inventory) {
        if (inventory.size() < 2 || inventory.get(1).isEmpty()) return;

        getData(instance).ifPresent(data -> {
            if (data.ingredient) {
                instance.moduleData.put(Miapi.id(KEY), ItemStack.CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, inventory.get(1).copy()).getOrThrow());
                instance.getRoot().writeToItem(crafted);
            }
        });
    }

    private void refund(ModuleInstance instance, Player player) {
        if (player == null) return;

        getData(instance).ifPresent(data -> {
            JsonElement element = instance.moduleData.get(Miapi.id(KEY));
            if (element != null) {
                player.addItem(ItemStack.CODEC.decode(JsonOpsBooleanPatched.INSTANCE, element).result().map(Pair::getFirst).orElse(ItemStack.EMPTY));
            } else {
                data.fallback.forEach(player::addItem);
            }
            data.given.forEach(player::addItem);
        });
    }

    @Override
    public StoredIngredientData merge(StoredIngredientData left, StoredIngredientData right, MergeType mergeType) {
        StoredIngredientData data = new StoredIngredientData();
        data.given = MergeAble.mergeList(left.given, right.given, mergeType);
        data.fallback = MergeAble.mergeList(left.fallback, right.fallback, mergeType).reversed();
        data.ingredient = MergeAble.decideLeftRight(left.ingredient, right.ingredient, mergeType);
        return data;
    }
}
