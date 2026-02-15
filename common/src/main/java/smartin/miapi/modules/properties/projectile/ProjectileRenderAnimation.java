package smartin.miapi.modules.properties.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.ItemProjectileRenderer;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.MiapiRegistry;

@Environment(EnvType.CLIENT)
public class ProjectileRenderAnimation extends CodecProperty<ProjectileRenderAnimation.RenderAnimation> {
    public static final ResourceLocation KEY = Miapi.id("projectile_animation");
    public static ProjectileRenderAnimation property;
    public static final MiapiRegistry<RenderAnimation> REGISTRY = MiapiRegistry.getInstance(RenderAnimation.class);
    public static final Codec<RenderAnimation> CODEC = REGISTRY.dispatchCodec(RenderAnimation::getCodec);

    public ProjectileRenderAnimation() {
        super(CODEC);
        property = this;
        MiapiProjectileEvents.ClientEvents.MODULAR_PROJECTILE_RENDER_EVENT.register(new MiapiProjectileEvents.ModularProjectileRenderEvent() {
            @Override
            public EventResult hit(ItemProjectileRenderer renderer, ItemStack stack, ItemProjectileEntity entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
                getData(stack).ifPresent(renderAnimation -> {
                    renderAnimation.runAnim(renderer, stack, entity, yaw, tickDelta, matrixStack, vertexConsumers, light);
                });
                return EventResult.pass();
            }
        });
        REGISTRY.register(Miapi.id("simple_rotation"), new SimpleSpinAnimation(SimpleSpinAnimation.Axis.VELOCITY, 0, 0));
    }


    @Override
    public RenderAnimation merge(RenderAnimation left, RenderAnimation right, MergeType mergeType) {
        return left.merge(left, right, mergeType);
    }

    public interface RenderAnimation extends MergeAble<RenderAnimation> {
        void runAnim(
                ItemProjectileRenderer renderer, ItemStack stack,
                ItemProjectileEntity entity, float yaw, float tickDelta,
                PoseStack matrixStack, MultiBufferSource vertexConsumers, int light);

        MapCodec<? extends RenderAnimation> getCodec();
    }

    public static class SimpleSpinAnimation implements ProjectileRenderAnimation.RenderAnimation {

        public static final MapCodec<SimpleSpinAnimation> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Axis.CODEC.fieldOf("axis").forGetter(a -> a.axis),
                        Codec.FLOAT.optionalFieldOf("degrees_per_tick", 20f).forGetter(a -> a.degreesPerTick),
                        Codec.FLOAT.optionalFieldOf("offset", 0f).forGetter(a -> a.offset)
                ).apply(instance, SimpleSpinAnimation::new)
        );

        private final Axis axis;
        private final float degreesPerTick;
        private final float offset;

        public SimpleSpinAnimation(Axis axis, float degreesPerTick, float offset) {
            this.axis = axis;
            this.degreesPerTick = degreesPerTick;
            this.offset = offset;
        }

        @Override
        public void runAnim(
                ItemProjectileRenderer renderer,
                ItemStack stack,
                ItemProjectileEntity entity,
                float yaw,
                float tickDelta,
                PoseStack poseStack,
                MultiBufferSource buffers,
                int light
        ) {
            float age = entity.tickCount + tickDelta;
            float rotation = age * degreesPerTick + offset;
            if (entity.getDeltaMovement().length() < 0.005 ||
                entity.onGround() ||
                entity.inGroundTick() > 0
            ) {
                return;
            }

            Vec3 axisVec = axis.getVector(entity);

            poseStack.mulPose(
                    new Quaternionf().rotateAxis(
                            (float) Math.toRadians(rotation),
                            (float) axisVec.x,
                            (float) axisVec.y,
                            (float) axisVec.z
                    )
            );
        }

        @Override
        public RenderAnimation merge(RenderAnimation left, RenderAnimation right, MergeType mergeType) {
            // Simple rule: right overrides left
            return mergeType == MergeType.OVERWRITE ? right : left;
        }

        @Override
        public MapCodec<? extends RenderAnimation> getCodec() {
            return CODEC;
        }

        /*
         * Axis enum — semantic, data-friendly
         */
        public enum Axis {
            X, Y, Z,
            VELOCITY;

            public static final Codec<Axis> CODEC = Codec.STRING.xmap(
                    Axis::valueOf,
                    Axis::name
            );

            public Vec3 getVector(ItemProjectileEntity entity) {
                return switch (this) {
                    case X -> new Vec3(1, 0, 0);
                    case Y -> new Vec3(0, 1, 0);
                    case Z -> new Vec3(0, 0, 1);
                    case VELOCITY -> {
                        Vec3 v = entity.getDeltaMovement();
                        if (v.lengthSqr() < 1e-6) yield new Vec3(0, 1, 0);
                        yield v.normalize();
                    }
                };
            }
        }
    }

}