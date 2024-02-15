package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.mixin.MiningToolItemAccessor;
import smartin.miapi.mixin.SmithingTransformRecipeAccessor;
import smartin.miapi.modules.material.palette.EmptyMaterialPalette;
import smartin.miapi.modules.material.palette.MaterialColorer;
import smartin.miapi.modules.material.palette.MaterialPaletteFromTexture;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.FakeTranslation;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static smartin.miapi.modules.material.MaterialProperty.materials;

public class GeneratedMaterial implements Material {
    public final ToolMaterial toolMaterial;
    public final ItemStack mainIngredient;
    public final String key;
    public final List<String> groups = new ArrayList<>();
    public final Map<String, Double> materialStats = new HashMap<>();
    public final Map<String, String> materialStatsString = new HashMap<>();
    public SwordItem swordItem;
    protected MaterialColorer palette;
    @Nullable
    public MaterialIcons.MaterialIcon icon;

    public static final List<ItemStack> generatedItems = new ArrayList<>();
    public static final List<ItemStack> generatedItemsTool = new ArrayList<>();
    public static final List<Item> woodItems = new ArrayList<>();
    public static final List<Item> stoneItems = new ArrayList<>();

    public static void setup() {
        ReloadEvents.MAIN.subscribe(isClient -> {
            if (isClient) {
                onReloadClient();
            } else {
                onReloadServer();
            }
        }, -1);
        ReloadEvents.dataSyncerRegistry.register("generated_materials", new ReloadEvents.DataSyncer() {
            @Override
            public PacketByteBuf createDataServer() {
                PacketByteBuf packetByteBuf = Networking.createBuffer();
                packetByteBuf.writeInt(generatedItems.size());
                for (int i = 0; i < generatedItems.size(); i++) {
                    packetByteBuf.writeItemStack(generatedItems.get(i));
                    packetByteBuf.writeItemStack(generatedItemsTool.get(i));
                }
                packetByteBuf.writeInt(woodItems.size());
                for (Item item : woodItems) {
                    packetByteBuf.writeItemStack(item.getDefaultStack());
                }
                packetByteBuf.writeInt(stoneItems.size());
                for (Item item : stoneItems) {
                    packetByteBuf.writeItemStack(item.getDefaultStack());
                }
                return packetByteBuf;
            }

            @Override
            public void interpretDataClient(PacketByteBuf buf) {
                List<ItemStack> generatedMaterials = new ArrayList<>();
                List<ItemStack> generatedMaterialsTools = new ArrayList<>();
                List<Item> stoneMaterials = new ArrayList<>();
                List<Item> woodMaterials = new ArrayList<>();
                int genMaterials = buf.readInt();
                for (int i = 0; i < genMaterials; i++) {
                    ItemStack itemStack = buf.readItemStack();
                    generatedMaterials.add(itemStack);
                    generatedMaterialsTools.add(buf.readItemStack());
                }
                genMaterials = buf.readInt();
                for (int i = 0; i < genMaterials; i++) {
                    Item item = buf.readItemStack().getItem();
                    stoneMaterials.add(item);
                }
                genMaterials = buf.readInt();
                for (int i = 0; i < genMaterials; i++) {
                    Item item = buf.readItemStack().getItem();
                    woodMaterials.add(item);
                }
                synchronized (generatedItems) {
                    generatedItems.clear();
                    generatedItems.addAll(generatedMaterials);
                }
                synchronized (stoneItems) {
                    stoneItems.clear();
                    stoneItems.addAll(stoneMaterials);
                }
                synchronized (woodItems) {
                    woodItems.clear();
                    woodItems.addAll(woodMaterials);
                }
            }
        });
    }

