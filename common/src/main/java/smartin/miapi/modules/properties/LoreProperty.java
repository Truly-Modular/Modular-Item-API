package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.AutoCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.modules.properties.util.CodecBasedProperty;

import java.util.ArrayList;
import java.util.List;

public class LoreProperty extends CodecBasedProperty<LoreProperty.Holder> {
    public static final String KEY = "itemLore";
    public static final Codec<LoreProperty.Holder> codec = AutoCodec.of(LoreProperty.Holder.class).codec();
    public static LoreProperty property;
    public static List<LoreSupplier> loreSuppliers = new ArrayList<>();

    public LoreProperty() {
        super(KEY, codec);

        property = this;
    }

    @Environment(EnvType.CLIENT)
    public void appendLore(List<Text> oldLore, ItemStack itemStack) {
        loreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        Holder holder = get(itemStack);
        if(holder!=null){
            oldLore.addAll(Text.translatable(holder.lang).getSiblings());
        }
    }

    public static class Holder {
        String lang = "miapi.lore.empty";
    }

    @Environment(EnvType.CLIENT)
    public interface LoreSupplier {
        List<Text> getLore(ItemStack itemStack);
    }
}
