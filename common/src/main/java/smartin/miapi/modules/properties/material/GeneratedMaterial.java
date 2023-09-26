package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.properties.material.palette.EmptyMaterialPalette;
import smartin.miapi.modules.properties.material.palette.MaterialPalette;
import smartin.miapi.modules.properties.material.palette.MaterialPaletteFromTexture;
import smartin.miapi.modules.properties.material.palette.PaletteCreators;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

public class GeneratedMaterial implements Material {
    public final ToolMaterial toolMaterial;
    public final ItemStack mainIngredient;
    public final String key;
    public final List<String> groups = new ArrayList<>();
    public final Map<String, Float> materialStats = new HashMap<>();
    public final Map<String, String> materialStatsString = new HashMap<>();
    public JsonObject jsonObject;
    @Environment(EnvType.CLIENT)
    public MaterialPalette materialPalette;

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient) {
        this(toolMaterial, isClient, toolMaterial.getRepairIngredient().getMatchingStacks()[0]);
    }

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient, ItemStack itemStack) {
        this.toolMaterial = toolMaterial;
        mainIngredient = itemStack;
        Arrays.stream(toolMaterial.getRepairIngredient().getMatchingStacks()).forEach(stack -> {
            Miapi.DEBUG_LOGGER.info("found item " + stack.getItem() + " " + isClient);
        });
        key = "generated_" + mainIngredient.getItem().getTranslationKey();
        if (mainIngredient.getItem().getTranslationKey().contains("ingot")) {
            groups.add("metal");
        }
        if (mainIngredient.getItem().getTranslationKey().contains("stone")) {
            groups.add("stone");
        }
        if (mainIngredient.getItem().getTranslationKey().contains("bone")) {
            groups.add("bone");
        }
        if (mainIngredient.isIn(ItemTags.PLANKS)) {
            groups.add("wood");
        }
        if (groups.isEmpty()) {
            groups.add("crystal");
        }
        materialStats.put("hardness", toolMaterial.getAttackDamage());
        materialStats.put("density", toolMaterial.getAttackDamage() - toolMaterial.getMiningLevel());
        materialStats.put("flexibility", toolMaterial.getAttackDamage() - toolMaterial.getMiningSpeedMultiplier() / 2);
        materialStats.put("durability", (float) toolMaterial.getDurability());
        materialStats.put("mining_level", (float) toolMaterial.getMiningLevel());
        materialStats.put("mining_speed", toolMaterial.getMiningSpeedMultiplier());
        materialStatsString.put("translation_key", mainIngredient.getItem().getTranslationKey());
        Identifier itemId = Registries.ITEM.getId(mainIngredient.getItem());
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"items\":");
        builder.append("[");
        builder.append("{");
        builder.append("\"item\": \"").append(itemId).append("\",");
        builder.append("\"value\": 1.0");
        builder.append("}");
        builder.append("]");
        builder.append("}");
        jsonObject = Miapi.gson.fromJson(builder.toString(), JsonObject.class);
        Miapi.DEBUG_LOGGER.warn(Miapi.gson.toJson(jsonObject));
        if (isClient) {
            clientSetup();
        }
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup() {
        Miapi.DEBUG_LOGGER.warn("CLEINTSEUTP");
        try {
            Miapi.DEBUG_LOGGER.warn("CLEINTSEUTP");
            BakedModel itemModel = MinecraftClient.getInstance().getItemRenderer().getModel(mainIngredient, MinecraftClient.getInstance().world, null, 0);
            SpriteContents contents = itemModel.getParticleSprite().getContents();
            materialPalette = new MaterialPaletteFromTexture(this, ((SpriteContentsAccessor) contents).getImage());
            Miapi.DEBUG_LOGGER.warn("PALETTE DONE "+materialPalette);
            return;
        } catch (Exception e) {
            Miapi.DEBUG_LOGGER.warn("Error during palette creation", e);
            materialPalette = new EmptyMaterialPalette(this);
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Override
    public MaterialPalette getPalette() {
        return materialPalette;
    }

    @Override
    public Map<ModuleProperty, JsonElement> materialProperties(String key) {
        return new HashMap<>();
    }

    @Override
    public JsonElement getRawElement(String key) {
        return jsonObject.get(key);
    }

    @Override
    public double getDouble(String property) {
        if (materialStats.containsKey(property)) {
            return materialStats.get(property);
        }
        return 0;
    }

    @Override
    public String getData(String property) {
        return "";
    }

    @Override
    public List<String> getTextureKeys() {
        List<String> keys = new ArrayList<>(this.groups);
        keys.add("default");
        return keys;
    }

    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public double getValueOfItem(ItemStack item) {
        return 1;
    }
}
