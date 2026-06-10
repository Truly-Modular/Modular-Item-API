package smartin.miapi.modules.properties.render.colorproviders;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.atlas.VertexConsumerProvider;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.SpriteColorer;
import smartin.miapi.material.palette.SpriteOverlayer;
import smartin.miapi.modules.ModuleInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class deals with recoloring models
 */
public interface ColorProvider {
    Map<String, ColorProvider> colorProviders = new HashMap<>();


    static void setup() {
        colorProviders.put("material", new MaterialColorProvider());
        colorProviders.put("model", new ModelColorProvider());
        colorProviders.put("potion", new PotionColorProvider());
        colorProviders.put("parent", new ParentColorProvider());
        colorProviders.put("item.material", new ItemMaterialColorProvider());
    }

    static ColorProvider getProvider(String type, ItemStack itemStack, ModuleInstance moduleInstance, TrimRenderer.TrimMode mode) {
        ColorProvider base = colorProviders.getOrDefault(type, colorProviders.get("material"));
        base = base.getInstance(itemStack, base.adapt(moduleInstance), mode);
        return base;
    }

    default Optional<Color> getVertexColor() {
        return Optional.empty();
    }

    default ModuleInstance adapt(ModuleInstance moduleInstance) {
        return moduleInstance;
    }

    @Environment(EnvType.CLIENT)
    void getConsumer(TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode, VertexConsumerProvider out);

    ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode);

    static boolean shouldHaveTrim(TrimRenderer.TrimMode mode) {
        return !mode.equals(TrimRenderer.TrimMode.NONE) && MiapiConfig.getClientConfig().render.enableFastTrim;
    }

    @Nullable
    static SpriteOverlayer getTrimController(Material material, TrimRenderer.TrimMode mode, ItemStack stack, SpriteColorer spriteColorer, ItemDisplayContext displayMode) {
        ArmorTrim armorTrim = stack.getComponents().get(DataComponents.TRIM);
        if (armorTrim != null) {
            Holder<ArmorMaterial> armorMaterial = (stack.getItem() instanceof ArmorItem armorItem) ? armorItem.getMaterial() : ArmorMaterials.IRON;
            TextureAtlasSprite trimSprite = switch (mode) {
                case ITEM -> {
                    if (stack.getItem() instanceof ArmorItem armorItem) {
                        if (armorItem.getType() == ArmorItem.Type.HELMET) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/helmet_trim_" + armorTrim.material().value().assetName()));
                        }
                        if (armorItem.getType() == ArmorItem.Type.CHESTPLATE) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/chestplate_trim_" + armorTrim.material().value().assetName()));
                        }
                        if (armorItem.getType() == ArmorItem.Type.LEGGINGS) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/leggings_trim_" + armorTrim.material().value().assetName()));
                        }
                        if (armorItem.getType() == ArmorItem.Type.BOOTS) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/boots_trim_" + armorTrim.material().value().assetName()));
                        }
                    }
                    yield null;
                }
                case TrimRenderer.TrimMode.ARMOR_LAYER_ONE ->
                        TrimRenderer.armorTrimsAtlas.getSprite(armorTrim.outerTexture(armorMaterial));
                case TrimRenderer.TrimMode.ARMOR_LAYER_TWO ->
                        TrimRenderer.armorTrimsAtlas.getSprite(armorTrim.innerTexture(armorMaterial));
                default -> {
                    if (stack.getItem() instanceof Equipable armorItem && displayMode.equals(ItemDisplayContext.GUI)) {
                        if (armorItem.getEquipmentSlot() == EquipmentSlot.HEAD) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/helmet_trim_" + armorTrim.material().value().assetName()));
                        }
                        if (armorItem.getEquipmentSlot() == EquipmentSlot.CHEST) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/chestplate_trim_" + armorTrim.material().value().assetName()));
                        }
                        if (armorItem.getEquipmentSlot() == EquipmentSlot.LEGS) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/leggings_trim_" + armorTrim.material().value().assetName()));
                        }
                        if (armorItem.getEquipmentSlot() == EquipmentSlot.FEET) {
                            yield TrimRenderer.atlas.getSprite(Miapi.id("minecraft:trims/items/boots_trim_" + armorTrim.material().value().assetName()));
                        }
                    }
                    yield null;
                }
            };
            if (trimSprite != null) {
                return new SpriteOverlayer(material, trimSprite, spriteColorer);
            }
        }
        return null;
    }


}
