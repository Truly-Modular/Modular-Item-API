package smartin.miapi.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.redpxnda.nucleus.impl.ShaderRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.atlas.MaterialAtlasManager;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(EnvType.CLIENT)
public class GlintShader {
    public static final ResourceLocation customGlintTexture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/custom_glint.png");

    //public static ShaderProgram translucentMaterialShader;
    public static ShaderInstance entityTranslucentMaterialShader;
    public static ShaderInstance glintShader;

    public static final RenderType modularItemGlint = RenderType.create(
            "miapi_glint_direct|immediatelyfast:renderlast",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256, true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> {
                        if (!RenderSystem.isOnRenderThreadOrInit()) {
                            throw new RuntimeException("attempted miapi glint setup on-non-render thread. Please report this to Truly Modular");
                        }
                        glintShader.apply();
                        //glintShader.addSampler("CustomGlintTexture",BLOCK_ATLAS_TEXTURE);
                        int id = 10;
                        RenderSystem.setShaderTexture(id, GlintShader.customGlintTexture);
                        RenderSystem.bindTexture(id);
                        int j = RenderSystem.getShaderTexture(id);
                        glintShader.setSampler("CustomGlintTexture", j);
                        var a = new RenderStateShard.TextureStateShard(customGlintTexture, true, false);
                        //glintShader.getUniformOrDefault("glintSize").set();
                        //NativeImage.
                        return glintShader;
                    }))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TextureAtlas.LOCATION_BLOCKS, false, false)
                            .add(MaterialAtlasManager.MATERIAL_ID, false, false).build())
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    //.cull(DISABLE_CULLING)
                    .setWriteMaskState(COLOR_WRITE)
                    .setTexturingState(RenderType.ENTITY_GLINT_TEXTURING)
                    .setOverlayState(OVERLAY).createCompositeState(false));

    public static final RenderType TRANSLUCENT_NO_CULL = RenderType.create(
            "miapi_translucent_no_cull", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            0x200000, true, true, RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_TRANSLUCENT_SHADER).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(TRANSLUCENT_TARGET).setCullState(NO_CULL).createCompositeState(true));

    private static void setupGlintTexturing(float scale) {
        long l = (long) ((double) Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0);
        float f = (float) (l % 110000L) / 110000.0f;
        float g = (float) (l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-f, g, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    public static void setupItem(Matrix4f matrix4f){
        //TODO:rework glint rendering as a whole
        //GlintShader.glintShader.safeGetUniform("ModelMat").set(new Matrix4f(matrix4f));
    }

    public static void registerShaders() {
        /*ShaderRegistry.register(
                new Identifier(Miapi.MOD_ID, "rendertype_translucent_material"),
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, s -> RegistryInventory.Client.translucentMaterialShader = s);*/

        ShaderRegistry.register(
                Miapi.id( "rendertype_entity_translucent_material"),
                DefaultVertexFormat.NEW_ENTITY, s -> entityTranslucentMaterialShader = s);
        ShaderRegistry.register(
                Miapi.id("rendertype_item_glint"),
                DefaultVertexFormat.NEW_ENTITY, s -> glintShader = s);
    }
}
