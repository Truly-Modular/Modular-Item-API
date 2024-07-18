package smartin.miapi.modules.material.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialIcons;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.material.palette.FallbackColorer;
import smartin.miapi.modules.material.palette.GrayscalePaletteColorer;
import smartin.miapi.modules.material.palette.MaterialRenderController;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.FakeTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratedMaterialFromCopy implements Material {
    ItemStack mainIngredient;
    String key;
    List<String> groups = new ArrayList<>();
    List<String> textureKeys;
    Map<String, Double> stats;
    TagKey<Block> incorrectForTool;
    GrayscalePaletteColorer palette;
    @Nullable
    MaterialIcons.MaterialIcon icon;
    Material source;

    public static Codec<GeneratedMaterialFromCopy> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ItemStack.CODEC
                            .fieldOf("ingredient")
                            .forGetter(m -> m.mainIngredient),
                    Codec.STRING
                            .fieldOf("material").
                            forGetter(m -> m.source.getKey())
            ).apply(instance, (itemstack, material) -> {
                return new GeneratedMaterialFromCopy(itemstack, MaterialProperty.materials.get(material));
            }));


    /**
     * generates a {@link Material} from a {@link Tier} and copies the stats from the "other" material
     *
     * @param mainIngredient the main {@link ItemStack} that is the source for the {@link Tier}
     * @param other          the {@link Material} the stats are copied from
     */
    public GeneratedMaterialFromCopy(ItemStack mainIngredient, Material other) {
        key = "generated_" + mainIngredient.getDescriptionId();
        this.source = other;
        this.mainIngredient = mainIngredient;
        groups.add(key);
        groups.addAll(other.getGroups());
        textureKeys = List.of("default");
        stats.put("hardness", other.getDouble("hardness"));
        stats.put("density", other.getDouble("density"));
        stats.put("flexibility", other.getDouble("flexibility"));
        stats.put("durability", other.getDouble("durability"));
        stats.put("mining_level", other.getDouble("mining_level"));
        stats.put("mining_speed", other.getDouble("mining_speed"));
        stats.put("axe_damage", other.getDouble("axe_damage"));
        stats.put("enchantability", other.getDouble("enchantability"));
        stats.put("armor_durability_offset", other.getDouble("armor_durability_offset"));
        MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(this, new ArrayList<>(), smartin.miapi.Environment.isClient());
        if(smartin.miapi.Environment.isClient()){
            setupClient();
        }
    }

    public String getLangKey() {
        return "miapi.material.generated." + mainIngredient.getItem().getDescriptionId();
    }

    @Environment(EnvType.CLIENT)
    public void setupClient() {
        palette = GrayscalePaletteColorer.createForGeneratedMaterial(this, mainIngredient);
        icon = new MaterialIcons.ItemMaterialIcon(mainIngredient, 0, null);
        FakeTranslation.translations.put(getLangKey(), NamingUtil.generateTranslation(new ArrayList<>(), mainIngredient));
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController() {
        if (palette == null) {
            return new FallbackColorer(this);
        }
        return palette;
    }

    @Override
    public Map<ModuleProperty<?>, Object> materialProperties(String key) {
        return source.materialProperties(key);
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return source.getAllPropertyKeys();
    }

    @Override
    public double getDouble(String property) {
        if (stats.containsKey(property)) {
            return stats.get(property);
        }
        return 0;
    }

    @Override
    public String getData(String property) {
        if ("translation".equals(property)) {
            return getLangKey();
        }
        return source.getData(property);
    }

    @Override
    public List<String> getTextureKeys() {
        return List.of();
    }

    @Override
    public double getValueOfItem(ItemStack itemStack) {
        if (itemStack.equals(mainIngredient)) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public @Nullable Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (itemStack.equals(mainIngredient)) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectForTool;
    }

    @Override
    public JsonObject getDebugJson() {
        JsonObject object = new JsonObject();
        object.add("key", new JsonPrimitive(getKey()));
        JsonArray jsonElements = new JsonArray();
        getTextureKeys().forEach(jsonElements::add);
        object.add("groups", jsonElements);

        stats.forEach(object::addProperty);
        object.addProperty("translation", getLangKey());
        if (smartin.miapi.Environment.isClient()) {
            object.addProperty("fake_translation", getTranslation().getString());
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(mainIngredient.getItem());
            String iconBuilder = "{" +
                                 "\"type\": \"" + "item" + "\"," +
                                 "\"item\": \"" + itemId + "\"" +
                                 "}";
            object.add("icon", Miapi.gson.fromJson(iconBuilder, JsonObject.class));
        }
        if (palette != null) {
            StringBuilder paletteBuilder = new StringBuilder();
            paletteBuilder.append("{");
            paletteBuilder.append("\"type\": \"").append("grayscale_map").append("\",");
            paletteBuilder.append("\"colors\": ");
            JsonObject innerPalette = new JsonObject();
            for (int i = 0; i < palette.getColors().length; i++) {
                int abgr = palette.getColors()[i];
                innerPalette.addProperty(String.valueOf(i), new Color(
                        FastColor.ABGR32.red(abgr),
                        FastColor.ABGR32.green(abgr),
                        FastColor.ABGR32.blue(abgr),
                        FastColor.ABGR32.alpha(abgr)).hex());
            }
            paletteBuilder.append(Miapi.gson.toJson(innerPalette));
            paletteBuilder.append("}");
            object.add("palette", Miapi.gson.fromJson(paletteBuilder.toString(), JsonObject.class));
        }
        JsonArray ingredients = new JsonArray();
        JsonObject mainIngredientJson = new JsonObject();
        mainIngredientJson.add("item", new JsonPrimitive(BuiltInRegistries.ITEM.getKey(this.mainIngredient.getItem()).toString()));
        mainIngredientJson.add("value", new JsonPrimitive(1.0));
        JsonObject otherIngredient = new JsonObject();
        otherIngredient.add("value", new JsonPrimitive(1.0));
        ingredients.add(mainIngredientJson);
        ingredients.add(otherIngredient);
        object.add("items", ingredients);
        return object;
    }
}
