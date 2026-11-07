package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.client.MiapiClient;

public abstract class SimulationState<S extends SimulationState<S>> {
    public double lastTime = MiapiClient.currentTickFull();
    public PoseStack pose = new PoseStack();
    public Vec3 cameraPose = MatrixHelper.getCamPos();

    public abstract S copy();

    public abstract S interpolate(float delta, S other);
}
