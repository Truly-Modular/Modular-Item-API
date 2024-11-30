package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

import static smartin.miapi.Miapi.MOD_ID;

public interface Material {
    ResourceLocation BASE_PALETTE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "miapi_materials/base_palette");

    ResourceLocation getID();

    default String getStringID() {
        String id = getID().toString();
        id = id.replace(":", ".");
        id = id.replaceAll("/", ".");
        return id;
    }

    @Environment(EnvType.CLIENT)
    static int getColor(String color) {
        if (color.equals("")) return FastColor.ARGB32.color(255, 255, 255, 255);
        long longValue = Long.parseLong(color, 16);
        return (int) (longValue & 0xffffffffL);
    }

    List<String> getGroups();

    default List<String> getGuiGroups() {
        return getGroups();
    }

    @Environment(EnvType.CLIENT)
    MaterialRenderController getRenderController();

    /**
     * @param drawContext a DrawContext that can be used to draw shtuff
     * @param x           x pos of the icon
     * @param y           y pos of the icon
     * @return how much to offset the text rendering by
     */
    @Environment(EnvType.CLIENT)
    default int renderIcon(GuiGraphics drawContext, int x, int y) {
        return 0;
    }

    /**
     * resolving a material from a module.
     * used to load additional data on a module instance, should be in the data part of the {@link ModuleInstance}
     *
     * @param moduleInstance
     * @return
     */
    default Material getMaterial(ModuleInstance moduleInstance) {
        return this;
    }

    /**
     * writting the material to the module instance.
     * used to write additional data to the module if needed
     *
     * @param moduleInstance
     */
    default void setMaterial(ModuleInstance moduleInstance) {
    }

    /**
     * allowing the material to change based on the ingredient, to return a variant of itself
     *
     * @param ingredient
     * @return
     */
    default Material getMaterialFromIngredient(ItemStack ingredient) {
        return this;
    }

    void addSmithingGroup();

    /**
     * the name of the Material
     *
     * @return
     */
    default Component getTranslation() {
        return Component.translatable("miapi.material." + getStringID());
    }

    @Environment(EnvType.CLIENT)
    default boolean hasIcon() {
        return false;
    }

    /**
     * Retuns all Material Properties for this key, see {@link MaterialProperties} for more details
     *
     * @param key
     * @return
     */
    Map<ModuleProperty<?>, Object> materialProperties(String key);

    /**
     * be sure to also implement {@link Material#getAllDisplayPropertyKeys()}
     * if you implement this
     *
     * @param key
     * @return
     */
    default Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
        return materialProperties(key);
    }

    /**
     * retuns all unique property keys this Material has Properties for.
     *
     * @return
     */
    List<String> getAllPropertyKeys();

    /**
     * the property keys to be displayed in the UI
     *
     * @return
     */
    default List<String> getAllDisplayPropertyKeys() {
        return getAllPropertyKeys();
    }

    /**
     * resolving a number data, used for material stats
     *
     * @param property
     * @return
     */
    double getDouble(String property);

    /**
     * resolving a string data, mostly unused
     *
     * @param property
     * @return
     */
    String getData(String property);

    /**
     * if modular converters should be generated for detected tool materials
     *
     * @return
     */
    default boolean generateConverters() {
        return false;
    }

    /**
     * the texturekeys in order of the material, some module might offer multiple variants, this is to access them
     *
     * @return
     */
    List<String> getTextureKeys();

    /**
     * a simplified integer color of the material, used for fallback rendering
     *
     * @return
     */
    @Environment(EnvType.CLIENT)
    default int getColor() {
        return getRenderController().getAverageColor().argb();
    }

    /**
     * get Crafting Value of Item, each module has a value assosiated of how much value is needed
     *
     * @param ingredient
     * @return
     */
    double getValueOfItem(ItemStack ingredient);

    /**
     * how much repair is done for an ingredient.
     * usually we only differentiate between 0 and positive values.
     *
     * @param ingredient
     * @return
     */
    default double getRepairValueOfItem(ItemStack ingredient) {
        return getValueOfItem(ingredient);
    }

    /**
     * return null if itemstack is not assosiated with the material
     * lower priority wins
     * this is to decide what Material is assosiated with this Ingredient Itemstack
     */
    @Nullable
    Double getPriorityOfIngredientItem(ItemStack ingredient);

    /**
     * creating a debug json, mostly for logging purposes
     *
     * @return
     */
    JsonObject getDebugJson();

    /**
     * the 1.21 equivalent of mining levels, a tag of blocks that are incorrect to be mined, a blacklist
     *
     * @return
     */
    TagKey<Block> getIncorrectBlocksForDrops();

    default Map<String, Map<ModuleProperty<?>, Object>> getDisplayProperty() {
        Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
        getAllDisplayPropertyKeys().forEach(s -> {
            propertyMap.put(s, getDisplayMaterialProperties(s));
        });
        return propertyMap;
    }

    default Map<String, Map<ModuleProperty<?>, Object>> getActualProperty() {
        Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
        getAllPropertyKeys().forEach(s -> {
            propertyMap.put(s, materialProperties(s));
        });
        return propertyMap;
    }

    /**
     * Complex Materials are stored differently,
     * allowing for per-material save data
     * if the optional is empty, its simply encoded and decoded
     * if its set, the codec is called when loading or saving to a module
     */
    default Optional<MapCodec<? extends Material>> codec() {
        return Optional.empty();
    }

    default Map<String, Map<ModuleProperty<?>, Object>> getHiddenProperty() {
        Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
        getAllPropertyKeys().forEach(s -> {
            var blackList = getDisplayMaterialProperties(s);
            var properties = materialProperties(s);
            Map<ModuleProperty<?>, Object> map = new HashMap<>();
            for (ModuleProperty<?> property : properties.keySet()) {
                if (!blackList.containsKey(property)) {
                    map.put(property, properties.get(property));
                }
            }
            propertyMap.put(s, map);
        });
        return propertyMap;
    }

    static Map<String, JsonElement> toJsonMap(Map<String, Map<ModuleProperty<?>, Object>> original) {
        Map<String, JsonElement> encoded = new HashMap<>();
        original.forEach((key, property) -> {
            encoded.put(key, ModuleDataPropertiesManager.createJsonFromProperties(property));
        });
        return encoded;
    }

    default List<Component> getDescription(){
        return List.of();
    }
}
