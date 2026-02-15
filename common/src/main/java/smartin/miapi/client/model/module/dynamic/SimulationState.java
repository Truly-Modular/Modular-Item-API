package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

public abstract class SimulationState {
    public double lastTime;
    public PoseStack pose;
    public Vec3 cameraPose;
}
