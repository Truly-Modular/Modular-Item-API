package smartin.miapi.client.model.collision;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector2f;
import org.joml.Vector3f;

public record RayHit(float distance, Vector3f position, Vector2f uv, BakedQuad quad, TextureAtlasSprite sprite) {}
