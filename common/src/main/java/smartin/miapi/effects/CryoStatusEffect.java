package smartin.miapi.effects;

import com.redpxnda.nucleus.event.ClientEvents;
import com.redpxnda.nucleus.event.MiscEvents;
import com.redpxnda.nucleus.registry.effect.RenderingMobEffect;
import com.redpxnda.nucleus.client.Rendering;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.registries.RegistryInventory;

public class CryoStatusEffect extends RenderingMobEffect {
    protected static final Identifier ICE_LOCATION = new Identifier("block/ice");

    public CryoStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 1160409);

        super.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "309da7c1-944e-4d5e-aad1-be2491a44695", -0.4, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

        MiscEvents.LIVING_JUMP_POWER.register(player -> {
            if (player.hasStatusEffect(this)) return CompoundEventResult.interruptFalse(0.2f);
            return CompoundEventResult.pass();
        });
        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            if (event.livingEntity.hasStatusEffect(this)) {
                StatusEffectInstance instance = event.livingEntity.getStatusEffect(this);
                if (instance != null) {
                    event.livingEntity.removeStatusEffect(this);
                    event.livingEntity.addStatusEffect(
                            new StatusEffectInstance(
                                    this, instance.getDuration()-20, instance.getAmplifier(),
                                    instance.isAmbient(), instance.shouldShowParticles(), instance.shouldShowIcon()));
                }
            }
            return EventResult.pass();
        });
    }

    @Environment(EnvType.CLIENT)
    public static void setupOnClient() {
        MiscEvents.CAN_CLIENT_SPRINT.register(player -> {
            if (player.hasStatusEffect(RegistryInventory.cryoStatusEffect)) return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientEvents.MODIFY_CAMERA_MOTION.register((mc, motion) -> {
            if (mc.player != null && mc.player.hasStatusEffect(RegistryInventory.cryoStatusEffect) && (motion.x != 0 || motion.y != 0)) {
                int amplifier =  mc.player.getStatusEffect(RegistryInventory.cryoStatusEffect).getAmplifier();
                motion.normalize();
                motion.div(Math.max(amplifier/2f, 2.25));
            }
        });
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.getWorld() instanceof ServerWorld world) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 1, 1);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        if (entity.getWorld() instanceof ServerWorld world) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 1);
            world.spawnParticles(
                            new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.ICE.getDefaultState()),
                            entity.getX(), entity.getY()+1, entity.getZ(), 50,
                            0.5, 0.5, 0.5, 1.5);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void renderPost(StatusEffectInstance instance, LivingEntity entity, float entityYaw, float partialTick, MatrixStack matrixStack, VertexConsumerProvider multiBufferSource, int packedLight) {
        if (entity.equals(MinecraftClient.getInstance().player) && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) return;
        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(ICE_LOCATION);
        VertexConsumer vc = multiBufferSource.getBuffer(RegistryInventory.Client.TRANSLUCENT_NO_CULL);
        matrixStack.push();

        Box bb = entity.getBoundingBox().offset(entity.getPos().negate());
        double minX = Math.floor(bb.minX*2)/2d;
        double maxX = Math.ceil(bb.maxX*2)/2d;
        double minZ = Math.floor(bb.minZ*2)/2d;
        double maxZ = Math.ceil(bb.maxZ*2)/2d;
        matrixStack.translate(0.5, 0.5, 0.5);

        for (double z = minZ; z < maxZ; z++) {
            boolean isLeft = z == minZ || maxZ-minZ <= 1;
            boolean isRight = z+1 >= maxZ || maxZ-minZ <= 1;

            for (double x = minX; x < maxX; x++) {
                boolean isFront = x == minX || maxX-minX <= 1;
                boolean isBack = x+1 >= maxX || maxX-minX <= 1;

                for (double y = bb.minY; y < bb.maxY; y++) {
                    boolean isBottom = y == bb.minY || bb.maxY-bb.minY <= 1;
                    boolean isTop = y+1 >= bb.maxY || bb.maxY-bb.minY <= 1;

                    matrixStack.push();
                    matrixStack.translate(x, y+.0001, z);

                    for (int side = 0; side < 6; side++) {
                        if (
                            (side == 0 && !isTop) ||
                            (side == 1 && !isBottom) ||
                            (side == 2 && !isRight) ||
                            (side == 3 && !isLeft) ||
                            (side == 4 && !isBack) ||
                            (side == 5 && !isFront)
                        ) continue;
                        Rendering.addQuad(
                                Rendering.CUBE[side], matrixStack, vc,
                                1f, 1f, 1f, 0.5f,
                                -0.5f, 0.5f, 0.5f,
                                sprite.getMinU(), sprite.getMaxU(),
                                sprite.getMinV(), sprite.getMaxV(),
                                packedLight
                        );
                    }
                    matrixStack.pop();
                }
            }
        }
        matrixStack.pop();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean renderHud(StatusEffectInstance instance, MinecraftClient minecraft, DrawContext graphics, float partialTick) {
        if (minecraft.options == null || !minecraft.options.getPerspective().isFirstPerson()) return false;
        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(ICE_LOCATION);
        Window window = MinecraftClient.getInstance().getWindow();
        graphics.drawSprite(
                0, 0, 0,
                window.getWidth()/2, window.getHeight()/2,
                sprite, 1f, 1f, 1f, 0.6f
        );
        return false;
    }

    @Override
    public int tickUpdateInterval() {
        return 20;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration > 0;
    }
}
