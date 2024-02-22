package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.crafter.replace.HoverMaterialList;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * This property manages the Itemlore of an Item
 */
public class LoreProperty implements ModuleProperty {
    public static final String KEY = "itemLore";
    public static final Codec<LoreProperty.Holder> codec = AutoCodec.of(LoreProperty.Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = new ArrayList<>();
    public static List<LoreSupplier> loreSuppliers = new ArrayList<>();
    public static Map<ItemStack, Material> materialLookupTable = new WeakHashMap<>();

    public LoreProperty() {
        super();
        property = this;
        loreSuppliers.add(itemStack -> {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            List<Text> descriptions = new ArrayList<>();
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    descriptions.add(gray(Text.translatable("miapi.ui.material_desc")));
                } else {
                    Text materialDesc = gray(Text.translatable("miapi.ui.material_desc_alt"));
                    descriptions.add(materialDesc);
                    if (Screen.hasAltDown()) {
                        descriptions.add(gray(Text.translatable("miapi.ui.material_desc_alt_2")));
                        for (i = 1; i < material.getGuiGroups().size(); i++) {
                            String groupId = material.getGuiGroups().get(i);
                            descriptions.add(gray(Text.literal(" - " + HoverMaterialList.getTranslation(groupId).getString())));
                        }
                    }
                }
            }
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.areEqual(converted, itemStack) || itemStack.getItem() instanceof ModularItem) {
                descriptions.add(format(Text.translatable("miapi.ui.modular_item"), Formatting.GRAY));
            }
            return descriptions;
        });
    }

    public List<Holder> getHolders(ItemStack itemStack) {
        return getHolders(ItemModule.getMergedProperty(itemStack, property));
    }

    public List<Holder> getHolders(JsonElement element) {
        List<Holder> holders = new ArrayList<>();
        if (element != null) {
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(element1 -> {
                    holders.add(getFromSingleElement(element1));
                });
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
        return codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {
            Miapi.LOGGER.error("Failed to decode using codec during cache creation for a CodecBasedProperty! -> " + s);
        });
    }

    private Text gray(Text text) {
        return format(text, Formatting.GRAY);
    }

    private Text format(Text text, Formatting... formatting) {
        return text.getWithStyle(Style.EMPTY.withFormatting(formatting)).get(0);
    }

    @Environment(EnvType.CLIENT)
    public void appendLoreTop(List<Text> oldLore, ItemStack itemStack) {
        loreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        getHolders(itemStack).stream().filter(h -> h.position.equals("top")).forEach(holder -> {
            oldLore.add(holder.getText());
        });
    }

    @Environment(EnvType.CLIENT)
    public void appendLoreBottom(List<Text> oldLore, ItemStack itemStack) {
        bottomLoreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        getHolders(itemStack).stream().filter(h -> h.position.equals("bottom")).forEach(holder -> {
            oldLore.add(holder.getText());
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }

    public static class Holder implements Comparable<Holder> {
        @AutoCodec.Optional
        @Deprecated
        /**
         * @deprecated will be fully removed and replaced with the {@link Holder#text}
         */
        public String lang;
        @AutoCodec.Optional
        public Text text;
        @AutoCodec.Mandatory
        public String position;
        @AutoCodec.Optional
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

    @Environment(EnvType.CLIENT)
    public interface LoreSupplier {
        List<Text> getLore(ItemStack itemStack);
    }
}
