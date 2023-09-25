package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

public class GeneratedMaterial implements Material {
    public final ToolMaterial toolMaterial;
    public final ItemStack mainIngredient;
    public final String key;
    public final List<String> groups = new ArrayList<>();
    public final Map<String, Float> materialStats = new HashMap<>();
    @Environment(EnvType.CLIENT)
    private Identifier spriteID;

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient) {
        this(toolMaterial, isClient, toolMaterial.getRepairIngredient().getMatchingStacks()[0]);
    }

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient, ItemStack itemStack) {
        this.toolMaterial = toolMaterial;
        mainIngredient = itemStack;
        Arrays.stream(toolMaterial.getRepairIngredient().getMatchingStacks()).forEach(stack -> {
            Miapi.DEBUG_LOGGER.info("found item " + stack.getItem());
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
        if(isClient){
            clientSetup();
        }
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup(){

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
    public @Nullable SpriteContents generateSpriteContents() {
        Sprite sprite = null;
        //SpriteLoader.fromAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).
        return null;
    }

    @Override
    public @Nullable Identifier getSpriteId() {
        return spriteID;
    }

    @Override
    public @Nullable void setSpriteId(Identifier identifier) {
        spriteID = identifier;
    }

    @Override
    public Map<ModuleProperty, JsonElement> materialProperties(String key) {
        return new HashMap<>();
    }

    @Override
    public JsonElement getRawElement(String key) {
        return new JsonObject();
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
        return new ArrayList<>();
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
