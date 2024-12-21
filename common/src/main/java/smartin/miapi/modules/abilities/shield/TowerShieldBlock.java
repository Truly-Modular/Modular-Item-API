package smartin.miapi.modules.abilities.shield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.items.shield.TowerShieldComponent;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

public class TowerShieldBlock implements ItemUseDefaultCooldownAbility<TowerShieldBlock.BlockData> {

    public TowerShieldBlock() {
        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            ItemStack itemStack = event.defender.getUseItem();
            BlockData data = getSpecialContext(itemStack);
            if (data != null) {
                itemStack.update(TowerShieldComponent.TOWER_SHIELD_COMPONENT, new TowerShieldComponent(event.defender.level().getGameTime()), (towerShieldComponent -> {
                    towerShieldComponent.blockCount++;
                    if (towerShieldComponent.blockCount > data.block.getValue()) {
                        if (event.defender instanceof Player player) {
                            double cooldown = data.cooldown.getValue();
                            if (event.damageSource.getDirectEntity() instanceof LivingEntity livingEntity) {
                                cooldown += livingEntity.getAttributeValue(AttributeRegistry.SHIELD_BREAK) * 20 - 100;
                            }
                            player.getCooldowns().addCooldown(itemStack.getItem(), Math.max(20, (int) cooldown));
                            towerShieldComponent.blockCount = 0;
                        }
                    }
                    return towerShieldComponent;
                }));
            }
            return EventResult.pass();
        });
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        BlockData data = getSpecialContext(itemStack);
        var component = itemStack.get(TowerShieldComponent.TOWER_SHIELD_COMPONENT);
        if (component != null) {
            component.update(world.getGameTime(), (int) data.cooldown.getValue());
            itemStack.set(TowerShieldComponent.TOWER_SHIELD_COMPONENT, component);
        }
        setAnimation(user, hand, data.animation);
        return InteractionResultHolder.pass(user.getItemInHand(hand));
    }

    @Override
    public BlockData getDefaultContext() {
        return null;
    }

    @Override
    public <K> BlockData decode(DynamicOps<K> ops, K prefix) {
        Codec<BlockData> codec = AutoCodec.of(BlockData.class).codec();
        return codec.decode(ops, prefix).getOrThrow().getFirst();
    }

    @Override
    public BlockData initialize(BlockData data, ModuleInstance moduleInstance) {
        BlockData init = new BlockData();
        init.cooldown = data.cooldown.initialize(moduleInstance);
        init.block = data.block.initialize(moduleInstance);
        init.animation = data.animation;
        return init;
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown.getValue();
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        return stack;
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
    }

    public void setAnimation(Player p, InteractionHand hand, ResourceLocation id) {
        if (p instanceof ServerPlayer player && id != null) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set(id.toString(), player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.reset(player);
            }
        }
    }

    public static class BlockData {
        @CodecBehavior.Optional
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(100);
        @CodecBehavior.Optional
        public DoubleOperationResolvable block = new DoubleOperationResolvable(1);
        @CodecBehavior.Optional
        public ResourceLocation animation = ResourceLocation.parse("miapi:block");
    }
}