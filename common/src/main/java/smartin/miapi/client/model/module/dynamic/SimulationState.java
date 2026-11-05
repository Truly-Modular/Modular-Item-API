package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

public abstract class SimulationState<S extends SimulationState<S>> {
    public double lastTime;
    public PoseStack pose;
    public Vec3 cameraPose;

    public abstract S copy();

    public abstract S interpolate(float delta, S other);
}
