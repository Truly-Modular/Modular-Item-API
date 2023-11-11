package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.AutoCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import smartin.miapi.client.gui.crafting.crafter.replace.HoverMaterialList;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.CodecBasedProperty;

import java.util.ArrayList;
import java.util.List;

public class LoreProperty extends CodecBasedProperty<LoreProperty.Holder> {
    public static final String KEY = "itemLore";
    public static final Codec<LoreProperty.Holder> codec = AutoCodec.of(LoreProperty.Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = new ArrayList<>();
    public static List<LoreSupplier> loreSuppliers = new ArrayList<>();

    public LoreProperty() {
        super(KEY, codec);

        property = this;
        loreSuppliers.add(itemStack -> {
            Material material = MaterialProperty.getMaterial(itemStack);
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
                        for (i = 1; i < material.getGroups().size(); i++) {
                            String groupId = material.getGroups().get(i);
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

    private Text gray(Text text) {
        return format(text, Formatting.GRAY);
    }

    private Text format(Text text, Formatting... formatting) {
        return text.getWithStyle(Style.EMPTY.withFormatting(formatting)).get(0);
    }

    @Environment(EnvType.CLIENT)
    public void appendLoreTop(List<Text> oldLore, ItemStack itemStack) {
        loreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        Holder holder = get(itemStack);
        if (holder != null && holder.position.equals("top")) {
            oldLore.addAll(Text.translatable(holder.lang).getSiblings());
        }
    }

    @Environment(EnvType.CLIENT)
    public void appendLoreBottom(List<Text> oldLore, ItemStack itemStack) {
        bottomLoreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        Holder holder = get(itemStack);
        if (holder != null && holder.position.equals("bottom")) {
            oldLore.addAll(Text.translatable(holder.lang).getSiblings());
        }
    }

    public static class Holder {
        String lang = "miapi.lore.empty";
        public String position = "top";
    }

    @Environment(EnvType.CLIENT)
    public interface LoreSupplier {
        List<Text> getLore(ItemStack itemStack);
    }
}
