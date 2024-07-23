package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.codec.misc.PolyCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

public class MaterialIcons {
    public static final Map<String, MaterialIconCreator> iconCreators = new HashMap<>();
    public static MaterialIconCreator textureIconCreator;

    public static MaterialIcon getMaterialIcon(String materialKey, JsonElement element) {
        return iconCreators.get(element.getAsJsonObject().get("type").getAsString()).create(element, materialKey);
    }

    public static void setup() {
        textureIconCreator = (element, mat) -> {
            if (!(element instanceof JsonObject object))
                throw new RuntimeException("JSON data for the icon of the '" + mat + "' material is not a JSON object! -> " + element);

            if (!(object.get("path") instanceof JsonPrimitive primitive) || !primitive.isString())
                throw new RuntimeException("'path' field for the icon of the '" + mat + "' material is either missing, or not a string! -> " + element);

            return new TextureMaterialIcon(ResourceLocation.parse(primitive.getAsString()));
        };
        iconCreators.put("texture", textureIconCreator);

        Codec<ItemStack> byItem = BuiltInRegistries.ITEM.byNameCodec().xmap(Item::getDefaultInstance, ItemStack::getItem);
        iconCreators.put("item", (element, mat) -> {
            if (!(element instanceof JsonObject object))
                throw new RuntimeException("JSON data for the icon of the '" + mat + "' material is not a JSON object! -> " + element);

            if (!object.has("item"))
                throw new RuntimeException("'item' field for the icon of the '" + mat + "' material is missing! -> " + element);

            ItemStack stack = MiscCodecs.quickParse(object.get("item"), PolyCodec.of(byItem, ItemStack.CODEC),
                    s -> new RuntimeException("Failed to parse item for the icon of the '" + mat + "' material! -> " + s));
            int offset = object.get("offset") instanceof JsonPrimitive prim ? prim.getAsInt() : 16;
            SpinSettings spin = object.has("spin") ?
                    MiscCodecs.quickParse(object.get("spin"), SpinSettings.CODEC,
                            s -> new RuntimeException("Failed to parse spin settings for item icon of the '" + mat + "' material! -> " + s)) :
                    null;

            return new ItemMaterialIcon(stack, offset, spin);
        });

        iconCreators.put("entity", (element, materialKey) -> {
            EntityIconHolder holder = MiscCodecs.quickParse(
                    element, EntityIconHolder.codec,
                    s -> new RuntimeException("Failed to parse entity icon for the '" + materialKey + "' material! -> " + s)
            );
            return new EntityMaterialIcon(holder);
        });
    }

    public interface MaterialIcon {
        /**
         * @param context draw context for use
         * @param x       x pos of icon
         * @param y       y pos of icon
         * @return amount to offset for text rendering
         */
        @Environment(EnvType.CLIENT)
        int render(GuiGraphics context, int x, int y);
    }

    public interface MaterialIconCreator {
        MaterialIcon create(JsonElement element, String materialKey);
    }

    public static class EntityIconHolder {
        public static final Codec<EntityIconHolder> codec = AutoCodec.of(EntityIconHolder.class).codec();

        public @AutoCodec.Ignored Entity actual = null;
        public EntityType<?> entity;
        public @CodecBehavior.Optional int offset = 16;
        public @CodecBehavior.Optional float x = 0;
        public @CodecBehavior.Optional float y = 0;
        public @CodecBehavior.Optional float scale = 0.85f;
        public @CodecBehavior.Optional SpinSettings spin = null;
    }

    public record EntityMaterialIcon(EntityIconHolder holder) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(GuiGraphics context, int x, int y) {
            renderRotatingEntity(context, x, y, 0, holder);
            return holder.offset;
        }
    }

    public record TextureMaterialIcon(ResourceLocation texture) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(GuiGraphics context, int x, int y) {
            context.blit(texture, x, y, 0, 0, 16, 16, 16, 16);
            return 16;
        }
    }

    public record ItemMaterialIcon(ItemStack item, int offset, SpinSettings spin) implements MaterialIcon {
        @Environment(EnvType.CLIENT)
        @Override
        public int render(GuiGraphics context, int x, int y) {
            if (spin != null) renderRotatingItem(context, item, x, y, 0, spin);
            else context.renderItem(item, x, y);
            return offset;
        }
    }

    @CodecBehavior.Override("codec")
    @AutoCodec.Settings(defaultOptionalBehavior = @CodecBehavior.Optional)
    public static class SpinSettings {
        public static final Codec<SpinSettings> CODEC = AutoCodec.of(SpinSettings.class).codec();

        public boolean x = false;
        public boolean y = true;
        public boolean z = false;
        public int originX = 0;
        public int originY = 0;
        public int originZ = 0;
        public float speed = 1;

        public void multiplyMatrices(PoseStack matrices) {
            float amount = Util.getMillis() * (0.0001745f * speed);
            matrices.rotateAround(new Quaternionf().rotationXYZ(x ? amount : 0, y ? amount : 0, z ? amount : 0), originX, originY, originZ);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void renderRotatingItem(GuiGraphics context, ItemStack stack, int x, int y, int z, SpinSettings spin) {
        if (stack.isEmpty()) {
            return;
        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(stack, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);
        context.pose().pushPose();
        context.pose().translate(x + 8, y + 8, 150 + (bakedModel.isGui3d() ? z : 0));
        boolean bl = !bakedModel.usesBlockLight();
        context.pose().mulPose(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        context.pose().scale(16.0f, 16.0f, 16.0f);

        spin.multiplyMatrices(context.pose());

        if (bl) {
            Lighting.setupForFlatItems();
        }
        itemRenderer.render(stack, ItemDisplayContext.GUI, false, context.pose(), context.bufferSource(), 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel);
        context.flush();
        if (bl) {
            Lighting.setupFor3DItems();
        }
        context.pose().popPose();
    }

    @Environment(EnvType.CLIENT)
    public static void renderRotatingEntity(GuiGraphics context, int x, int y, int z, EntityIconHolder holder) {
        if (holder.actual == null) holder.actual = holder.entity.create(Minecraft.getInstance().level);

        EntityRenderDispatcher renderer = Minecraft.getInstance().getEntityRenderDispatcher();
        context.pose().pushPose();
        context.pose().translate(x + 8 + holder.x, y + 16 - holder.y, 150 + z);
        context.pose().mulPose(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        context.pose().scale(16.0f, 16.0f, 16.0f);
        context.pose().scale(holder.scale, holder.scale, holder.scale);

        if (holder.spin != null) {
            holder.spin.multiplyMatrices(context.pose());
        }

        renderer.render(holder.actual, 0, 0, 0, 0,
                0, context.pose(),
                Minecraft.getInstance().renderBuffers().bufferSource(), LightTexture.FULL_BRIGHT);
        context.flush();
        context.pose().popPose();
    }
}
