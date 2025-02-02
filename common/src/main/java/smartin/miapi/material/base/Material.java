package smartin.miapi.material.base;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import smartin.miapi.client.gui.crafting.crafter.replace.hover.HoverMaterialList;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.modules.properties.LoreProperty.gray;

public interface Material extends PropertyController, ColorController, StatController, IngredientController {
    ResourceLocation BASE_PALETTE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "miapi_materials/base_palette");

    ResourceLocation getID();

    default String getStringID() {
        String id = getID().toString();
        id = id.replace(":", ".");
        id = id.replaceAll("/", ".");
        return id;
    }

    List<String> getGroups();

    default List<String> getGuiGroups() {
        return getGroups();
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
     * allowing the material to change based on the ingredient, to return a variant of itself
     *
     * @param ingredient
     * @return
     */
    default Material getMaterialFromIngredient(ItemStack ingredient) {
        return this;
    }

    default boolean canBeDyed() {
        return false;
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

    /**
     * if modular converters should be generated for detected tool materials
     *
     * @return
     */
    default boolean generateConverters() {
        return false;
    }

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

    /**
     * Complex Materials are stored differently,
     * allowing for per-material save data
     * if the optional is empty, its simply encoded and decoded
     * if its set, the codec is called when loading or saving to a module
     */
    default Optional<MapCodec<? extends Material>> codec() {
        return Optional.empty();
    }

    default List<Component> getDescription(boolean extended) {
        List<Component> lines = new ArrayList<>();
        if (extended) {
            lines.add(gray(Component.translatable("miapi.ui.material_desc_alt_2")));
            for (int i = 1; i < this.getGuiGroups().size(); i++) {
                String groupId = this.getGuiGroups().get(i);
                lines.add(gray(Component.literal(" - " + HoverMaterialList.getTranslation(groupId).getString())));
            }
        }
        return lines;
    }
}
