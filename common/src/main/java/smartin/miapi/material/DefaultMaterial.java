package smartin.miapi.material;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.FallbackColorer;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultMaterial implements Material {
    public static ResourceLocation DEFAULT_ID = Miapi.id("default_runtime_material");

    @Override
    public ResourceLocation getID() {
        return DEFAULT_ID;
    }

    @Override
    public List<String> getGroups() {
        return Collections.emptyList();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode) {
        return new FallbackColorer(this);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int renderIcon(GuiGraphics drawContext, int x, int y) {
        // No icon rendering for default material
        return 0;
    }

    @Override
    public Material getMaterial(ModuleInstance moduleInstance) {
        return this; // Returns itself as it doesn't change
    }

    @Override
    public void setMaterial(ModuleInstance moduleInstance) {
        // No-op as it has no state to set
    }

    @Override
    public Material getMaterialFromIngredient(ItemStack ingredient) {
        return this; // Always returns itself
    }

    @Override
    public void addSmithingGroup() {

    }

    @Override
    public Component getTranslation() {
        return Component.translatable("miapi.material.default_material");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean hasIcon() {
        return false; // No icon
    }

    @Override
    public Map<ModuleProperty<?>, Object> materialProperties(String key) {
        return Collections.emptyMap();
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return Collections.emptyList(); // No property keys
    }

    @Override
    public double getDouble(String property) {
        return 0; // Default value
    }

    @Override
    public String getData(String property) {
        return ""; // Default value
    }

    @Override
    public boolean generateConverters() {
        return false; // Does not generate converters
    }

    @Override
    public List<String> getTextureKeys() {
        return Collections.EMPTY_LIST; // No texture keys
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColor(ModuleInstance context) {
        return FastColor.ARGB32.color(255, 255, 255, 255); // Default white color
    }

    @Override
    public double getValueOfItem(ItemStack ingredient) {
        return 0; // Default value
    }

    @Override
    public double getRepairValueOfItem(ItemStack ingredient) {
        return 0; // Default value
    }

    @Nullable
    @Override
    public Double getPriorityOfIngredientItem(ItemStack ingredient) {
        return null; // No priority
    }

    @Override
    public JsonObject getDebugJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", getID().toString());
        return jsonObject; // Minimal debug information
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultMaterial d) {
            return d.getID().equals(getID());
        }
        return super.equals(obj);
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL; // No specific blocks
    }

    @Override
    public Optional<MapCodec<? extends Material>> codec() {
        return Optional.empty(); // No codec provided
    }
}