    public static void onReloadClient() {
        List<ToolItem> toolItems = Registries.ITEM.stream()
                .filter(ToolItem.class::isInstance)
                .map(ToolItem.class::cast)
                .toList();
        for (int i = 0; i < generatedItems.size(); i++) {
            ItemStack itemStack = generatedItems.get(i);
            try {
                ItemStack toolStack = generatedItemsTool.get(i);
                if (toolStack.getItem() instanceof ToolItem toolItem) {
                    GeneratedMaterial generatedMaterial = new GeneratedMaterial(toolItem.getMaterial(), itemStack, true);
                    if (generatedMaterial.assignStats(toolItems, false)) {
                        materials.put(generatedMaterial.getKey(), generatedMaterial);
                    }
                }
            } catch (Exception e) {
                if (itemStack != null) {
                    Miapi.LOGGER.info("Failure to generate Material on Client for :" + itemStack.getTranslationKey(), e);
                }
            }
        }
        woodItems.forEach(woodItem -> {
            try {
                GeneratedMaterial generatedMaterial = new GeneratedMaterial(ToolMaterials.WOOD, woodItem.getDefaultStack(), false);
                materials.put(generatedMaterial.getKey(), generatedMaterial);
                generatedMaterial.copyStatsFrom(materials.get("wood"));
            } catch (Exception e) {
                Miapi.LOGGER.error("Failure to setup Wood Material for " + woodItem.getTranslationKey(), e);
            }
        });
        stoneItems.forEach(stoneItem -> {
            try{
                GeneratedMaterial generatedMaterial = new GeneratedMaterial(ToolMaterials.STONE, stoneItem.getDefaultStack(), false);
                materials.put(generatedMaterial.getKey(), generatedMaterial);
                generatedMaterial.copyStatsFrom(materials.get("stone"));
            } catch (Exception e) {
                Miapi.LOGGER.error("Failure to setup Stone Material for " + stoneItem.getTranslationKey(), e);
            }
        });
    }

