package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.thread.DynamicModelPhysicsWork;
import smartin.miapi.client.model.module.dynamic.thread.PhysicsScheduler;

/**
 * Dynamic model class with util for animation and models that move their submodules.
 * Has State Control for different RenderContexts
 *
 * @param <S> the State class in question
 */
public abstract class DynamicModel<S extends SimulationState<S>>
        implements MiapiModel {

    private S lastState;
    private S currentState;
    public boolean multiThreadPhysicsUpdate = true;


    private S getState(RenderContext context) {
        if (currentState == null) {
            currentState = createState(MatrixHelper.getCamPos(), context);
        }
        return currentState;
    }

    public static void guiGravity() {
        CURRENT_GRAVITY = new Vector3f(0, 1, 0);
    }

    public static void worldGravity() {
        CURRENT_GRAVITY = new Vector3f(0, -1, 0);
    }

    public static Vector3f CURRENT_GRAVITY = new Vector3f(0, -1, 0);

    @Override
    public void render(RenderContext context) {
        S state = getState(context);
        if (state == null) {
            return;
        }

        double now = MiapiClient.currentTickFull();

        double elapsedTicks = now - state.lastTime;

        state.lastTime = now;

        float deltaSeconds = (float) (elapsedTicks / 20.0);

        Minecraft.getInstance().getProfiler().push("Physics");
        if (deltaSeconds > 0) {
            queuePhysicsUpdate(state, MatrixHelper.getCamPos(), new Vector3f(CURRENT_GRAVITY), context, deltaSeconds);
        }
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().push("Rendering");
        Vector3f start = new Vector3f();
        Matrix4f end = computeEnd(state, context.matrices());

        renderDynamic(context, start, end, state);

        Minecraft.getInstance().getProfiler().pop();
    }

    /**
     * Creating the State for the current Context
     *
     * @param context
     * @return
     */
    protected abstract S createState(Vec3 pos, RenderContext context);

    protected void queuePhysicsUpdate(S state,
                                      Vec3 camPos,
                                      Vector3f down,
                                      RenderContext context, double deltaSecond) {
        state.pose = new PoseStack();
        state.pose.last().pose().set(context.matrices().last().pose());
        state.cameraPose = camPos;
        //updatePhysics(state, context.entity(), (float) deltaSecond, context.matrices(), down, context.transformationMode());
        if(multiThreadPhysicsUpdate){
            PhysicsScheduler.submit(
                    new DynamicModelPhysicsWork<>(
                            this,
                            state,
                            context.transformationMode(),
                            context.entity(),
                            down,
                            deltaSecond
                    )
            );
        }else{
            updatePhysics(state, context.entity(), (float) deltaSecond, context.matrices(), down, context.transformationMode());
        }
    }

    /**
     * Physics update.
     * TODO: add option to run this on worker threads without bugs.
     */
    public abstract void updatePhysics(
            S state,
            @Nullable LivingEntity entity,
            double delta,
            PoseStack pose,
            Vector3f down,
            ItemDisplayContext context
    );

    /**
     * THe end position for the next module for the current state
     *
     * @return
     */
    protected abstract Matrix4f computeEnd(S state, PoseStack poseStack);

    /**
     * The actual render call.
     * seperated from physics update to allow for async physics updates
     */
    protected abstract void renderDynamic(
            RenderContext context,
            Vector3f start,
            Matrix4f end,
            S state);

    @Override
    public boolean hasAnimatedModuleMatrix() {
        return true;
    }

    @Override
    public Matrix4f subModuleMatrix(RenderContext context) {
        S state = getState(context);
        return computeEnd(state, context.matrices());
    }
}

