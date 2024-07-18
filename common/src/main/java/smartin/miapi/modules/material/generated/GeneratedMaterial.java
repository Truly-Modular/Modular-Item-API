package smartin.miapi.modules.material.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
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
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialIcons;
import smartin.miapi.modules.material.palette.FallbackColorer;
import smartin.miapi.modules.material.palette.GrayscalePaletteColorer;
import smartin.miapi.modules.material.palette.MaterialRenderController;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeneratedMaterial implements Material {
    ItemStack mainIngredient;
    Ingredient ingredient;
    String key;
    List<String> groups = new ArrayList<>();
    List<String> textureKeys;
    Map<String, Double> stats;
    TagKey<Block> incorrectForTool;
    GrayscalePaletteColorer palette;
    @Nullable
    MaterialIcons.MaterialIcon icon;
    final Tier toolMaterial;
    boolean isValid = false;
    SwordItem swordItem;
    List<TieredItem> toolItems;
    @Nullable
    String smithingParent = null;

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
                    Codec.STRING
                            .fieldOf("smithing_key")
                            .forGetter(m -> m.smithingParent == null ? "" : m.smithingParent)
            ).apply(instance, (itemstack, additionalIngredient, swordItem, ingredient_toolItems, smithingKey) -> {
                GeneratedMaterial material = new GeneratedMaterial(itemstack, additionalIngredient, ((SwordItem) (swordItem.getItem())).getTier(),
                        ingredient_toolItems.stream().map(itemStack -> (TieredItem) itemStack.getItem()).toList()
                );
                if (smithingKey != null && !smithingKey.isBlank()) {
                    material.setSmithingMaterial(smithingKey);
                }
                return material;
            }));
    public static StreamCodec<ByteBuf, GeneratedMaterial> STREAMCODEC = ByteBufCodecs.fromCodec(CODEC);

    /**
     * generates a {@link Material} from a {@link Tier} and an implemented Sword and Axe Item.
     * The Axe Item is important, its damage and attackspeed are scanned for further stats
     *
     * @param mainIngredient the main {@link ItemStack} that is the source for the {@link Tier}
     * @param ingredient     the whole {@link Ingredient} for the {@link Tier}
     * @param sourceTier     the {@link Tier} itself
     * @param toolItems      all the assosiated Tooltitems of the {@link Tier}
     */
    public GeneratedMaterial(ItemStack mainIngredient, Ingredient ingredient, Tier sourceTier, List<TieredItem> toolItems) {
        key = "generated_" + mainIngredient.getDescriptionId();
        this.toolMaterial = sourceTier;
        this.ingredient = ingredient;
        this.toolItems = toolItems;
        groups.add(key);
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
            setupClient();
        }
        stats.put("durability", (double) toolMaterial.getUses());
        stats.put("mining_speed", (double) toolMaterial.getSpeed());
        stats.put("enchantability", (double) toolMaterial.getEnchantmentValue());
        isValid = assignStats(toolItems);
        if (isValid) {
            MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(this, toolItems, smartin.miapi.Environment.isClient());
        }
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
            stats.put("hardness", (double) foundSwordItem.getTier().getAttackDamageBonus());
            double swordAttackDmg = foundSwordItem.getTier().getAttackDamageBonus();
            double axeAttackDmg = AttributeUtil.getActualValue(axeItem.getDefaultInstance(), EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE.value(), 0.0);
            double calculatedDamage = Math.floor(Math.pow((swordAttackDmg - 3.4) * 2.3, 1.0 / 3.0)) + 7;

            if (groups.contains("stone")) {
                stats.put("density", swordAttackDmg);
            } else if (groups.contains("crystal")) {
                stats.put("density", axeAttackDmg);
            } else {
                stats.put("density", swordAttackDmg / 3);
            }

            if (Math.abs(calculatedDamage - axeAttackDmg) > 0.1) {
                stats.put("axe_damage", calculatedDamage - axeAttackDmg);
            }

            if (groups.contains("crystal") || groups.contains("gemstone")) {
                stats.put("flexibility", 0.0);
            } else {
                stats.put("flexibility", (double) (toolMaterial.getSpeed() / 4));
            }
            return true;
        }
        return false;
    }

    public SwordItem getSwordItem() {
        return swordItem;
    }

    public void setSmithingMaterial(String other) {
        this.smithingParent = other;
        this.groups = List.of(getKey(), "smithing");
    }

    public String getLangKey() {
        return "miapi.material.generated." + mainIngredient.getItem().getDescriptionId();
    }

    public boolean isValid() {
        return isValid;
    }

    @Environment(EnvType.CLIENT)
    public void setupClient() {
        palette = GrayscalePaletteColorer.createForGeneratedMaterial(this, mainIngredient);
        icon = new MaterialIcons.ItemMaterialIcon(mainIngredient, 0, null);
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
        return Map.of();
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return List.of();
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
        return null;
    }

    @Override
    public List<String> getTextureKeys() {
        return List.of();
    }

    @Override
    public double getValueOfItem(ItemStack itemStack) {
        if (ingredient.test(itemStack)) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public @Nullable Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (ingredient.test(itemStack)) {
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
        otherIngredient.add("ingredient", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, toolMaterial.getRepairIngredient()).getOrThrow());
        otherIngredient.add("value", new JsonPrimitive(1.0));
        ingredients.add(mainIngredientJson);
        ingredients.add(otherIngredient);
        object.add("items", ingredients);
        return object;
    }
}
