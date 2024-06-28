package smartin.miapi.effects;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.nucleus.event.ClientEvents;
import com.redpxnda.nucleus.event.MiscEvents;
import com.redpxnda.nucleus.registry.effect.RenderingMobEffect;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.registries.RegistryInventory;

public class CryoStatusEffect extends RenderingMobEffect {
    protected static final ResourceLocation ICE_LOCATION = ResourceLocation.parse("block/ice");

    public CryoStatusEffect() {
        super(MobEffectCategory.HARMFUL, 1160409);

        super.addAttributeModifier(Attributes.MOVEMENT_SPEED, Miapi.id("cryo_temp_movementspeed_slow"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        MiscEvents.LIVING_JUMP_POWER.register(player -> {
            MobEffectInstance instance = player.getEffect(this);
            if (instance != null) return CompoundEventResult.interruptFalse(0.4f - Math.min(0.4f, (instance.getAmplifier()+1)*0.04f));
            return CompoundEventResult.pass();
        });
        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            if (event.livingEntity.hasEffect(this)) {
                MobEffectInstance instance = event.livingEntity.getEffect(this);
                if (instance != null) {
                    event.livingEntity.removeEffect(this);
                    event.livingEntity.addEffect(
                            new MobEffectInstance(
                                    this, instance.getDuration()-30, instance.getAmplifier(),
                                    instance.isAmbient(), instance.isVisible(), instance.showIcon()));
                }
            }
            return EventResult.pass();
        });
    }

    @Environment(EnvType.CLIENT)
    public static void setupOnClient() {
        MiscEvents.CAN_CLIENT_SPRINT.register(player -> {
            if (player.hasEffect(RegistryInventory.cryoStatusEffect)) return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientEvents.MODIFY_CAMERA_MOTION.register((mc, motion) -> {
            if (mc.player != null && mc.player.hasEffect(RegistryInventory.cryoStatusEffect) && (motion.x != 0 || motion.y != 0)) {
                int amplifier =  mc.player.getEffect(RegistryInventory.cryoStatusEffect).getAmplifier();
                motion.normalize();
                motion.div(Math.min((amplifier+6)/5f, 2.25));
            }
        });
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        if (entity.level() instanceof ServerLevel world) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (entity.level() instanceof ServerLevel world) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 1);
            world.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                            entity.getX(), entity.getY()+1, entity.getZ(), 50,
                            0.5, 0.5, 0.5, 1.5);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void renderPost(MobEffectInstance instance, LivingEntity entity, float entityYaw, float partialTick, PoseStack matrixStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (entity.equals(Minecraft.getInstance().player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ICE_LOCATION);
        VertexConsumer vc = multiBufferSource.getBuffer(RegistryInventory.Client.TRANSLUCENT_NO_CULL);
        matrixStack.pushPose();

        AABB bb = entity.getBoundingBox().move(entity.position().reverse());
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

                    matrixStack.pushPose();
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
                                sprite.getU0(), sprite.getU1(),
                                sprite.getV0(), sprite.getV1(),
                                packedLight
                        );
                    }
                    matrixStack.popPose();
                }
            }
        }
        matrixStack.popPose();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean renderHud(MobEffectInstance instance, Minecraft minecraft, GuiGraphics graphics, float partialTick) {
        if (minecraft.options == null || !minecraft.options.getCameraType().isFirstPerson()) return false;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ICE_LOCATION);
        Window window = Minecraft.getInstance().getWindow();
        graphics.blit(
                0, 0, 0,
                window.getScreenWidth()/2, window.getScreenHeight()/2,
                sprite, 1f, 1f, 1f, 0.6f
        );
        return false;
    }

    @Override
    public int tickUpdateInterval() {
        return 20;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }
}
