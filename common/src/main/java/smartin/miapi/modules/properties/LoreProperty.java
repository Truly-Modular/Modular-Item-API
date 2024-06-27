package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.crafter.replace.HoverMaterialList;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This property manages the Itemlore of an Item
 */
public class LoreProperty implements ModuleProperty {
    public static final String KEY = "itemLore";
    public static final Codec<Holder> codec = AutoCodec.of(Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static List<ToolTipSupplierSupplier> loreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static Map<ItemStack, Material> materialLookupTable = Collections.synchronizedMap(new WeakHashMap<>());

    public LoreProperty() {
        super();
        property = this;
        loreSuppliers.add((ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) -> {
            if (itemStack.getItem() instanceof ModularItem) {
                tooltip.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
                getHolders(itemStack).stream().filter(h -> h.position.equals("top")).forEach(holder -> tooltip.add(holder.getText()));
            }
        });
    }

    public List<Holder> getHolders(ItemStack itemStack) {
        return getHolders(ItemModule.getMergedProperty(itemStack, property));
    }

    public List<Holder> getHolders(JsonElement element) {
        List<Holder> holders = new ArrayList<>();
        if (element != null) {
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(element1 -> holders.add(getFromSingleElement(element1)));
            } else {
                holders.add(getFromSingleElement(element));
            }
        }
        return holders.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (MergeType.OVERWRITE.equals(type)) {
            return ModuleProperty.mergeToList(old, toMerge);
        }

        return old;
    }

    private Holder getFromSingleElement(JsonElement element) {
        try {
            return codec.parse(JsonOps.INSTANCE, element).getOrThrow((Function<String, Throwable>) s -> new IllegalArgumentException("could not parse Lore Context" + s));
        } catch (Throwable e) {
            Miapi.LOGGER.error("", e);
            return new Holder();
        }
    }

    private Component gray(Component text) {
        return format(text, ChatFormatting.GRAY);
    }

    private Component format(Component text, ChatFormatting... formatting) {
        return text.toFlatList(Style.EMPTY.applyFormats(formatting)).get(0);
    }

    public void injectTooltipOnNonModularItems(List<Component> tooltip, ItemStack itemStack) {
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
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.INSTANCE.client.other.injectLoreWithoutGroup) {
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
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularItem) {
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.matches(converted, itemStack) || itemStack.getItem() instanceof ModularItem) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
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
        return lines;
    }

    List<Component> addToolTipsServer(ItemStack itemStack) {
        List<Component> lines = new ArrayList<>();
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.INSTANCE.client.other.injectLoreWithoutGroup) {
                        lines.add(gray(Component.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Component materialDesc = gray(Component.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                }
            }
        }
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularItem) {
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.matches(converted, itemStack) || itemStack.getItem() instanceof ModularItem) {
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
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }

    public static class Holder implements Comparable<Holder> {
        @CodecBehavior.Optional
        @Deprecated
        /**
         * @deprecated will be fully removed and replaced with the {@link Holder#text}
         */
        public String lang;
        @CodecBehavior.Optional
        public Component text;
        @CodecBehavior.Optional(false)
        public String position;
        @CodecBehavior.Optional
        public float priority = 0;

        public Component getText() {
            if (lang != null) {
                return Component.translatable(lang);
            }
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
