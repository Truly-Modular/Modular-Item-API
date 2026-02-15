package smartin.miapi.client.model.module.dynamic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class ChainCollisionUtil {

    /**
     * Resolves collision of a single node with the world.
     */
    public static boolean collideNodeWithWorld(
            Level level,
            Vector3f pos,
            float radius
    ) {
        if (!level.isClientSide() ||
            Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount < 5) {
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
                    BlockState state = level.getBlockState(cursor);

                    if (state.isAir()) continue;

                    VoxelShape shape = state.getCollisionShape(level, cursor);
                    if (shape.isEmpty()) continue;

                    // Minecraft shapes can have multiple AABBs
                    for (AABB blockBox : shape.toAabbs()) {
                        blockBox = blockBox.move(cursor.getX(), cursor.getY(), cursor.getZ());
                        if (blockBox.intersects(nodeBox)) {
                            resolveAabbPenetration(nodeBox, blockBox, pos);
                            collide = true;
                            // rebuild node AABB after move
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
