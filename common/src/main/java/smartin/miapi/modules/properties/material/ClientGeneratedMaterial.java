package smartin.miapi.modules.properties.material;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
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
import smartin.miapi.registries.FakeTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ClientGeneratedMaterial extends GeneratedMaterial {
    public MaterialPalette materialPalette;
    @Nullable
    public MaterialIcons.MaterialIcon icon;

    public ClientGeneratedMaterial(ToolMaterial toolMaterial) {
        super(toolMaterial);
        clientSetup();
    }

    public ClientGeneratedMaterial(ToolMaterial toolMaterial, ItemStack itemStack) {
        super(toolMaterial, itemStack);
        clientSetup();
    }

    public void copyStatsFrom(Material other) {
        materialStats.put("hardness", other.getDouble("hardness"));
        materialStats.put("density", other.getDouble("density"));
        materialStats.put("flexibility", other.getDouble("flexibility"));
        materialStats.put("durability", other.getDouble("durability"));
        materialStats.put("mining_level", other.getDouble("mining_level"));
        materialStats.put("mining_speed", other.getDouble("mining_speed"));
        addFakeTranslationForCopy();
    }

    public void addFakeTranslationForCopy() {
        String materialTranslation = Text.translatable(mainIngredient.getTranslationKey()).getString();
        String translationKey = "miapi.material.generated." + mainIngredient.getItem().getTranslationKey();
        if (!materialTranslation.endsWith(" ")) {
            materialTranslation += " ";
        }
        FakeTranslation.translations.put(translationKey, materialTranslation);
        materialStatsString.put("translation", translationKey);
    }

    public boolean hasIcon() {
        return true;
    }

    public int renderIcon(DrawContext drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
    }

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
    public MaterialPalette getPalette() {
        return materialPalette;
    }

    @Override
    public List<String> getTextureKeys() {
        List<String> keys = new ArrayList<>(this.groups);
        keys.add("default");
        return keys;
    }

    @Override
    public boolean assignStats(List<ToolItem> toolItems) {
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
                generateTranslation(toolMaterials);
                MiapiEvents.GENERATED_MATERIAL.invoker().generated(this, mainIngredient, toolMaterials, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getColor() {
        return 0;
    }
}
