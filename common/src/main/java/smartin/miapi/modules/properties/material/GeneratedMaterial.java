package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.mixin.MiningToolItemAccessor;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.properties.material.palette.EmptyMaterialPalette;
import smartin.miapi.modules.properties.material.palette.MaterialPalette;
import smartin.miapi.modules.properties.material.palette.MaterialPaletteFromTexture;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.FakeTranslation;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratedMaterial implements Material {
    public final ToolMaterial toolMaterial;
    public final ItemStack mainIngredient;
    public final String key;
    public final List<String> groups = new ArrayList<>();
    public final Map<String, Double> materialStats = new HashMap<>();
    public final Map<String, String> materialStatsString = new HashMap<>();
    public JsonObject jsonObject;
    @Environment(EnvType.CLIENT)
    public MaterialPalette materialPalette;
    @Nullable
    @Environment(EnvType.CLIENT)
    public MaterialIcons.MaterialIcon icon;

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient) {
        this(toolMaterial, isClient, toolMaterial.getRepairIngredient().getMatchingStacks()[0]);
    }

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient, ItemStack itemStack) {
        this.toolMaterial = toolMaterial;
        mainIngredient = itemStack;
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
        //TODO:generate those sensible ig?
        //maybe scan all items assosiaated with the toolmaterial to get somewhat valid stats?
        materialStats.put("durability", (double) toolMaterial.getDurability());
        materialStats.put("mining_level", (double) toolMaterial.getMiningLevel());
        materialStats.put("mining_speed", (double) toolMaterial.getMiningSpeedMultiplier());
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
        if (isClient && Platform.getEnvironment() == Env.CLIENT) {
            clientSetup();
        }
    }

    public boolean assignStats(List<ToolItem> toolItems, boolean isClient) {
        List<Item> toolMaterials = toolItems.stream()
                .filter(material -> toolMaterial.equals(material.getMaterial()))
                .collect(Collectors.toList());
        Optional<Item> swordItem = toolMaterials.stream().filter(SwordItem.class::isInstance).findFirst();
        Optional<Item> axeItem = toolMaterials.stream().filter(AxeItem.class::isInstance).findFirst();
        if (axeItem.isEmpty()) {
            axeItem = toolMaterials.stream().filter(MiningToolItem.class::isInstance).filter(miningTool -> ((MiningToolItemAccessor) miningTool).getEffectiveBlocks().equals(BlockTags.AXE_MINEABLE)).findFirst();
        }
        if (swordItem.isPresent() && axeItem.isPresent()) {
            if (swordItem.get() instanceof SwordItem swordItem1 && axeItem.get() instanceof MiningToolItem axeItem1) {
                materialStats.put("hardness", (double) swordItem1.getAttackDamage());

                double firstPart = Math.floor(Math.pow((swordItem1.getAttackDamage() - 3.4) * 2.3, 1.0 / 3.0)) + 7;

                materialStats.put("density", ((axeItem1.getAttackDamage() - firstPart) / 2.0) * 4.0);
                materialStats.put("flexibility", (double) (toolMaterial.getMiningSpeedMultiplier() / 4));
                if (isClient) {
                    generateTranslation(toolMaterials);
                }
                MiapiEvents.GENERATED_MATERIAL.invoker().generated(this, mainIngredient, toolMaterials, isClient);
                return true;
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public void generateTranslation(List<Item> items) {
        List<String> names = new ArrayList<>();
        items.forEach(item -> names.add(Text.translatable(item.getTranslationKey()).getString()));
        String materialName = Text.translatable(mainIngredient.getTranslationKey()).getString();
        String translationKey = "miapi.material.generated." + mainIngredient.getItem().getTranslationKey();
        String materialTranslation = findCommonSubstring(names, materialName);
        if (!materialTranslation.endsWith(" ")) {
            materialTranslation += " ";
        }
        FakeTranslation.translations.put(translationKey, materialTranslation);
        materialStatsString.put("translation", translationKey);
    }

    static String findCommonSubstring(List<String> itemNames, String materialName) {
        Map<String, Integer> map = new HashMap<>();
        map.put(materialName, 1);
        int highest = 0;
        String longestCommonSubstring = materialName;
        for (String itemName : itemNames) {
            String commonString = longestSubsString(itemName, materialName);
            if (commonString.length() > 3) {
                if (map.containsKey(commonString)) {
                    map.put(commonString, map.get(commonString) + 1);
                    if (map.get(commonString) > highest) {
                        highest = map.get(commonString);
                        longestCommonSubstring = commonString;
                    }
                } else {
                    map.put(commonString, 1);
                }
            }
        }
        return longestCommonSubstring;
    }

    static String longestSubsString(String stringA, String stringB) {
        // Find length of both the Strings.
        try{
            if (stringB.length() > stringA.length()) {
                String buffer = stringA;
                stringA = stringB;
                stringB = buffer;
            }
            int m = stringA.length();
            int n = stringB.length();

            // Variable to store length of longest
            // common subString.
            int result = 0;

            // Variable to store ending point of
            // longest common subString in X.
            int end = 0;

            // Matrix to store result of two
            // consecutive rows at a time.
            int len[][] = new int[2][m];

            // Variable to represent which row of
            // matrix is current row.
            int currRow = 0;

            // For a particular value of i and j,
            // len[currRow][j] stores length of longest
            // common subString in String X[0..i] and Y[0..j].
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == 0 || j == 0) {
                        len[currRow][j] = 0;
                    } else if (stringA.charAt(i - 1) == stringB.charAt(j - 1)) {
                        len[currRow][j] = len[1 - currRow][j - 1] + 1;
                        if (len[currRow][j] > result) {
                            result = len[currRow][j];
                            end = i - 1;
                        }
                    } else {
                        len[currRow][j] = 0;
                    }
                }

                // Make current row as previous row and
                // previous row as new current row.
                currRow = 1 - currRow;
            }

            // If there is no common subString, print -1.
            if (result == 0) {
                return "";
            }

            // Longest common subString is from index
            // end - result + 1 to index end in X.
            return stringA.substring(end - result + 1, result);
        }
        catch (Exception e){
            Miapi.LOGGER.warn("Exception during string comparison");
            return "";
        }
    }

    public void copyStatsFrom(Material other, boolean isClient) {
        materialStats.put("hardness", other.getDouble("hardness"));
        materialStats.put("density", other.getDouble("density"));
        materialStats.put("flexibility", other.getDouble("flexibility"));
        materialStats.put("durability", other.getDouble("durability"));
        materialStats.put("mining_level", other.getDouble("mining_level"));
        materialStats.put("mining_speed", other.getDouble("mining_speed"));
        if (isClient) {
            addFakeTranslationForCopy();
        }
    }

    @Environment(EnvType.CLIENT)
    public void addFakeTranslationForCopy(){
        String materialTranslation = Text.translatable(mainIngredient.getTranslationKey()).getString();
        String translationKey = "miapi.material.generated." + mainIngredient.getItem().getTranslationKey();
        if (!materialTranslation.endsWith(" ")) {
            materialTranslation += " ";
        }
        FakeTranslation.translations.put(translationKey, materialTranslation);
        materialStatsString.put("translation", translationKey);
    }

    @Environment(EnvType.CLIENT)
    public boolean hasIcon() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    public int renderIcon(DrawContext drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup() {
        Identifier itemId = Registries.ITEM.getId(mainIngredient.getItem());
        StringBuilder iconBuilder = new StringBuilder();
        iconBuilder.append("{");
        iconBuilder.append("\"type\": \"").append("item").append("\",");
        iconBuilder.append("\"item\": \"").append(itemId).append("\"");
        iconBuilder.append("}");
        icon = MaterialIcons.getMaterialIcon(key, Miapi.gson.fromJson(iconBuilder.toString(), JsonObject.class));
        try {
            materialPalette = new MaterialPaletteFromTexture(this, () -> {
                BakedModel itemModel = MinecraftClient.getInstance().getItemRenderer().getModel(mainIngredient, MinecraftClient.getInstance().world, null, 0);
                SpriteContents contents = itemModel.getParticleSprite().getContents();
                return ((SpriteContentsAccessor) contents).getImage();
            });
        } catch (Exception e) {
            Miapi.LOGGER.warn("Error during palette creation", e);
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
    @Environment(EnvType.CLIENT)
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
        return materialStatsString.get(property);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<String> getTextureKeys() {
        List<String> keys = new ArrayList<>(this.groups);
        keys.add("default");
        return keys;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getColor() {
        return 0;
    }

    @Override
    public double getValueOfItem(ItemStack item) {
        return 1;
    }
}
