package smartin.miapi.material.generated;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import smartin.miapi.blueprint.IngredientWithCount;
import smartin.miapi.material.CodecMaterial;
import smartin.miapi.modules.PropertyHolder;

import java.util.*;

public class MaterialHelper {

    public static CodecMaterial toCodecMaterial(GeneratedMaterial mat) {
        // 1. Icon JSON (client-only)
        Optional<JsonElement> iconJson = Optional.empty();
        if (mat.icon != null) {
            JsonObject iconObj = new JsonObject();
            iconObj.addProperty("type", "item");
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(mat.mainIngredient.getItem());
            iconObj.addProperty("item", itemId.toString());
            iconJson = Optional.of(iconObj);
        }

        // 2. Palette JSON (client-only)
        JsonObject paletteObj = new JsonObject();
        paletteObj.addProperty("type", "image_generated_item");
        paletteObj.addProperty("item", mat.mainIngredient.getItem().arch$registryName().toString());
        Optional<JsonElement> paletteJson = Optional.of(paletteObj);

        // 3. Dye palette JSON — not directly stored in GeneratedMaterial
        Optional<JsonElement> dyePaletteJson = Optional.empty();

        // 4. Groups
        List<String> groups = new ArrayList<>(mat.getGroups());
        List<String> hiddenGroups = List.of(); // depends on your logic
        List<String> guiGroups = List.of();    // depends on your logic

        // 5. Properties → convert to Map<String, JsonElement>
        Map<String, JsonElement> property = new HashMap<>();
        mat.properties.forEach((key, valueMap) -> {
            property.put(key, PropertyHolder.PROPERTY_MAP_CODEC.encodeStart(JsonOps.INSTANCE, valueMap).result().get());
        });
        Map<String, JsonElement> visualProperty = Map.of();
        Map<String, JsonElement> hiddenProperty = Map.of();

        // 6. Texture keys
        List<String> textureKeys = new ArrayList<>(mat.getTextureKeys());

        // 7. Incorrect for tool
        Optional<ResourceLocation> incorrectForToolId = Optional.empty();
        if (mat.getIncorrectBlocksForDrops() != null) {
            incorrectForToolId = Optional.of(mat.getIncorrectBlocksForDrops().location());
        }

        // 8. Translation
        Optional<Component> translation = Optional.ofNullable(mat.getTranslation());

        // 10. Items list
        List<IngredientWithCount> items = new ArrayList<>();
        items.add(new IngredientWithCount(Ingredient.of(mat.mainIngredient), 1));
        items.add(new IngredientWithCount(mat.ingredient, 1));

        // 11. generateConverters — unknown logic, leave empty
        Optional<Boolean> generateConverters = Optional.empty();

        // Build and return
        CodecMaterial codec = new CodecMaterial(
                iconJson,
                paletteJson,
                dyePaletteJson,
                groups,
                hiddenGroups,
                guiGroups,
                property,
                visualProperty,
                hiddenProperty,
                textureKeys,
                incorrectForToolId,
                translation,
                Optional.empty(),
                items,
                generateConverters
        );
        codec.doubleMap = new HashMap<>(mat.stats);
        return codec;
    }

}
