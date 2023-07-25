package smartin.miapi.client.model;

import net.minecraft.client.render.model.BakedQuad;
import org.joml.Vector3f;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MutableQuad {
    public BakedQuad quad;
    public Vector3f[] vertices;
    private Vector3f[] oldVertices;
    public int[] vertexData;
    public static final float accuracy = 1e-6F;
    private static final float TOLERANCE = 1e-6f;

    public MutableQuad(BakedQuad bakedQuad) {
        this.quad = bakedQuad;
        vertexData = bakedQuad.getVertexData();
        vertices = new Vector3f[4];
        oldVertices = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            vertices[i] = new Vector3f(
                    Float.intBitsToFloat(vertexData[i * 8]),
                    Float.intBitsToFloat(vertexData[i * 8 + 1]),
                    Float.intBitsToFloat(vertexData[i * 8 + 2]));
            oldVertices[i] = new Vector3f(vertices[i]);
        }
        List<Vector3f> verteces = Arrays.stream(vertices).toList();
        if (hasDuplicatesOrNearDuplicates(verteces)) {
            Miapi.LOGGER.error("FAILURE");
        }
    }

    public List<MutableQuad> cutListWithQuad(List<MutableQuad> quads) {
        List<MutableQuad> thisQuads = new ArrayList<>();
        thisQuads.add(this);
        List<MutableQuad> cutQuads = new ArrayList<>();
        for (MutableQuad otherQuad : quads) {
            List<MutableQuad> thisQuadsNext = new ArrayList<>();
            for (MutableQuad thisQuad : thisQuads) {
                if (thisQuad.isQuadOverlapping(otherQuad)) {
                    //cutQuads.addAll(otherQuad.cutWithQuad(thisQuad));
                    //thisQuadsNext.addAll((thisQuad.cutWithQuad(otherQuad)));
                } else {
                    cutQuads.add(otherQuad);
                    thisQuadsNext.add(thisQuad);
                }
            }
            thisQuads = thisQuadsNext;
        }
        cutQuads.addAll(thisQuads);
        return cutQuads;
    }

    public List<MutableQuad> cutWithQuad(MutableQuad quad) {
        List<MutableQuad> quads = new ArrayList<>();
        quads.add(this);
        if (!isQuadOverlapping(quad)) {
            Miapi.LOGGER.error("quad is not overlapping");
            return quads;
        }
        for (int i = 0; i < 4; i++) {
            Vector3f vector3f = new Vector3f(quad.vertices[i]);
            Vector3f vector3f2 = new Vector3f(quad.vertices[(i + 1) % 4]);
            List<MutableQuad> replaceList = new ArrayList<>(quads);
            for (MutableQuad mutableQuad : replaceList) {
                if (mutableQuad.isEdgeInsideQuad(vector3f, vector3f2)) {
                    Miapi.LOGGER.error("CUTTING QUAD");
                    quads.remove(mutableQuad);
                    quads.addAll(mutableQuad.cut(vector3f, vector3f2));
                } else {
                    Miapi.LOGGER.error("not cutting quad lol");
                }
            }
        }
        return quads;
    }

    public List<MutableQuad> cut(Vector3f edgeStart, Vector3f edgeEnd) {
        List<MutableQuad> quads = new ArrayList<>();
        //quads.add(this);

        Vector3f planeVector = new Vector3f(edgeEnd).sub(edgeStart);
        Vector3f quadNormal = calculateNormal(vertices[0], vertices[1], vertices[2]);
        Vector3f normal = new Vector3f(planeVector).cross(quadNormal).normalize();
        if (Float.isNaN(normal.x)) {
            return quads;
        }

        Vector3f v1 = new Vector3f(vertices[0]);
        Vector3f v2 = new Vector3f(vertices[1]);
        Vector3f v3 = new Vector3f(vertices[2]);
        Vector3f v4 = new Vector3f(vertices[3]);
        List<Vector3f> toCheck = Arrays.stream(vertices).map(Vector3f::new).toList();
        if (hasDuplicatesOrNearDuplicates(toCheck)) {
            Miapi.LOGGER.error("FAILURE");
        }

        List<Vector3f> frontVertices = new ArrayList<>();

        // List to store vertices on the back side of the plane
        List<Vector3f> backVertices = new ArrayList<>();

        // Calculate the plane's distance to the origin (needed for intersection tests)
        float planeDistance = -edgeStart.dot(normal);

        // Quad vertices as an array for easier processing
        Vector3f[] quadVertices = {v1, v2, v3, v4};

        // Loop over the quad vertices
        for (int i = 0; i < 4; i++) {
            Vector3f currentVertex = quadVertices[i];

            float currentDistance = new Vector3f(currentVertex).dot(normal) + planeDistance;
            if (Math.abs(currentDistance) > accuracy) {
                Vector3f nextVertex = quadVertices[(i + 1) % 4]; // Get the next vertex (wrapping around to the first one)
                float nextDistance = new Vector3f(nextVertex).dot(normal) + planeDistance;
                if (currentDistance > 0) {
                    frontVertices.add(currentVertex);
                } else {
                    backVertices.add(currentVertex);
                }
                if (Math.abs(nextDistance) > accuracy) {
                    if (currentDistance > 0 != nextDistance > 0) {
                        Vector3f intersection = intersectLineWithPlane(currentVertex, nextVertex, edgeStart, new Vector3f(normal), planeDistance);
                        frontVertices.add(intersection);
                        backVertices.add(intersection);
                        if (hasDuplicatesOrNearDuplicates(frontVertices)) {
                            Miapi.LOGGER.error("Front has Duplicates, this dhould not be the case");
                        }
                        if (hasDuplicatesOrNearDuplicates(backVertices)) {
                            Miapi.LOGGER.error("Front has Duplicates, this dhould not be the case");
                        }
                    }
                }
            } else {
                frontVertices.add(currentVertex);
                backVertices.add(currentVertex);
            }
            // Add the vertex to the corresponding list
        }
        if (Float.isNaN(planeDistance)) {
            Miapi.LOGGER.error("STH WENT TERRIBLE WRONG");
        }
        if (hasDuplicatesOrNearDuplicates(frontVertices)) {
            Miapi.LOGGER.error("Front has Duplicates, this dhould not be the case");
        }
        if (hasDuplicatesOrNearDuplicates(backVertices)) {
            Miapi.LOGGER.error("Front has Duplicates, this dhould not be the case");
        }
        //quads.addAll(fromQuads(frontVertices));
        //quads.addAll(fromQuads(backVertices));
        //quads.add(this);

        return quads;
    }

    public static boolean hasDuplicatesOrNearDuplicates(List<Vector3f> vectors) {
        int size = vectors.size();
        for (int i = 0; i < size; i++) {
            Vector3f vector1 = vectors.get(i);
            for (int j = i + 1; j < size; j++) {
                Vector3f vector2 = vectors.get(j);
                if (areNearDuplicates(vector1, vector2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean areNearDuplicates(Vector3f vector1, Vector3f vector2) {
        return vector1.distanceSquared(vector2) < TOLERANCE * TOLERANCE;
    }

    List<MutableQuad> fromQuads(List<Vector3f> vertecies) {
        List<MutableQuad> quads = new ArrayList<>();
        switch (vertecies.size()) {
            case 3: {
                MutableQuad mutableQuad = new MutableQuad(quad);
                mutableQuad.vertices[0] = new Vector3f(vertecies.get(0));
                mutableQuad.vertices[1] = new Vector3f(vertecies.get(1));
                mutableQuad.vertices[2] = new Vector3f(vertecies.get(2));
                Vector3f next = new Vector3f(vertecies.get(2)).add(vertecies.get(0)).mul(0.5f);
                if (new Vector3f(next).sub(vertecies.get(2)).length() < accuracy) {
                    Miapi.LOGGER.error("FAIL");
                }
                mutableQuad.vertices[3] = next;
                quads.add(mutableQuad);
                break;
            }
            case 4: {
                MutableQuad mutableQuad = new MutableQuad(quad);
                mutableQuad.vertices[0] = new Vector3f(vertecies.get(0));
                mutableQuad.vertices[1] = new Vector3f(vertecies.get(1));
                mutableQuad.vertices[2] = new Vector3f(vertecies.get(2));
                mutableQuad.vertices[3] = new Vector3f(vertecies.get(3));
                quads.add(mutableQuad);
                break;
            }
            case 5: {
                MutableQuad mutableQuadA = new MutableQuad(quad);
                mutableQuadA.vertices[0] = new Vector3f(vertecies.get(0));
                mutableQuadA.vertices[1] = new Vector3f(vertecies.get(1));
                mutableQuadA.vertices[2] = new Vector3f(vertecies.get(2));
                mutableQuadA.vertices[3] = new Vector3f(vertecies.get(2)).add(vertecies.get(0)).mul(0.5f);
                quads.add(mutableQuadA);
                MutableQuad mutableQuadB = new MutableQuad(quad);
                mutableQuadB.vertices[0] = new Vector3f(vertecies.get(2));
                mutableQuadB.vertices[1] = new Vector3f(vertecies.get(3));
                mutableQuadB.vertices[2] = new Vector3f(vertecies.get(4));
                mutableQuadB.vertices[3] = new Vector3f(vertecies.get(0));
                quads.add(mutableQuadB);
                break;
            }
            default: {
                //Miapi.LOGGER.error("sth went terrible wrong here " + vertecies.size());
                break;
            }
        }
        return quads;
    }

    // Helper function to calculate intersection point between a line segment and the plane
    Vector3f intersectLineWithPlane(Vector3f p1, Vector3f p2, Vector3f planePoint, Vector3f normal, float planeDistance) {
        Vector3f direction = new Vector3f(p2).sub(p1);
        float denom = normal.dot(direction);

        if (Math.abs(denom) < 1e-6f) {
            return null; // The line and plane are nearly parallel
        }

        float t = -(normal.dot(p1) + planeDistance) / denom;
        if (t >= 0 && t <= 1) {
            // Intersection point lies within the segment
            Vector3f intersection = new Vector3f(p1).add(new Vector3f(direction).mul(t));
            return new Vector3f(intersection).add(new Vector3f(planePoint).negate());
        } else {
            return null; // Intersection point lies outside the segment
        }
    }

    public static Vector3f calculateNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
        // Calculate two vectors representing two sides of the triangle
        Vector3f v1 = new Vector3f(p2).sub(p1);
        Vector3f v2 = new Vector3f(p3).sub(p1);

        // Calculate the cross product of the two vectors
        Vector3f normalVector = new Vector3f(v1).cross(v2);


        return normalVector.normalize();
    }

    public boolean isQuadOverlapping(MutableQuad other) {
        for (int i = 0; i < 4; i++) {
            if (isPointInsideQuad(new Vector3f(other.vertices[i]))) {
                return true;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (isEdgeInsideQuad(new Vector3f(other.vertices[i % 4]), new Vector3f(other.vertices[(i + 1) % 4]))) {
                //return true;
            }
        }
        return false;
    }

    public boolean isPointInsideQuad(Vector3f point) {
        Vector3f v0 = new Vector3f(vertices[0]);
        Vector3f v1 = new Vector3f(vertices[1]);
        Vector3f v2 = new Vector3f(vertices[2]);
        Vector3f v3 = new Vector3f(vertices[3]);

        if (isPointInsideTriangle(point, v0, v1, v2)) {
            return true;
        }
        if (isPointInsideTriangle(point, v1, v2, v3)) {
            return true;
        }

        return false;
    }

    private boolean isPointOnSamePlane(Vector3f point, Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f vBA = new Vector3f(v1).sub(v0);
        Vector3f vCA = new Vector3f(v2).sub(v0);
        Vector3f normal = new Vector3f(vBA).cross(vCA);

        // If the normal is zero (triangle has no area), the point cannot be inside the triangle
        if (normal.lengthSquared() == 0f) {
            return false;
        }

        // Calculate the plane equation (ax + by + cz + d = 0) using one of the triangle's vertices
        // We can use v0 as the reference point
        float a = normal.x;
        float b = normal.y;
        float c = normal.z;
        float d = -normal.dot(v0);

        // Check if the point lies on the plane
        float distanceToPoint = (a * point.x) + (b * point.y) + (c * point.z) + d;

        return Math.abs(distanceToPoint) >= accuracy;
    }

    private boolean isPointInsideTriangle(Vector3f point, Vector3f v0, Vector3f v1, Vector3f v2) {
        // Calculate the normal vector of the triangle

        Vector3f vBA = new Vector3f(v1).sub(v0);
        Vector3f vCA = new Vector3f(v2).sub(v0);
        Vector3f normal = new Vector3f(vBA).cross(vCA);

        // If the normal is zero (triangle has no area), the point cannot be inside the triangle
        if (normal.lengthSquared() == 0f) {
            return false;
        }

        normal.normalize();

        // Calculate the plane equation (ax + by + cz + d = 0) using one of the triangle's vertices
        // We can use v0 as the reference point
        float a = normal.x;
        float b = normal.y;
        float c = normal.z;
        float d = -normal.dot(v0);

        // Check if the point lies on the plane
        float distanceToPoint = (a * point.x) + (b * point.y) + (c * point.z) + d;

        if (Math.abs(distanceToPoint) >= accuracy) {
            return false; // Point is not on the plane
        }

        // Calculate barycentric coordinates of the point with respect to the triangle's vertices
        Vector3f vPA = new Vector3f(point).sub(v0);

        float d00 = vBA.dot(vBA);
        float d01 = vBA.dot(vCA);
        float d11 = vCA.dot(vCA);
        float d20 = vPA.dot(vCA);
        float d21 = vPA.dot(vBA);

        float denom = d00 * d11 - d01 * d01;
        float v = (d11 * d20 - d01 * d21) / denom;
        float w = (d00 * d21 - d01 * d20) / denom;
        float u = 1.0f - v - w;

        if (((u >= -accuracy) && (v >= -accuracy) && (w >= -accuracy)) && !(u >= accuracy) &&(v >= accuracy) &&
        (w >= accuracy)){
            Miapi.LOGGER.error("CHECK");
        }
        // Check if the point is inside the triangle on the plane
        return (u >= -accuracy) && (v >= -accuracy) && (w >= -accuracy);
    }


    public boolean isEdgeInsideQuad(Vector3f edgeStart, Vector3f edgeEnd) {
        Vector3f[] edges = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            Vector3f v1 = new Vector3f(vertices[i]);
            Vector3f v2 = new Vector3f(vertices[(i + 1) % 4]);
            edges[i] = v2.sub(v1);
        }

        // Check intersection with the first triangle formed by vertices 0, 1, 2
        if (isEdgeTriangleIntersect(edgeStart, edgeEnd, new Vector3f(vertices[0]), new Vector3f(vertices[1]), new Vector3f(vertices[2]))) {
            return true;
        }

        // Check intersection with the second triangle formed by vertices 2, 3, 0
        if (isEdgeTriangleIntersect(edgeStart, edgeEnd, new Vector3f(vertices[2]), new Vector3f(vertices[3]), new Vector3f(vertices[0]))) {
            return true;
        }

        return false;
    }

    private boolean isEdgeTriangleIntersect(Vector3f edgeStart, Vector3f edgeEnd, Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f edgeDirection = new Vector3f(edgeEnd).sub(edgeStart);

        Vector3f pvec = new Vector3f(edgeDirection).cross(v2).sub(v0);

        float det = edgeDirection.dot(pvec);

        if (Math.abs(det) < accuracy) {
            return false; // Edge and triangle are parallel
        }

        float invDet = 1.0f / det;

        Vector3f tvec = new Vector3f(edgeStart).sub(v0);
        float u = tvec.dot(pvec) * invDet;

        if (u < 0 || u > 1) {
            return false; // Intersection is outside the triangle
        }

        Vector3f qvec = new Vector3f(tvec).cross(v1).sub(v0);
        float v = edgeDirection.dot(qvec) * invDet;

        if (v < 0 || u + v > 1) {
            return false; // Intersection is outside the triangle
        }

        float t = new Vector3f(v2).sub(v0).dot(qvec) * invDet;
        return t >= 0 && t <= 1; // Intersection lies on the edge
    }

    public Vector3f getIntersection(Vector3f p0, Vector3f p1, Vector3f v0, Vector3f v1) {
        return new Vector3f();
    }


    public BakedQuad write() {
        for (int i = 0; i < 4; i++) {
            vertexData[i * 8] = Float.floatToIntBits(vertices[i].x);
            vertexData[i * 8 + 1] = Float.floatToIntBits(vertices[i].y);
            vertexData[i * 8 + 2] = Float.floatToIntBits(vertices[i].z);
        }
        return new BakedQuad(vertexData, quad.getColorIndex(), quad.getFace(), quad.getSprite(), quad.hasShade());
    }
}
