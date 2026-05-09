package smartin.miapi.modules.properties.render.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.BannerMiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.properties.MaterialInscribeDataProperty;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Property that attaches banner overlays to existing models.
 * Uses the attached overlay system.
 */
public class BannerOverlayProperty extends AttachedModelProperty<BannerOverlayProperty.BannerOverlayData> {
    public static final ResourceLocation KEY = Miapi.id("banner_overlay");
    public static BannerOverlayProperty property;

    public static final MapCodec<BannerOverlayData> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(d -> d.type),
                    Codec.STRING.optionalFieldOf("model",KEY.toString()).forGetter(d -> d.model)
            ).apply(instance, BannerOverlayData::new)
    );

    public BannerOverlayProperty() {
        super(DATA_CODEC);
        property = this;
    }

    public static class BannerOverlayData extends CustomData {
        public final String type;
        public final String model;

        public BannerOverlayData(String type, String model) {
            this.type = type;
            this.model = model;
        }

        @Override
        public void preload() {
            // Preload banner texture or model variant if necessary
            //BannerMiapiModel.preload(model);
        }

        /**
         * Creates banner overlay models based on this data.
         */
        @Override
        @Nullable
        public List<MiapiModel> createModel(ItemStack stack, ModuleInstance base, ModuleInstance source, ModelHolder holder, ItemDisplayContext context) {
            List<MiapiModel> result = new ArrayList<>();

            Supplier<ItemStack> stackSupplier = switch (type) {
                case "item_nbt" -> () -> stack;
                case "module_data" -> () -> MaterialInscribeDataProperty.readStackFromModuleInstance(source, model);
                default -> () -> ItemStack.EMPTY;
            };

            ItemStack bannerStack = stackSupplier.get();
            if (bannerStack.isEmpty()) return Collections.emptyList();

            BannerMiapiModel.BannerMode mode = BannerMiapiModel.getMode(model);

            return result;
        }

        /**
         * Extracts the texture sprites used by this banner overlay.
         */
        public List<TextureAtlasSprite> extractSprites(ItemStack stack, ModuleInstance module) {
            ItemStack bannerStack = switch (type) {
                case "item_nbt" -> stack;
                case "module_data" -> MaterialInscribeDataProperty.readStackFromModuleInstance(module, KEY.toString());
                default -> ItemStack.EMPTY;
            };

            if (bannerStack.isEmpty()) return List.of();

            BannerMiapiModel.BannerMode mode = BannerMiapiModel.getMode(model);
            BannerMiapiModel bannerModel = BannerMiapiModel.getFromStack(bannerStack, mode, Transform.IDENTITY.toMatrix());

            if (bannerModel != null) {
                //return bannerModel.getAllSprites();
            }

            return List.of();
        }
    }
}
