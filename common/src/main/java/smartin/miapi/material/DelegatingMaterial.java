package smartin.miapi.material;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DelegatingMaterial implements Material {
    public final Material parent;

    public DelegatingMaterial(Material parent) {
        this.parent = parent;
    }

    @Override
    public ResourceLocation getID() {
        return parent.getID();
    }

    @Override
    public List<String> getGroups() {
        return parent.getGroups();
    }

    @Override
    public List<String> getGuiGroups() {
        return parent.getGuiGroups();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController(ModuleInstance context,  ItemDisplayContext mode) {
        return parent.getRenderController(context, mode);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int renderIcon(GuiGraphics drawContext, int x, int y) {
        return parent.renderIcon(drawContext, x, y);
    }

    @Override
    public Material getMaterial(ModuleInstance moduleInstance) {
        return parent.getMaterial(moduleInstance);
    }

    @Override
    public void setMaterial(ModuleInstance moduleInstance) {
        parent.setMaterial(moduleInstance);
    }

    @Override
    public Material getMaterialFromIngredient(ItemStack ingredient) {
        return parent.getMaterialFromIngredient(ingredient);
    }

    @Override
    public void addSmithingGroup() {
        parent.addSmithingGroup();
    }

    @Override
    public Component getTranslation() {
        return parent.getTranslation();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean hasIcon() {
        return parent.hasIcon();
    }

    @Override
    public Map<ModuleProperty<?>, Object> materialProperties(String key) {
        return parent.materialProperties(key);
    }

    @Override
    public Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
        return parent.getDisplayMaterialProperties(key);
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return parent.getAllPropertyKeys();
    }

    @Override
    public List<String> getAllDisplayPropertyKeys() {
        return parent.getAllDisplayPropertyKeys();
    }

    @Override
    public double getDouble(String property) {
        return parent.getDouble(property);
    }

    @Override
    public String getData(String property) {
        return parent.getData(property);
    }

    @Override
    public boolean generateConverters() {
        return parent.generateConverters();
    }

    @Override
    public List<String> getTextureKeys() {
        return parent.getTextureKeys();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColor(ModuleInstance context) {
        return parent.getColor(context);
    }

    @Override
    public double getValueOfItem(ItemStack ingredient) {
        return parent.getValueOfItem(ingredient);
    }

    @Override
    public double getRepairValueOfItem(ItemStack ingredient) {
        return parent.getRepairValueOfItem(ingredient);
    }

    @Nullable
    @Override
    public Double getPriorityOfIngredientItem(ItemStack ingredient) {
        return parent.getPriorityOfIngredientItem(ingredient);
    }

    @Override
    public JsonObject getDebugJson() {
        return parent.getDebugJson();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return parent.getIncorrectBlocksForDrops();
    }

    @Override
    public Map<String, Map<ModuleProperty<?>, Object>> getDisplayProperty() {
        return parent.getDisplayProperty();
    }

    @Override
    public Map<String, Map<ModuleProperty<?>, Object>> getActualProperty() {
        return parent.getActualProperty();
    }

    @Override
    public Optional<MapCodec<? extends Material>> codec() {
        return parent.codec();
    }

    @Override
    public Map<String, Map<ModuleProperty<?>, Object>> getHiddenProperty() {
        return parent.getHiddenProperty();
    }

    @Override
    public List<Component> getDescription(boolean extended){
        return parent.getDescription(extended);
    }

    @Override
    public int hashCode(){
        return getID().hashCode();
    }
}
