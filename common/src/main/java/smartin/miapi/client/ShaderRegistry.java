package smartin.miapi.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.config.MiapiConfig;

import static net.minecraft.client.render.RenderPhase.*;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(EnvType.CLIENT)
public class ShaderRegistry {
    public static final Identifier customGlintTexture = new Identifier(MOD_ID, "textures/custom_glint.png");

    //public static ShaderProgram translucentMaterialShader;
    public static ShaderProgram entityTranslucentMaterialShader;
    public static ShaderProgram glintShader;

    public static RenderLayer modularItemGlint;

    public static RenderLayer TRANSLUCENT_NO_CULL;

    public static void setup(){
        if(MiapiConfig.INSTANCE.client.other.enchantingGlint){
            modularItemGlint = RenderLayer.of(
                    "miapi_glint_direct|immediatelyfast:renderlast",
                    VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS,
                    256, true, true,
                    RenderLayer.MultiPhaseParameters.builder()
                            .program(new RenderPhase.ShaderProgram(() -> {
                                if (!RenderSystem.isOnRenderThreadOrInit()) {
                                    throw new RuntimeException("attempted miapi glint setup on-non-render thread. Please report this to Truly Modular");
                                }
                                glintShader.bind();
                                //glintShader.addSampler("CustomGlintTexture",BLOCK_ATLAS_TEXTURE);
                                int id = 10;
                                RenderSystem.setShaderTexture(id, ShaderRegistry.customGlintTexture);
                                RenderSystem.bindTexture(id);
                                int j = RenderSystem.getShaderTexture(id);
                                glintShader.addSampler("CustomGlintTexture", j);
                                var a = new RenderPhase.Texture(customGlintTexture, true, false);
                                //glintShader.getUniformOrDefault("glintSize").set();
                                //NativeImage.
                                return glintShader;
                            }))
                            .texture(RenderPhase.Textures.create().add(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, false)
                                    .add(MaterialAtlasManager.MATERIAL_ID, false, false).build())
                            .depthTest(EQUAL_DEPTH_TEST)
                            .transparency(GLINT_TRANSPARENCY)
                            .lightmap(ENABLE_LIGHTMAP)
                            //.cull(DISABLE_CULLING)
                            .writeMaskState(COLOR_MASK)
                            .texturing(RenderLayer.ENTITY_GLINT_TEXTURING)
                            .overlay(ENABLE_OVERLAY_COLOR).build(false));

            TRANSLUCENT_NO_CULL = RenderLayer.of(
                    "miapi_translucent_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
                    0x200000, true, true, RenderLayer.MultiPhaseParameters.builder()
                            .lightmap(ENABLE_LIGHTMAP).program(TRANSLUCENT_PROGRAM).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY)
                            .target(TRANSLUCENT_TARGET).cull(DISABLE_CULLING).build(true));
        }
    }

    private static void setupGlintTexturing(float scale) {
        long l = (long) ((double) Util.getMeasuringTimeMs() * MinecraftClient.getInstance().options.getGlintSpeed().getValue() * 8.0);
        float f = (float) (l % 110000L) / 110000.0f;
        float g = (float) (l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-f, g, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }
}
