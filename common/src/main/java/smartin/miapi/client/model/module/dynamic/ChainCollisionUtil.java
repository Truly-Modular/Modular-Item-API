package smartin.miapi.client.model.module.dynamic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import smartin.miapi.client.model.module.dynamic.thread.WorldPhysicsAccess;

public class ChainCollisionUtil {

    /**
     * Resolves collision of a single node with the world.
     */
    public static boolean collideNodeWithWorld(
            WorldPhysicsAccess level,
            Vector3f pos,
            float radius
    ) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount < 5) {
            return false;
        }
        boolean collide = false;
        AABB nodeBox = new AABB(
                pos.x - radius,
                pos.y - radius,
                pos.z - radius,
                pos.x + radius,
                pos.y + radius,
                pos.z + radius
        );

        int minX = (int) Math.floor(nodeBox.minX);
        int minY = (int) Math.floor(nodeBox.minY);
        int minZ = (int) Math.floor(nodeBox.minZ);
        int maxX = (int) Math.floor(nodeBox.maxX);
        int maxY = (int) Math.floor(nodeBox.maxY);
        int maxZ = (int) Math.floor(nodeBox.maxZ);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    cursor.set(x, y, z);
                    VoxelShape shape = level.getVoxelShape(cursor);
                    if (shape == null || shape.isEmpty()) continue;

                    for (AABB blockBox : shape.toAabbs()) {
                        blockBox = blockBox.move(cursor.getX(), cursor.getY(), cursor.getZ());
                        if (blockBox.intersects(nodeBox)) {
                            resolveAabbPenetration(nodeBox, blockBox, pos);
                            collide = true;
                            nodeBox = new AABB(
                                    pos.x - radius,
                                    pos.y - radius,
                                    pos.z - radius,
                                    pos.x + radius,
                                    pos.y + radius,
                                    pos.z + radius
                            );
                        }
                    }
                }
            }
        }
        return collide;
    }

    public static boolean collideNodeWithEntities(
            WorldPhysicsAccess level,
            Entity excluded,
            Vector3f pos,
            float radius
    ) {
        if (level == null ||
            Minecraft.getInstance().player != null &&
            Minecraft.getInstance().player.tickCount < 5) {
            return false;
        }

        AABB nodeBox = new AABB(
                pos.x - radius,
                pos.y - radius,
                pos.z - radius,
                pos.x + radius,
                pos.y + radius,
                pos.z + radius
        );

        boolean collided = false;

        /*
        for (Entity entity : level.getEntities(
                excluded,
                nodeBox,
                e -> e.isAlive() && e.isPushable() && !e.isSpectator()
        )) {
            AABB entityBox = entity.getBoundingBox();

            if (!entityBox.intersects(nodeBox)) {
                continue;
            }

            resolveAabbPenetration(nodeBox, entityBox, pos);

            collided = true;

            // Update the node box after resolving penetration.
            nodeBox = new AABB(
                    pos.x - radius,
                    pos.y - radius,
                    pos.z - radius,
                    pos.x + radius,
                    pos.y + radius,
                    pos.z + radius
            );
        }
         */

        return collided;
    }

    /**
     * Push node minimally outside block AABB along shortest penetration axis.
     */
    private static void resolveAabbPenetration(
            AABB node,
            AABB block,
            Vector3f pos
    ) {
        double dx1 = block.maxX - node.minX;
        double dx2 = node.maxX - block.minX;
        double dy1 = block.maxY - node.minY;
        double dy2 = node.maxY - block.minY;
        double dz1 = block.maxZ - node.minZ;
        double dz2 = node.maxZ - block.minZ;

        double px = Math.min(dx1, dx2);
        double py = Math.min(dy1, dy2);
        double pz = Math.min(dz1, dz2);
        if (px < py && px < pz) {
            pos.x -= (dx1 < dx2 ? -px : px); // push inwards along shortest axis
        } else if (py < pz) {
            pos.y -= (dy1 < dy2 ? -py : py);
        } else {
            pos.z -= (dz1 < dz2 ? -pz : pz);
        }
    }
}
