package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.util.InterfaceDispatcher;
import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.codec.misc.PolyCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.Map;

public class MaterialIcons {
    public static final Map<String, MaterialIconCreator> iconCreators = new HashMap<>();
    private static final InterfaceDispatcher<MaterialIconCreator> dispatcher = InterfaceDispatcher.of(iconCreators, "type");
    public static MaterialIconCreator textureIconCreator;

    public static MaterialIcon getMaterialIcon(String materialKey, JsonElement element) {
        return dispatcher.dispatcher().create(element, materialKey);
    }

    public static void setup() {
        textureIconCreator = (element, mat) -> {
            if (!(element instanceof JsonObject object))
                throw new RuntimeException("JSON data for the icon of the '" + mat + "' material is not a JSON object! -> " + element);

            if (!(object.get("path") instanceof JsonPrimitive primitive) || !primitive.isString())
                throw new RuntimeException("'path' field for the icon of the '" + mat + "' material is either missing, or not a string! -> " + element);

            return new TextureMaterialIcon(new Identifier(primitive.getAsString()));
        };
        iconCreators.put("texture", textureIconCreator);

        Codec<ItemStack> byItem = Registries.ITEM.getCodec().xmap(Item::getDefaultStack, ItemStack::getItem);
        iconCreators.put("item", (element, mat) -> {
            if (!(element instanceof JsonObject object))
                throw new RuntimeException("JSON data for the icon of the '" + mat + "' material is not a JSON object! -> " + element);

            if (!object.has("item"))
                throw new RuntimeException("'item' field for the icon of the '" + mat + "' material is missing! -> " + element);

            ItemStack stack = MiscCodecs.quickParse(object.get("item"), PolyCodec.of(byItem, ItemStack.CODEC),
                    s -> Miapi.LOGGER.error("Failed to parse item for the icon of the '{}' material! -> {}", mat, s));
            int offset = object.get("offset") instanceof JsonPrimitive prim ? prim.getAsInt() : 16;
            SpinSettings spin = object.has("spin") ?
                    MiscCodecs.quickParse(object.get("spin"), SpinSettings.codec,
                            s -> Miapi.LOGGER.error("Failed to parse spin settings for item icon of the '{}' material! -> {}", mat, s)) :
                    null;

            return new ItemMaterialIcon(stack, offset, spin);
        });

        iconCreators.put("entity", (element, materialKey) -> {
            EntityIconHolder holder = MiscCodecs.quickParse(
                    element, EntityIconHolder.codec,
                    s -> Miapi.LOGGER.error("Failed to parse entity icon for the '{}' material! -> {}", materialKey, s)
            );
            return new EntityMaterialIcon(holder);
        });
    }

    public interface MaterialIcon {
        /**
         * @param context draw context for use
         * @param x x pos of icon
         * @param y y pos of icon
         * @return amount to offset for text rendering
         */
        @Environment(EnvType.CLIENT)
        int render(DrawContext context, int x, int y);
    }
    public interface MaterialIconCreator {
        MaterialIcon create(JsonElement element, String materialKey);
    }

    public static class EntityIconHolder {
        public static final Codec<EntityType<?>> entityTypeCodec = Registries.ENTITY_TYPE.getCodec();
        public static final Codec<EntityIconHolder> codec = AutoCodec.of(EntityIconHolder.class).codec();

        public @AutoCodec.Ignored Entity actual = null;
        public @AutoCodec.Override("entityTypeCodec") EntityType<?> entity;
        public @AutoCodec.Optional int offset = 16;
        public @AutoCodec.Optional float x = 0;
        public @AutoCodec.Optional float y = 0;
        public @AutoCodec.Optional float scale = 0.85f;
        public @AutoCodec.Optional SpinSettings spin = null;
    }
    public record EntityMaterialIcon(EntityIconHolder holder) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(DrawContext context, int x, int y) {
            renderRotatingEntity(context, x, y, 0, holder);
            return holder.offset;
        }
    }

    public record TextureMaterialIcon(Identifier texture) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(DrawContext context, int x, int y) {
            context.drawTexture(texture, x, y, 0, 0, 16, 16, 16, 16);
            return 16;
        }
    }

    public record ItemMaterialIcon(ItemStack item, int offset, SpinSettings spin) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(DrawContext context, int x, int y) {
            if (spin != null) renderRotatingItem(context, item, x, y, 0, spin);
            else context.drawItem(item, x, y);
            return offset;
        }
    }

    @AutoCodec.Override("codec")
    @AutoCodec.Settings(optionalByDefault = true)
    public static class SpinSettings {
        public static final Codec<SpinSettings> codec = AutoCodec.of(SpinSettings.class).codec();

        public boolean x = false;
        public boolean y = true;
        public boolean z = false;
        public int originX = 0;
        public int originY = 0;
        public int originZ = 0;
        public float speed = 1;

        public void multiplyMatrices(MatrixStack matrices) {
            float amount = Util.getMeasuringTimeMs()*(0.0001745f*speed);
            matrices.multiply(new Quaternionf().rotationXYZ(x ? amount : 0, y ? amount : 0, z ? amount : 0), originX, originY, originZ);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void renderRotatingItem(DrawContext context, ItemStack stack, int x, int y, int z, SpinSettings spin) {
        if (stack.isEmpty()) {
            return;
        }
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(stack, MinecraftClient.getInstance().world, MinecraftClient.getInstance().player, 0);
        context.getMatrices().push();
        context.getMatrices().translate(x + 8, y + 8, 150 + (bakedModel.hasDepth() ? z : 0));
        boolean bl = !bakedModel.isSideLit();
        context.getMatrices().multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        context.getMatrices().scale(16.0f, 16.0f, 16.0f);

        spin.multiplyMatrices(context.getMatrices());

        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }
        itemRenderer.renderItem(stack, ModelTransformationMode.GUI, false, context.getMatrices(), context.getVertexConsumers(), 0xF000F0, OverlayTexture.DEFAULT_UV, bakedModel);
        context.draw();
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }
        context.getMatrices().pop();
    }

    @Environment(EnvType.CLIENT)
    public static void renderRotatingEntity(DrawContext context, int x, int y, int z, EntityIconHolder holder) {
        if (holder.actual == null) holder.actual = holder.entity.create(MinecraftClient.getInstance().world);

        EntityRenderDispatcher renderer = MinecraftClient.getInstance().getEntityRenderDispatcher();
        context.getMatrices().push();
        context.getMatrices().translate(x + 8 + holder.x, y + 16 - holder.y, 150 + z);
        context.getMatrices().multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        context.getMatrices().scale(16.0f, 16.0f, 16.0f);
        context.getMatrices().scale(holder.scale, holder.scale, holder.scale);

        if (holder.spin != null) {
            holder.spin.multiplyMatrices(context.getMatrices());
        }

        renderer.render(holder.actual, 0, 0, 0, 0,
                0, context.getMatrices(),
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        context.draw();
        context.getMatrices().pop();
    }
}
