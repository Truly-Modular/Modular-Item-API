package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.crafter.replace.HoverMaterialList;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.generated.SmithingRecipeUtil;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

/**
 * @header Lore Property
 * @path /data_types/properties/item_lore
 * @description_start The LoreProperty manages the lore (or descriptive text) of an item. This property allows items to display custom lore
 * that can be added either at the top or bottom of the item's tooltip. The lore is defined as a list of {@link Holder} objects,
 * each specifying the text, position, and priority of the lore entry.
 * <p>
 * The lore can be customized based on whether the item is modular or not, and additional configurations are available
 * through {@link MiapiConfig}. Depending on the environment (client or server), different lore might be injected.
 * @description_end
 * @data a list of Lore Entires, compromoised of:
 * @data text: The {@link Component} text to display.
 * @data position: The position of the lore ("top" or "bottom").
 * @data priority: The priority of the lore entry, used for sorting.
 */

public class LoreProperty extends CodecProperty<List<LoreProperty.Holder>> {
    public static final ResourceLocation KEY = Miapi.id("item_lore");
    public static final Codec<Holder> codec = AutoCodec.of(Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static List<ToolTipSupplierSupplier> loreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static Map<ItemStack, Material> materialLookupTable = Collections.synchronizedMap(new WeakHashMap<>());
    public static Map<Item, List<Component>> smithingTemplate = Collections.synchronizedMap(new WeakHashMap<>());

    public LoreProperty() {
        super(Codec.list(codec));
        property = this;
        loreSuppliers.add((ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) -> {
            if (ModularItem.isModularItem(itemStack)) {
                tooltip.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
                getHolders(itemStack).stream().filter(h -> h.position.equals("top")).forEach(holder -> tooltip.add(holder.getText()));
            }
        });
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            try {
                var recipeManager = SmithingRecipeUtil.findManager(isClient);
                if (recipeManager != null) {
                    recipeManager.getAllRecipesFor(RecipeType.SMITHING).forEach(recipeHolder -> {
                        if (recipeHolder.value() instanceof MaterialSmithingRecipe smithingRecipe) {
                            smithingTemplate.put(smithingRecipe.smithingTemplate.getItems()[0].getItem(), new ArrayList<>());
                            List<Component> list = smithingTemplate.getOrDefault(smithingRecipe.smithingTemplate.getItems()[0].getItem(), new ArrayList<>());
                            Material ingredient = MaterialProperty.materials.get(smithingRecipe.startMaterial);
                            Material target = MaterialProperty.materials.get(smithingRecipe.resultMaterial);
                            if (ingredient != null && target != null) {
                                Component materialDescription = Component.translatable("miapi.material_template.smithing", ingredient.getTranslation().getString(), target.getTranslation().getString()).withStyle(ChatFormatting.GRAY);
                                list.add(materialDescription);
                            }
                        }
                    });
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not setup smithing lore injection", e);
            }
        });
    }

    public List<Holder> getHolders(ItemStack itemStack) {
        return getData(itemStack).orElse(new ArrayList<>());
    }

    private Component gray(Component text) {
        return format(text, ChatFormatting.GRAY);
    }

    private Component format(Component text, ChatFormatting... formatting) {
        return text.toFlatList(Style.EMPTY.applyFormats(formatting)).get(0);
    }

    public void injectTooltipOnNonModularItems(List<Component> tooltip, ItemStack itemStack) {
        if(!smartin.miapi.Environment.isClient() && MiapiConfig.INSTANCE.server.other.serverLoreInjection){
            return;
        }
        synchronized (property) {
            if (smartin.miapi.Environment.isClient()) {
                tooltip.addAll(addToolTipsClient(itemStack));
            } else {
                tooltip.addAll(addToolTipsServer(itemStack));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    List<Component> addToolTipsClient(ItemStack itemStack) {
        List<Component> lines = new ArrayList<>();
        if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreWithoutGroup) {
                        lines.add(gray(Component.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Component materialDesc = gray(Component.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                    if (smartin.miapi.Environment.isClient()) {
                        lines.addAll(getAltClient(material));
                    }
                }
            }
        }
        if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreModularItem) {
            if (ModularItem.isModularItem(itemStack)) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
                return lines;
            }
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.matches(converted, itemStack) && ModularItem.isModularItem(converted)) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
            }
        }
        if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreModularTemplate) {
            var description = smithingTemplate.get(itemStack.getItem());
            if (description != null) {
                lines.add(Component.translatable("miapi.material_template.smithing.header").withStyle(ChatFormatting.GRAY));
                lines.addAll(description);
            }
        }
        return lines;
    }

    @Environment(EnvType.CLIENT)
    List<Component> getAltClient(Material material) {
        List<Component> lines = new ArrayList<>();
        if (net.minecraft.client.gui.screens.Screen.hasAltDown()) {
            lines.add(gray(Component.translatable("miapi.ui.material_desc_alt_2")));
            for (int i = 1; i < material.getGuiGroups().size(); i++) {
                String groupId = material.getGuiGroups().get(i);
                lines.add(gray(Component.literal(" - " + HoverMaterialList.getTranslation(groupId).getString())));
            }
        }
        lines.addAll(material.getDescription());
        return lines;
    }

    List<Component> addToolTipsServer(ItemStack itemStack) {
        List<Component> lines = new ArrayList<>();
        if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreWithoutGroup) {
                        lines.add(gray(Component.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Component materialDesc = gray(Component.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                }
            }
        }
        if (MiapiConfig.INSTANCE.client.loreConfig.injectLoreModularItem) {
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.matches(converted, itemStack) || ModularItem.isModularItem(itemStack)) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    public static void appendLoreTop(ItemStack stack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) {
        loreSuppliers.forEach(supplierSupplier -> supplierSupplier.getLore(stack, tooltip, context, tooltipType));
    }

    public void appendLoreBottom(List<Component> oldLore, ItemStack itemStack) {
        bottomLoreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        getHolders(itemStack).stream().filter(h -> h.position.equals("bottom")).forEach(holder -> oldLore.add(holder.getText()));
    }

    @Override
    public List<Holder> merge(List<Holder> left, List<Holder> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

    public static class Holder implements Comparable<Holder> {
        @CodecBehavior.Optional
        public Component text;
        @CodecBehavior.Optional(false)
        public String position;
        @CodecBehavior.Optional
        public float priority = 0;

        public Component getText() {
            if (text != null) {
                return text;
                //return Codecs.TEXT.parse(JsonOps.INSTANCE, text).result().orElse(Text.empty());
            }
            return Component.empty();
        }

        @Override
        public int compareTo(@NotNull Holder o) {
            return Float.compare(priority, o.priority);
        }
    }

    //@Environment(EnvType.CLIENT)
    public interface LoreSupplier {
        List<Component> getLore(ItemStack itemStack);
    }

    public interface ToolTipSupplierSupplier {
        void getLore(ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType);
    }
}
