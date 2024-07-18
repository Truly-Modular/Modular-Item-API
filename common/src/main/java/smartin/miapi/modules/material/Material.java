package smartin.miapi.modules.material;

import com.google.gson.JsonObject;
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
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.palette.MaterialRenderController;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

import static smartin.miapi.Miapi.MOD_ID;

public interface Material {
    ResourceLocation BASE_PALETTE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "miapi_materials/base_palette");

    String getKey();

    @Environment(EnvType.CLIENT)
    static int getColor(String color) {
        if (color.equals("")) return FastColor.ARGB32.color(255, 255, 255, 255);
        long longValue = Long.parseLong(color, 16);
        return (int) (longValue & 0xffffffffL);
    }

    List<String> getGroups();

    default List<String> getGuiGroups(){
        return getGroups();
    }

    @Environment(EnvType.CLIENT)
    MaterialRenderController getRenderController();

    /**
     * @param drawContext a DrawContext that can be used to draw shtuff
     * @param x x pos of the icon
     * @param y y pos of the icon
     * @return how much to offset the text rendering by
     */
    @Environment(EnvType.CLIENT)
    default int renderIcon(GuiGraphics drawContext, int x, int y) {
        return 0;
    }

    default Material getMaterial(ModuleInstance moduleInstance){
        return this;
    }

    default Material getMaterialFromIngredient(ItemStack ingredient){
        return this;
    }

    default Component getTranslation(){
        return Component.translatable(getData("translation"));
    }

    @Environment(EnvType.CLIENT)
    default boolean hasIcon() {
        return false;
    }

    /**
     * Retuns all Material Properties for this key, see {@link MaterialProperties} for more details
     * @param key
     * @return
     */
    Map<ModuleProperty<?>, Object> materialProperties(String key);

    /**
     * be sure to also implement {@link Material#getAllDisplayPropertyKeys()}
     * if you implement this
     * @param key
     * @return
     */
    default Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
        return materialProperties(key);
    }

    /**
     * retuns all unique property keys this Material has Properties for.
     * @return
     */
    List<String> getAllPropertyKeys();

    default List<String> getAllDisplayPropertyKeys() {
        return getAllPropertyKeys();
    }

    double getDouble(String property);

    String getData(String property);

    default boolean generateConverters(){
        return false;
    }

    List<String> getTextureKeys();

    @Environment(EnvType.CLIENT)
    default int getColor() {
        return getRenderController().getAverageColor().argb();
    }

    double getValueOfItem(ItemStack itemStack);

    /**
     * return null if itemstack is not assosiated with the material
     */
    @Nullable
    Double getPriorityOfIngredientItem(ItemStack itemStack);

    JsonObject getDebugJson();

    TagKey<Block> getIncorrectBlocksForDrops();
}
