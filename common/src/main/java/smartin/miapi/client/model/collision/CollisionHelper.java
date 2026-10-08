package smartin.miapi.client.model.collision;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import smartin.miapi.mixin.client.SpriteContentsAccessor;

import java.util.List;
import java.util.Optional;

public class CollisionHelper {

    public static Optional<RayHit> raycast(Ray ray, List<Pair<TextureAtlasSprite, List<BakedQuad>>> batches, Matrix4f modelToRaySpace) {
        if (Math.abs(modelToRaySpace.determinant()) < TriangleHitHelper.RAY_EPSILON) {return Optional.empty();}
        Matrix4f inverse = new Matrix4f(modelToRaySpace).invert();
        Ray localRay = ray.transform(inverse);
        float closestDistance = Float.POSITIVE_INFINITY;
        RayHit closestHit = null;
        for (Pair<TextureAtlasSprite, List<BakedQuad>> batch : batches) {
            TextureAtlasSprite sprite = batch.getFirst();
            for (BakedQuad quad : batch.getSecond()) {
                int[] vertices = quad.getVertices();
                if (vertices.length < FaceBakery.VERTEX_INT_SIZE * 4) {continue;}
                Vector3f p0 = readPosition(vertices, 0);
                Vector3f p1 = readPosition(vertices, 1);
                Vector3f p2 = readPosition(vertices, 2);
                Vector3f p3 = readPosition(vertices, 3);
                Vector2f uv0 = readUv(vertices, 0);
                Vector2f uv1 = readUv(vertices, 1);
                Vector2f uv2 = readUv(vertices, 2);
                Vector2f uv3 = readUv(vertices, 3);
                TriangleHitHelper.TriangleHit hit = TriangleHitHelper.intersectTriangle(localRay, p0, p1, p2);
                Vector2f hitUv = null;
                if (hit != null) {hitUv = interpolateUv(uv0, uv1, uv2, hit.u(), hit.v());} else {
                    hit = TriangleHitHelper.intersectTriangle(localRay, p0, p2, p3);
                    if (hit != null) {hitUv = interpolateUv(uv0, uv2, uv3, hit.u(), hit.v());}
                }
                if (hit == null || hitUv == null) {
                    continue;
                }
                if (!isOpaque(sprite, hitUv.x, hitUv.y)) {
                    continue;
                }
                Vector3f localPosition = localRay.at(hit.t());
                Vector3f position = modelToRaySpace.transformPosition(localPosition, new Vector3f());
                float distance = ray.distanceTo(position);
                if (distance < 0.0f) {continue;}
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestHit = new RayHit(distance, position, hitUv, quad, sprite);
                }
            }
        }
        return Optional.ofNullable(closestHit);
    }

    public static void renderRedQuads(List<Pair<TextureAtlasSprite, List<BakedQuad>>> batches, Matrix4f modelToRaySpace, MultiBufferSource bufferSource, int randomColor) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugQuads());

        for (Pair<TextureAtlasSprite, List<BakedQuad>> batch : batches) {
            TextureAtlasSprite sprite = batch.getFirst();

            int width = sprite.contents().width();
            int height = sprite.contents().height();

            for (BakedQuad quad : batch.getSecond()) {
                int[] vertices = quad.getVertices();
                if (vertices.length < FaceBakery.VERTEX_INT_SIZE * 4) {
                    continue;
                }

                Vector3f p0 = readPosition(vertices, 0);
                Vector3f p1 = readPosition(vertices, 1);
                Vector3f p2 = readPosition(vertices, 2);
                Vector3f p3 = readPosition(vertices, 3);

                Vector2f uv0 = readUv(vertices, 0);
                Vector2f uv1 = readUv(vertices, 1);
                Vector2f uv2 = readUv(vertices, 2);
                Vector2f uv3 = readUv(vertices, 3);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        float u0 = (float) x / width;
                        float v0 = (float) y / height;
                        float u1 = (float) (x + 1) / width;
                        float v1 = (float) (y + 1) / height;

                        float uc = (u0 + u1) * 0.5f;
                        float vc = (v0 + v1) * 0.5f;

                        float textureU = bilinearUv(uc, vc, uv0.x, uv1.x, uv2.x, uv3.x);

                        float textureV = bilinearUv(uc, vc, uv0.y, uv1.y, uv2.y, uv3.y);

                        if (!isOpaque(sprite, textureU, textureV)) {
                            continue;
                        }

                        Vector3f a = bilinearPosition(p0, p1, p2, p3, u0, v0);
                        Vector3f b = bilinearPosition(p0, p1, p2, p3, u1, v0);
                        Vector3f c = bilinearPosition(p0, p1, p2, p3, u1, v1);
                        Vector3f d = bilinearPosition(p0, p1, p2, p3, u0, v1);

                        buffer.addVertex(modelToRaySpace, a.x, a.y, a.z).setColor(randomColor);
                        buffer.addVertex(modelToRaySpace, b.x, b.y, b.z).setColor(randomColor);
                        buffer.addVertex(modelToRaySpace, c.x, c.y, c.z).setColor(randomColor);
                        buffer.addVertex(modelToRaySpace, d.x, d.y, d.z).setColor(randomColor);
                    }
                }
            }
        }
    }

    private static Vector3f bilinearPosition(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float u, float v) {
        float w0 = (1.0f - u) * (1.0f - v);
        float w1 = u * (1.0f - v);
        float w2 = u * v;
        float w3 = (1.0f - u) * v;

        return new Vector3f(p0.x * w0 + p1.x * w1 + p2.x * w2 + p3.x * w3, p0.y * w0 + p1.y * w1 + p2.y * w2 + p3.y * w3, p0.z * w0 + p1.z * w1 + p2.z * w2 + p3.z * w3);
    }

    private static float bilinearUv(float u, float v, float a, float b, float c, float d) {
        return a * (1.0f - u) * (1.0f - v) + b * u * (1.0f - v) + c * u * v + d * (1.0f - u) * v;
    }

    private static Vector3f bilinear(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float u, float v) {
        float w0 = (1.0f - u) * (1.0f - v);
        float w1 = u * (1.0f - v);
        float w2 = u * v;
        float w3 = (1.0f - u) * v;

        return new Vector3f(p0.x * w0 + p1.x * w1 + p2.x * w2 + p3.x * w3, p0.y * w0 + p1.y * w1 + p2.y * w2 + p3.y * w3, p0.z * w0 + p1.z * w1 + p2.z * w2 + p3.z * w3);
    }

    private static Vector3f readPosition(int[] vertices, int vertex) {
        int offset = vertex * FaceBakery.VERTEX_INT_SIZE;
        return new Vector3f(Float.intBitsToFloat(vertices[offset]), Float.intBitsToFloat(vertices[offset + 1]), Float.intBitsToFloat(vertices[offset + 2]));
    }

    private static Vector2f readUv(int[] vertices, int vertex) {
        int offset = vertex * FaceBakery.VERTEX_INT_SIZE + FaceBakery.UV_INDEX;
        return new Vector2f(Float.intBitsToFloat(vertices[offset]), Float.intBitsToFloat(vertices[offset + 1]));
    }

    private static Vector2f interpolateUv(Vector2f uv0, Vector2f uv1, Vector2f uv2, float u, float v) {
        float w = 1.0f - u - v;
        return new Vector2f(uv0.x * w + uv1.x * u + uv2.x * v, uv0.y * w + uv1.y * u + uv2.y * v);
    }

    private static boolean isOpaque(TextureAtlasSprite sprite, float atlasU, float atlasV) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float uWidth = u1 - u0;
        float vHeight = v1 - v0;
        if (uWidth <= 0.0f || vHeight <= 0.0f) {
            return false;
        }
        float u = (atlasU - u0) / uWidth;
        float v = (atlasV - v0) / vHeight;
        u = Math.max(0.0f, Math.min(1.0f, u));
        v = Math.max(0.0f, Math.min(1.0f, v));
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        if (width <= 0 || height <= 0) {return false;}
        int x = Math.min(width - 1, Math.max(0, (int) (u * width)));
        int y = Math.min(height - 1, Math.max(0, (int) (v * height)));
        int rgba = ((SpriteContentsAccessor) sprite.contents()).getImage().getPixelRGBA(x, y);
        int alpha = (rgba >>> 24) & 0xFF;
        return alpha >= TriangleHitHelper.RAYCAST_ALPHA_THRESHOLD;
    }
}