    public static void onReloadServer() {
        woodItems.clear();
        stoneItems.clear();
        generatedItems.clear();
        if (!MiapiConfig.OtherConfigGroup.generateMaterial.getValue()) {
            return;
        }
        List<ToolItem> toolItems = Registries.ITEM.stream()
                .filter(ToolItem.class::isInstance)
                .map(ToolItem.class::cast)
                .toList();
        if (MiapiConfig.OtherConfigGroup.generateOtherMaterials.getValue()) {
            toolItems.stream()
                    .map(ToolItem::getMaterial)
                    .collect(Collectors.toSet())
                    .stream()
                    .filter(toolMaterial -> toolMaterial.getRepairIngredient().getMatchingStacks().length > 0)
                    .filter(toolMaterial -> !toolMaterial.getRepairIngredient().getMatchingStacks()[0].isIn(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                    .filter(toolMaterial -> toolMaterial.getRepairIngredient() != null && toolMaterial.getRepairIngredient().getMatchingStacks() != null)
                    .filter(toolMaterial -> Arrays.stream(toolMaterial.getRepairIngredient().getMatchingStacks()).allMatch(itemStack -> MaterialProperty.getMaterialFromIngredient(itemStack) == null && !itemStack.getItem().equals(Items.BARRIER)))
                    .collect(Collectors.toSet()).forEach(toolMaterial -> {
                        if (isValidItem(toolMaterial.getRepairIngredient().getMatchingStacks()[0].getItem())) {
                            GeneratedMaterial generatedMaterial = new GeneratedMaterial(toolMaterial, false);
                            if (generatedMaterial.assignStats(toolItems, false)) {
                                materials.put(generatedMaterial.getKey(), generatedMaterial);
                                generatedItems.add(generatedMaterial.mainIngredient);
                                generatedItemsTool.add(generatedMaterial.swordItem.getDefaultStack());
                            } else {
                                //Miapi.LOGGER.warn("Couldn't correctly setup material for " + generatedMaterial.mainIngredient.getItem());
                            }
                        }
                    });
        }

        if (MiapiConfig.OtherConfigGroup.generateWoodMaterials.getValue()) {
            Registries.ITEM.stream().filter(item ->
                    item.getDefaultStack().isIn(ItemTags.PLANKS) &&
                            !item.getDefaultStack().isIn(RegistryInventory.MIAPI_FORBIDDEN_TAG)).forEach(item -> {
                if (isValidItem(item)) {
                    woodItems.add(item);
                    GeneratedMaterial generatedMaterial = new GeneratedMaterial(ToolMaterials.WOOD, item.getDefaultStack(), false);
                    materials.put(generatedMaterial.getKey(), generatedMaterial);
                    generatedMaterial.copyStatsFrom(materials.get("wood"));
                }
            });
        }

        if (MiapiConfig.OtherConfigGroup.generateStoneMaterials.getValue()) {
            Registries.ITEM.stream().filter(item -> item.getDefaultStack().isIn(ItemTags.STONE_TOOL_MATERIALS) &&
                    !item.getDefaultStack().isIn(RegistryInventory.MIAPI_FORBIDDEN_TAG)).forEach(item -> {
                if (isValidItem(item)) {
                    stoneItems.add(item);
                    GeneratedMaterial generatedMaterial = new GeneratedMaterial(ToolMaterials.STONE, item.getDefaultStack(), false);
                    materials.put(generatedMaterial.getKey(), generatedMaterial);
                    generatedMaterial.copyStatsFrom(materials.get("stone"));
                }
            });
        }

        toolItems.stream()
                .map(ToolItem::getMaterial)
                .collect(Collectors.toSet())
                .stream()
                .filter(toolMaterial -> toolMaterial.getRepairIngredient().getMatchingStacks().length > 0)
                .filter(toolMaterial -> !toolMaterial.getRepairIngredient().getMatchingStacks()[0].isIn(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                .filter(toolMaterial -> toolMaterial.getRepairIngredient() != null && toolMaterial.getRepairIngredient().getMatchingStacks() != null)
                .filter(toolMaterial -> Arrays.stream(toolMaterial.getRepairIngredient().getMatchingStacks()).allMatch(itemStack -> MaterialProperty.getMaterialFromIngredient(itemStack) != null && !itemStack.getItem().equals(Items.BARRIER)))
                .forEach(toolMaterial -> {
                    Material material = MaterialProperty.getMaterialFromIngredient(toolMaterial.getRepairIngredient().getMatchingStacks()[0]);
                    List<Item> toolMaterials = toolItems.stream()
                            .filter(toolMat -> toolMaterial.equals(toolMat.getMaterial()))
                            .collect(Collectors.toList());
                    if (material != null && material.generateConverters()) {
                        MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(material, toolMaterials, false);
                    }
                });

    }

    public static boolean isValidItem(Item item) {
        Identifier identifier = Registries.ITEM.getId(item);
        Pattern pattern = Pattern.compile(MiapiConfig.OtherConfigGroup.blockRegexGeneratedMaterials.getValue());
        if (pattern.matcher(identifier.toString()).find()) {
            return false;
        }
        return true;
    }

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient) {
        this(toolMaterial, toolMaterial.getRepairIngredient().getMatchingStacks()[0], isClient);
    }

    public GeneratedMaterial(ToolMaterial toolMaterial, ItemStack itemStack, boolean isClient) {
        this.toolMaterial = toolMaterial;
        mainIngredient = itemStack;
        key = "generated_" + mainIngredient.getTranslationKey();
        groups.add(key);
        if (mainIngredient.getTranslationKey().contains("ingot")) {
            groups.add("metal");
        }
        if (mainIngredient.getTranslationKey().contains("stone")) {
            groups.add("stone");
        }
        if (mainIngredient.getTranslationKey().contains("bone")) {
            groups.add("bone");
        }
        if (mainIngredient.isIn(ItemTags.PLANKS)) {
            groups.add("wood");
        }
        if (groups.size() == 1) {
            groups.add("crystal");
        }
        //TODO:generate those sensible ig?
        //maybe scan all items assosiaated with the toolmaterial to getRaw somewhat valid stats?
        materialStats.put("durability", (double) toolMaterial.getDurability());
        materialStats.put("mining_level", (double) toolMaterial.getMiningLevel());
        materialStats.put("mining_speed", (double) toolMaterial.getMiningSpeedMultiplier());
        materialStats.put("enchantability", (double) toolMaterial.getEnchantability());

        if (isClient) {
            clientSetup();
        }
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
        palette = MaterialPaletteFromTexture.forGeneratedMaterial(this, mainIngredient);
    }

    public boolean assignStats(List<ToolItem> toolItems, boolean isClient) {
        List<Item> toolMaterials = toolItems.stream()
                .filter(material -> toolMaterial.equals(material.getMaterial()))
                .collect(Collectors.toList());
        Optional<Item> swordItemOptional = toolMaterials.stream().filter(SwordItem.class::isInstance).findFirst();
        Optional<Item> axeItemOptional = toolMaterials.stream().filter(AxeItem.class::isInstance).findFirst();
        if (axeItemOptional.isEmpty()) {
            axeItemOptional = toolMaterials.stream().filter(MiningToolItem.class::isInstance).filter(miningTool -> ((MiningToolItemAccessor) miningTool).getEffectiveBlocks().equals(BlockTags.AXE_MINEABLE)).findFirst();
        }
        if (swordItemOptional.isPresent() && axeItemOptional.isPresent()) {
            if (swordItemOptional.get() instanceof SwordItem foundSwordItem && axeItemOptional.get() instanceof MiningToolItem axeItem) {
                this.swordItem = foundSwordItem;
                materialStats.put("hardness", (double) swordItem.getAttackDamage());

                double firstPart = Math.floor(Math.pow((swordItem.getAttackDamage() - 3.4) * 2.3, 1.0 / 3.0)) + 7;

                materialStats.put("density", ((axeItem.getAttackDamage() - firstPart) / 2.0) * 4.0);
                materialStats.put("flexibility", (double) (toolMaterial.getMiningSpeedMultiplier() / 4));
                if (Platform.getEnvironment() == Env.CLIENT) {
                    generateTranslation(toolMaterials);
                }
                MiapiEvents.GENERATED_MATERIAL.invoker().generated(this, mainIngredient, toolMaterials, isClient);
                MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(this, toolMaterials, isClient);
                return true;
            }
        }
        return false;
    }

    public void testForSmithingMaterial(boolean isClient) {
        RecipeManager manager = findManager(isClient);
        DynamicRegistryManager registryManager = findRegistryManager(isClient);
        manager.listAllOfType(RecipeType.SMITHING).stream()
                .filter(SmithingTransformRecipe.class::isInstance)
                //filter for only ItemChanging Recipes
                .map(SmithingTransformRecipe.class::cast)
                //check if the output is valid
                .filter(recipe -> isValidRecipe(recipe, swordItem, registryManager))
                .findAny()
                .ifPresent(smithingTransformRecipe -> {
                    ItemStack templateItem = Arrays.stream(((SmithingTransformRecipeAccessor) smithingTransformRecipe).getTemplate().getMatchingStacks()).filter(itemStack -> !itemStack.isEmpty()).findAny().orElse(ItemStack.EMPTY);
                    if (templateItem.isEmpty()) {
                        //make sure the recipe is valid by testing its template Item
                        return;
                    }
                    Arrays.stream(((SmithingTransformRecipeAccessor) smithingTransformRecipe).getBase().getMatchingStacks())
                            //making sure the input has a valid SourceMaterial
                            .filter(itemStack -> {
                                if (itemStack.getItem() instanceof ToolItem toolItem) {
                                    Material material = MaterialProperty.getMaterialFromIngredient(toolItem.getMaterial()
                                            .getRepairIngredient().getMatchingStacks()[0]);
                                    return material != null;
                                }
                                return false;
                            })
                            .map(itemStack -> MaterialProperty.getMaterialFromIngredient(((ToolItem) itemStack.getItem()).getMaterial()
                                    .getRepairIngredient().getMatchingStacks()[0]))
                            .findAny()
                            .ifPresent(sourceMaterial -> {
                                addSmithingRecipe(sourceMaterial, templateItem, smithingTransformRecipe, isClient);
                            });
                });
    }

    public void addSmithingRecipe(Material sourceMaterial, ItemStack templateItem, SmithingTransformRecipe smithingTransformRecipe, boolean isClient) {
        RecipeManager manager = findManager(isClient);
        Collection<Recipe<?>> recipes = manager.values();
        String id = "generated_material_recipe." + key + "." + Registries.ITEM.getId(templateItem.getItem()) + "." + sourceMaterial.getKey();
        id = id.replace(":",".");
        Identifier recipeId = new Identifier(Miapi.MOD_ID, id);
        if (manager.get(recipeId).isEmpty()) {
            recipes.add(new MaterialSmithingRecipe(
                    recipeId,
                    Ingredient.ofStacks(templateItem),
                    sourceMaterial.getKey(),
                    ((SmithingTransformRecipeAccessor) smithingTransformRecipe).getAddition(),
                    this.key
            ));
            Miapi.LOGGER.warn("added Smithing Recipe for " + sourceMaterial.getKey() + " to " + this.key + " via " + templateItem.getItem());
            this.groups.clear();
            this.groups.add(this.key);
            this.groups.add("smithing");
            manager.setRecipes(recipes);
        }
    }

    public boolean generateConverters() {
        return true;
    }

    static boolean isValidRecipe
            (SmithingTransformRecipe recipe, SwordItem swordItem, DynamicRegistryManager manager) {
        if (recipe.getOutput(manager).getItem().equals(swordItem)) {
            return true;
        }
        return ((SmithingTransformRecipeAccessor) recipe).getResult().getItem().equals(swordItem);
    }

    static RecipeManager findManager(boolean isClient) {
        if (isClient) {
            return MinecraftClient.getInstance().world.getRecipeManager();
        } else {
            return Miapi.server.getRecipeManager();
        }
    }

    static DynamicRegistryManager findRegistryManager(boolean isClient) {
        if (isClient) {
            return MinecraftClient.getInstance().world.getRegistryManager();
        } else {
            return Miapi.server.getRegistryManager();
        }
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
        if (stringB == null || stringA == null) {
            return "";
        }
        try {
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
            return stringA.substring(end - result + 1, end + 1);
        } catch (Exception e) {
            Miapi.LOGGER.warn("Exception during string comparison" + e);
            return "";
        }
    }

    public void copyStatsFrom(Material other) {
        materialStats.put("hardness", other.getDouble("hardness"));
        materialStats.put("density", other.getDouble("density"));
        materialStats.put("flexibility", other.getDouble("flexibility"));
        materialStats.put("durability", other.getDouble("durability"));
        materialStats.put("mining_level", other.getDouble("mining_level"));
        materialStats.put("mining_speed", other.getDouble("mining_speed"));
        if (Platform.getEnvironment() == Env.CLIENT) addFakeTranslationForCopy();
    }

    @Environment(EnvType.CLIENT)
    public void addFakeTranslationForCopy() {
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
        return icon != null;
    }

    @Environment(EnvType.CLIENT)
    public int renderIcon(DrawContext drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
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
    public Map<ModuleProperty, JsonElement> materialProperties(String key) {
        return new HashMap<>();
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return new ArrayList<>();
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
    public List<String> getTextureKeys() {
        List<String> keys = new ArrayList<>(this.groups);
        keys.add("default");
        return keys;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialColorer getPalette() {
        if (palette == null) {
            return new EmptyMaterialPalette(this);
        }
        return palette;
    }

    @Override
    public double getValueOfItem(ItemStack item) {
        if (toolMaterial.getRepairIngredient().test(item)) {
            return 1;
        }
        return item.getItem().equals(mainIngredient.getItem()) ? 1 : 0;
    }

    @Override
    public @Nullable Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (mainIngredient.getItem().equals(itemStack.getItem())) {
            return 0.0;
        }
        if (toolMaterial.getRepairIngredient().test(itemStack)) {
            return 0.0;
        }
        return null;
    }
}
