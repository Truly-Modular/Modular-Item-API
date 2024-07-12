package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
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
import smartin.miapi.client.gui.crafting.crafter.replace.HoverMaterialList;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

/**
 * This property manages the Itemlore of an Item
 */
public class LoreProperty extends CodecProperty<List<LoreProperty.Holder>> {
    public static final String KEY = "itemLore";
    public static final Codec<Holder> codec = AutoCodec.of(Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static List<ToolTipSupplierSupplier> loreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static Map<ItemStack, Material> materialLookupTable = Collections.synchronizedMap(new WeakHashMap<>());

    public LoreProperty() {
        super(Codec.list(codec));
        property = this;
        loreSuppliers.add((ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) -> {
            if (itemStack.getItem() instanceof ModularItem) {
                tooltip.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
                getHolders(itemStack).stream().filter(h -> h.position.equals("top")).forEach(holder -> tooltip.add(holder.getText()));
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
