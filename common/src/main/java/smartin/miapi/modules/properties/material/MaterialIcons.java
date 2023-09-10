package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.InterfaceDispatcher;
import com.redpxnda.nucleus.codec.MiscCodecs;
import com.redpxnda.nucleus.codec.PolyCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.Map;

public class MaterialIcons {
    public static final Map<String, MaterialIconCreator> iconCreators = new HashMap<>();
    private static final InterfaceDispatcher<MaterialIconCreator> dispatcher = InterfaceDispatcher.of(iconCreators, "type");
    public static MaterialIconCreator textureIconCreator;

    public static MaterialIcon getMaterialIcon(String materialKey, JsonElement element) {
        return dispatcher.dispatcher().create(element, materialKey);
    }

    public static void setup() {
        textureIconCreator = (element, mat) -> {
            if (!(element instanceof JsonObject object))
                throw new RuntimeException("JSON data for the icon of the '" + mat + "' material is not a JSON object! -> " + element);

            if (!(object.get("path") instanceof JsonPrimitive primitive) || !primitive.isString())
                throw new RuntimeException("'path' field for the icon of the '" + mat + "' material is either missing, or not a string! -> " + element);

            return new TextureMaterialIcon(new Identifier(primitive.getAsString()));
        };
        iconCreators.put("texture", textureIconCreator);

        Codec<ItemStack> byItem = Registries.ITEM.getCodec().xmap(Item::getDefaultStack, ItemStack::getItem);
        iconCreators.put("item", (element, mat) -> {
            if (!(element instanceof JsonObject object))
                throw new RuntimeException("JSON data for the icon of the '" + mat + "' material is not a JSON object! -> " + element);

            if (!object.has("item"))
                throw new RuntimeException("'item' field for the icon of the '" + mat + "' material is missing! -> " + element);

            ItemStack stack = MiscCodecs.quickParse(object.get("item"), PolyCodec.of(byItem, ItemStack.CODEC),
                    s -> Miapi.LOGGER.error("Failed to parse item for the icon of the '{}' material! -> {}", mat, s));
            int offset = object.get("offset") instanceof JsonPrimitive prim ? prim.getAsInt() : 16;

            return new ItemMaterialIcon(stack, offset);
        });
    }

    public interface MaterialIcon {
        /**
         * @param context draw context for use
         * @param x x pos of icon
         * @param y y pos of icon
         * @return amount to offset for text rendering
         */
        @Environment(EnvType.CLIENT)
        int render(DrawContext context, int x, int y);
    }
    public interface MaterialIconCreator {
        MaterialIcon create(JsonElement element, String materialKey);
    }

    public record TextureMaterialIcon(Identifier texture) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(DrawContext context, int x, int y) {
            context.drawTexture(texture, x, y, 0, 0, 16, 16, 16, 16);
            return 16;
        }
    }

    public record ItemMaterialIcon(ItemStack item, int offset) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(DrawContext context, int x, int y) {
            context.drawItem(item, x, y);
            return offset;
        }
    }
}
