package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    public static final Codec<LoreProperty.Holder> codec = AutoCodec.of(LoreProperty.Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static List<ToolTipSupplierSupplier> loreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static Map<ItemStack, Material> materialLookupTable = Collections.synchronizedMap(new WeakHashMap<>());

    public LoreProperty() {
        super();
        property = this;
        loreSuppliers.add((ItemStack itemStack, List<Text> tooltip, Item.TooltipContext context, TooltipType tooltipType) -> {
            if (itemStack.getItem() instanceof ModularItem) {
                tooltip.add(format(Text.translatable("miapi.ui.modular_item"), Formatting.GRAY));
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

    private Text gray(Text text) {
        return format(text, Formatting.GRAY);
    }

    private Text format(Text text, Formatting... formatting) {
        return text.getWithStyle(Style.EMPTY.withFormatting(formatting)).get(0);
    }

    public void injectTooltipOnNonModularItems(List<Text> tooltip, ItemStack itemStack) {
        synchronized (property) {
            if (smartin.miapi.Environment.isClient()) {
                tooltip.addAll(addToolTipsClient(itemStack));
            } else {
                tooltip.addAll(addToolTipsServer(itemStack));
            }

        }
    }

    @Environment(EnvType.CLIENT)
    List<Text> addToolTipsClient(ItemStack itemStack) {
        List<Text> lines = new ArrayList<>();
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.INSTANCE.client.other.injectLoreWithoutGroup) {
                        lines.add(gray(Text.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Text materialDesc = gray(Text.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                    if (smartin.miapi.Environment.isClient()) {
                        lines.addAll(getAltClient(material));
                    }
                }
            }
        }
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularItem) {
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.areEqual(converted, itemStack) || itemStack.getItem() instanceof ModularItem) {
                lines.add(format(Text.translatable("miapi.ui.modular_item"), Formatting.GRAY));
            }
        }
        return lines;
    }

    @Environment(EnvType.CLIENT)
    List<Text> getAltClient(Material material) {
        List<Text> lines = new ArrayList<>();
        if (net.minecraft.client.gui.screen.Screen.hasAltDown()) {
            lines.add(gray(Text.translatable("miapi.ui.material_desc_alt_2")));
            for (int i = 1; i < material.getGuiGroups().size(); i++) {
                String groupId = material.getGuiGroups().get(i);
                lines.add(gray(Text.literal(" - " + HoverMaterialList.getTranslation(groupId).getString())));
            }
        }
        return lines;
    }

    List<Text> addToolTipsServer(ItemStack itemStack) {
        List<Text> lines = new ArrayList<>();
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.INSTANCE.client.other.injectLoreWithoutGroup) {
                        lines.add(gray(Text.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Text materialDesc = gray(Text.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                }
            }
        }
        if (MiapiConfig.INSTANCE.client.other.injectLoreModularItem) {
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.areEqual(converted, itemStack) || itemStack.getItem() instanceof ModularItem) {
                lines.add(format(Text.translatable("miapi.ui.modular_item"), Formatting.GRAY));
            }
        }
        return lines;
    }

    public static void appendLoreTop(ItemStack stack, List<Text> tooltip, Item.TooltipContext context, TooltipType tooltipType) {
        loreSuppliers.forEach(supplierSupplier -> supplierSupplier.getLore(stack, tooltip, context, tooltipType));
    }

    public void appendLoreBottom(List<Text> oldLore, ItemStack itemStack) {
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
        public Text text;
        @CodecBehavior.Optional(false)
        public String position;
        @CodecBehavior.Optional
        public float priority = 0;

        public Text getText() {
            if (lang != null) {
                return Text.translatable(lang);
            }
            if (text != null) {
                return text;
                //return Codecs.TEXT.parse(JsonOps.INSTANCE, text).result().orElse(Text.empty());
            }
            return Text.empty();
        }

        @Override
        public int compareTo(@NotNull Holder o) {
            return Float.compare(priority, o.priority);
        }
    }

    //@Environment(EnvType.CLIENT)
    public interface LoreSupplier {
        List<Text> getLore(ItemStack itemStack);
    }

    public interface ToolTipSupplierSupplier {
        void getLore(ItemStack itemStack, List<Text> tooltip, Item.TooltipContext context, TooltipType tooltipType);
    }
}
