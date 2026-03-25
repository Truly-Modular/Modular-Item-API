package smartin.miapi.material.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.MaterialIcons;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.FallbackColorer;
import smartin.miapi.material.palette.GrayscalePaletteColorer;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.tag.ModuleTagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratedMaterial implements Material {
    ItemStack mainIngredient = ItemStack.EMPTY;
    Ingredient ingredient = Ingredient.EMPTY;
    ResourceLocation key = Miapi.id("empty_material");
    List<String> groups = new ArrayList<>();
    List<String> textureKeys = new ArrayList<>();
    public Map<String, Double> stats = new HashMap<>();
    List<ArmorItem> armorItems = new ArrayList<>();
    TagKey<Block> incorrectForTool = BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    GrayscalePaletteColorer palette;
    @Nullable
    MaterialIcons.MaterialIcon icon;
    final Tier toolMaterial;
    boolean isValid = false;
    SwordItem swordItem;
    List<TieredItem> toolItems;
    Optional<ResourceLocation> smithingParent = Optional.empty();
    Component name = null;
    public Map<String, Map<ModuleProperty<?>, Object>> properties = new HashMap<>();
    SmithingMode smithingMode = SmithingMode.NONE;
    ItemStack smithingTemplate = ItemStack.EMPTY;
    Optional<Float> armorHardness = Optional.empty();

    public static Codec<GeneratedMaterial> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ItemStack.CODEC
                            .fieldOf("main_ingredient")
                            .forGetter(m -> m.mainIngredient),
                    Ingredient.CODEC
                            .fieldOf("ingredient").
                            forGetter(m -> m.ingredient),
                    ItemStack.CODEC
                            .fieldOf("sword")
                            .forGetter(m -> m.swordItem.getDefaultInstance()),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf("toolItems")
                            .forGetter(m -> m.toolItems.stream().map(Item::getDefaultInstance).toList()),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf("armorItems")
                            .forGetter(m -> m.toolItems.stream().map(Item::getDefaultInstance).toList()),
                    Codec.FLOAT
                            .optionalFieldOf("armor_hardness")
                            .forGetter(m -> m.armorHardness),
                    ResourceLocation.CODEC
                            .optionalFieldOf("smithing_key")
                            .forGetter(m -> m.smithingParent),
                    ItemStack.CODEC
                            .optionalFieldOf("smithing_template", ItemStack.EMPTY)
                            .forGetter(m -> m.swordItem.getDefaultInstance())
            ).apply(instance, (itemstack, additionalIngredient, swordItem, toolItems, armorItems, armor, smithingKey, smithingItem) -> {
                GeneratedMaterial material = new GeneratedMaterial(itemstack, additionalIngredient, ((SwordItem) (swordItem.getItem())).getTier(),
                        toolItems.stream()
                                .filter(stack -> stack.getItem() instanceof TieredItem)
                                .map(itemStack -> (TieredItem) itemStack.getItem()).toList(),
                        armorItems.stream()
                                .filter(stack -> stack.getItem() instanceof ArmorItem)
                                .map(itemStack -> (ArmorItem) itemStack.getItem()).toList()
                );
                smithingKey.ifPresent(location -> material.setSmithingMaterial(location, Ingredient.of(smithingItem)));
                armor.ifPresent(aFloat -> material.stats.put("armor_hardness", (double) aFloat));
                return material;
            }));

    /**
     * generates a {@link Material} from a {@link Tier} and an implemented Sword and Axe Item.
     * The Axe Item is important, its damage and attackspeed are scanned for further stats
     *
     * @param mainIngredient the main {@link ItemStack} that is the source for the {@link Tier}
     * @param ingredient     the whole {@link Ingredient} for the {@link Tier}
     * @param sourceTier     the {@link Tier} itself
     * @param toolItems      all the assosiated Tooltitems of the {@link Tier}
     */
    public GeneratedMaterial(ItemStack mainIngredient, Ingredient ingredient, Tier sourceTier, List<TieredItem> toolItems, List<ArmorItem> armorlItems) {
        key = Miapi.id("generated/" + mainIngredient.getDescriptionId() + toolItems.getFirst().getDescriptionId());
        this.armorItems = armorlItems;
        this.toolMaterial = sourceTier;
        this.ingredient = ingredient;
        this.toolItems = toolItems;
        this.mainIngredient = mainIngredient;
        groups.add(key.toString());
        textureKeys = List.of("default");
        if (mainIngredient.getDescriptionId().contains("ingot")) {
            groups.add("metal");
        }
        if (mainIngredient.getDescriptionId().contains("stone")) {
            groups.add("stone");
        }
        if (mainIngredient.getDescriptionId().contains("bone")) {
            groups.add("bone");
        }
        if (mainIngredient.is(ItemTags.PLANKS)) {
            groups.add("wood");
        }
        if (groups.size() == 1) {
            groups.add("crystal");
        }
        if (smartin.miapi.Environment.isClient()) {
            //setupClient();
        }
        stats.put("durability", (double) toolMaterial.getUses() - 15);
        stats.put("mining_speed", (double) toolMaterial.getSpeed());
        stats.put("enchantability", (double) toolMaterial.getEnchantmentValue());
        isValid = assignStats(toolItems);
        this.incorrectForTool = sourceTier.getIncorrectBlocksForDrops();
    }

    public boolean assignStats(List<TieredItem> toolItems) {
        List<Item> toolMaterials = toolItems.stream()
                .filter(material -> toolMaterial.equals(material.getTier()))
                .collect(Collectors.toList());
        Optional<Item> swordItemOptional = toolMaterials.stream().filter(SwordItem.class::isInstance).findFirst();
        Optional<Item> axeItemOptional = toolMaterials.stream().filter(AxeItem.class::isInstance).findFirst();
        if (swordItemOptional.isPresent() && axeItemOptional.isPresent() &&
            swordItemOptional.get() instanceof SwordItem foundSwordItem &&
            axeItemOptional.get() instanceof DiggerItem axeItem) {
            swordItem = foundSwordItem;
            key = Miapi.id("generated/" + mainIngredient.getDescriptionId() + swordItem.getDescriptionId());
            double swordAttackDmg = AttributeUtil.getActualValue(swordItem.getDefaultInstance(), EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE.value(), 0.0);
            double axeAttackDmg = AttributeUtil.getActualValue(axeItem.getDefaultInstance(), EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE.value(), 0.0);

            stats.put("hardness", swordAttackDmg);
            stats.put("density", Math.max(0, axeAttackDmg - swordAttackDmg));

            if (groups.contains("crystal") || groups.contains("gemstone")) {
                stats.put("flexibility", 0.0);
            } else {
                stats.put("flexibility", (double) (toolMaterial.getSpeed() / 4));
            }
            stats.put("tier", swordAttackDmg - 2);
            armorItems = findRelatedArmorItems();
            properties = GeneratedMaterialPropertyManager.setup(getID(), swordItem, axeItem, toolMaterials, armorItems, Map.of());
            return true;
        }
        return false;
    }

    public Material getMaterial(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties) {
        if (ModuleTagProperty.getTags(moduleInstance).contains("armor")) {
            if (stats.containsKey("armor_hardness")) {
                return new DelegatingMaterial(this) {
                    @Override
                    public double getDouble(String property) {
                        if (property.equals("hardness")) {
                            if (GeneratedMaterialManager.verboseLogging()) {
                                Miapi.LOGGER.info("returning hardness " + stats.get("armor_hardness"));
                            }
                            return stats.get("armor_hardness");
                        }
                        if (stats.containsKey(property)) {
                            return stats.get(property);
                        }
                        return 0;
                    }
                };
            }
        }
        return this;
    }

    public List<ArmorItem> findRelatedArmorItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(ArmorItem.class::isInstance)
                .map(r -> (ArmorItem) r)
                .filter(armorItem -> {
                    try {
                        if (armorItem.getMaterial().value().repairIngredient() != null) {
                            return armorItem.getMaterial().value().repairIngredient().get().test(mainIngredient);
                        }
                    } catch (RuntimeException e) {
                        if (GeneratedMaterialManager.verboseLogging()) {
                            Miapi.LOGGER.error("Error during armor test", e);
                        }
                    }
                    return false;
                }).toList();
    }

    public SwordItem getSwordItem() {
        return swordItem;
    }

    public void setSmithingMaterial(ResourceLocation other, Ingredient ingredient) {
        this.smithingParent = Optional.of(other);
        var otherMat = MaterialProperty.getMaterialFromIngredient(mainIngredient);
        if (GeneratedMaterialManager.verboseLogging()) {
            Miapi.LOGGER.info("other mat for smithing test " + otherMat);
        }
        this.addSmithingGroup();
        if (otherMat == null || this.equals(otherMat) || otherMat.getID().equals(this.getID())) {
            smithingMode = SmithingMode.INGREDIENT;
        } else {
            smithingMode = SmithingMode.TEMPLATE;
            if (otherMat != null) {
                otherMat.addSmithingGroup();
            }
            if (!ingredient.isEmpty()) {
                if (ingredient.getItems() != null &&
                    ingredient.getItems()[0] != null &&
                    !ingredient.getItems()[0].isEmpty()) {
                    smithingTemplate = ingredient.getItems()[0];
                    //this.groups = List.of("smithing", Component.translatable("miapi.template.source", smithingTemplate.getDisplayName()).getString());
                }
            }
        }
    }

    public void addSmithingGroup() {
        if (!groups.contains("smithing")) {
            groups = new ArrayList<>(List.of(key.toString(),"smithing"));
        }
    }

    public Component getTranslation() {
        if (this.name == null) {
            this.name = Component.literal(NamingUtil.generateTranslation(toolItems, mainIngredient));
        }
        return this.name;
    }

    public boolean isValid() {
        return isValid;
    }

    @Environment(EnvType.CLIENT)
    public void setupClient() {
        palette = GrayscalePaletteColorer.createForGeneratedMaterial(this, mainIngredient);
        icon = new MaterialIcons.ItemMaterialIcon(mainIngredient, 18, null);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int renderIcon(GuiGraphics drawContext, int x, int y) {
        if (icon == null) {
            return 0;
        }
        return icon.render(drawContext, x, y);
    }

    @Override
    public boolean hasIcon() {
        return true;
    }

    @Override
    public ResourceLocation getID() {
        return key;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode) {
        if (palette == null) {
            return new FallbackColorer(this);
        }
        return palette;
    }

    @Override
    public Map<ModuleProperty<?>, Object> materialProperties(String key) {
        return properties.getOrDefault(key, Map.of());
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return properties.keySet().stream().toList();
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
        return null;
    }

    @Override
    public List<String> getTextureKeys() {
        return textureKeys;
    }

    @Override
    public double getValueOfItem(ItemStack itemStack) {
        if (!smithingMode.equals(SmithingMode.TEMPLATE)) {
            if (mainIngredient.getItem().equals(itemStack.getItem())) {
                return 1.0;
            }
            if (ingredient.test(itemStack)) {
                return 1.0;
            }
        } else {
            if (mainIngredient.getItem().equals(itemStack.getItem())) {
                return 1.0;
            }
            if (ingredient.test(itemStack)) {
                return 1.0;
            }
        }
        return 0.0;
    }

    @Override
    public double getRepairValueOfItem(ItemStack itemStack) {
        if (mainIngredient.getItem().equals(itemStack.getItem())) {
            return 1.0;
        }
        if (ingredient.test(itemStack)) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public @Nullable Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (!smithingMode.equals(SmithingMode.TEMPLATE)) {
            if (mainIngredient.getItem().equals(itemStack.getItem())) {
                return -10.0;
            }
            if (ingredient.test(itemStack)) {
                return -1.0;
            }
        } else {
            if (itemStack.getItem().equals(smithingTemplate.getItem())) {
                return 1.0;
            }
            if (mainIngredient.getItem().equals(itemStack.getItem())) {
                return -10.0;
            }
            if (ingredient.test(itemStack)) {
                return -1.0;
            }
        }
        return null;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectForTool;
    }

    @Override
    public JsonObject getDebugJson() {
        try {
            return MaterialHelper.toCodecMaterial(this).getDebugJson();
        } catch (RuntimeException e) {
            Miapi.LOGGER.warn("could not convert generated material", e);
        }
        JsonObject object = new JsonObject();
        object.add("id", new JsonPrimitive(getID().toString()));
        JsonArray jsonElements = new JsonArray();
        getTextureKeys().forEach(jsonElements::add);
        object.add("groups", jsonElements);

        stats.forEach(object::addProperty);
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
        otherIngredient.add("ingredient", Ingredient.CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, toolMaterial.getRepairIngredient()).getOrThrow());
        otherIngredient.add("value", new JsonPrimitive(1.0));
        ingredients.add(mainIngredientJson);
        ingredients.add(otherIngredient);
        object.add("items", ingredients);
        return object;
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    enum SmithingMode {
        NONE,
        TEMPLATE,
        INGREDIENT
    }
}
