package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

public class CastLightingAbility implements ItemUseDefaultCooldownAbility<CastLightingAbility.CastLightingContext>, ItemUseMinHoldAbility<CastLightingAbility.CastLightingContext> {
    public static String KEY = "cast_lighting";

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown().getValue();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        SonicBoom sonicBoom;
        return (int) getSpecialContext(itemStack).minHold().getValue();
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return Math.pow(getSpecialContext(itemStack).maxRange().getValue(), 2) >= abilityHitContext.hitResult().getClickedPos().distToCenterSqr(player.getX(), player.getY(), player.getZ());
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return 7200;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return null;
    }

    public InteractionResult useOnBlock(UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        CastLightingContext castLightingContext = getSpecialContext(itemStack);
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (Math.pow(castLightingContext.maxRange().getValue(), 2) >=
                context.getClickedPos().distToCenterSqr(player.getX(), player.getY(), player.getZ())) {
                context.getClickedPos();
                for (int i = 0; i < castLightingContext.lighting().getValue(); i++) {
                    LightningBolt lightningEntity = EntityType.LIGHTNING_BOLT.create(context.getLevel());
                    assert lightningEntity != null;
                    lightningEntity.moveTo(Vec3.atBottomCenterOf(context.getClickedPos()));
                    lightningEntity.setCause(serverPlayer);
                    context.getLevel().addFreshEntity(lightningEntity);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public <K> CastLightingContext decode(DynamicOps<K> ops, K prefix) {
        return CastLightingContext.CODEC.decode(ops, prefix).getOrThrow().getFirst();
    }

    @Override
    public CastLightingContext getDefaultContext() {
        return new CastLightingContext(
                SoundEvents.EMPTY,
                new DoubleOperationResolvable(10),
                new DoubleOperationResolvable(1),
                new DoubleOperationResolvable(40),
                new DoubleOperationResolvable(6));
    }

    @Override
    public CastLightingContext initialize(CastLightingContext json, ModuleInstance moduleInstance) {
        return json.initialize(moduleInstance);
    }

    @Override
    public CastLightingContext merge(CastLightingContext left, CastLightingContext right, MergeType mergeType) {
        return CastLightingContext.merge(left, right, mergeType);
    }

    public record CastLightingContext(SoundEvent onThrow, DoubleOperationResolvable minHold,
                                      DoubleOperationResolvable lighting, DoubleOperationResolvable cooldown,
                                      DoubleOperationResolvable maxRange) {
        public static final Codec<CastLightingContext> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        SoundEvent.DIRECT_CODEC.optionalFieldOf("on_throw", SoundEvents.EMPTY)
                                .forGetter(CastLightingContext::onThrow),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("min_hold", new DoubleOperationResolvable(10))
                                .forGetter(CastLightingContext::minHold),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("lighting", new DoubleOperationResolvable(1))
                                .forGetter(CastLightingContext::lighting),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("cooldown", new DoubleOperationResolvable(50))
                                .forGetter(CastLightingContext::maxRange),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("max_range", new DoubleOperationResolvable(5))
                                .forGetter(CastLightingContext::maxRange)
                ).apply(instance, CastLightingContext::new)
        );

        public CastLightingContext initialize(ModuleInstance moduleInstance) {
            SoundEvent initializedOnThrow = onThrow; // Assuming no further initialization is needed for SoundEvent
            DoubleOperationResolvable initializedMinHold = minHold.initialize(moduleInstance);
            DoubleOperationResolvable initializedLighting = lighting.initialize(moduleInstance);
            DoubleOperationResolvable initializedCooldown = cooldown.initialize(moduleInstance);
            DoubleOperationResolvable initializedMaxRange = maxRange.initialize(moduleInstance);

            return new CastLightingContext(initializedOnThrow, initializedMinHold, initializedLighting, initializedCooldown, initializedMaxRange);
        }

        public static CastLightingContext merge(CastLightingContext left, CastLightingContext right, MergeType mergeType) {
            SoundEvent mergedOnThrow;
            if (MergeType.EXTEND.equals(mergeType) && left.onThrow != null) {
                mergedOnThrow = left.onThrow;
            } else {
                mergedOnThrow = right.onThrow;
            }

            DoubleOperationResolvable mergedMinHold = left.minHold.merge(right.minHold, mergeType);
            DoubleOperationResolvable mergedLighting = left.lighting.merge(right.lighting, mergeType);
            DoubleOperationResolvable mergedCooldown = left.cooldown.merge(right.cooldown, mergeType);
            DoubleOperationResolvable mergedMaxRange = left.maxRange.merge(right.maxRange, mergeType);

            return new CastLightingContext(mergedOnThrow, mergedMinHold, mergedLighting, mergedCooldown, mergedMaxRange);
        }


    }
}
