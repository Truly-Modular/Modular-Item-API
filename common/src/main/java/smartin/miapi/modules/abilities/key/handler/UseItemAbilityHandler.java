package smartin.miapi.modules.abilities.key.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.client.MinecraftAccessor;
import smartin.miapi.modules.abilities.key.KeyBindAbilityManagerProperty;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.abilities.key.MiapiBinding;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.network.modern.ModernNetworking;

import java.util.ArrayList;
import java.util.List;

public final class UseItemAbilityHandler implements KeybindHandler {
    public static boolean isUsing = false;
    public static final KeybindHandlerType<UseItemAbilityHandler> TYPE =
            KeybindHandlerTypes.register(new KeybindHandlerType<>() {

                @Override
                public ResourceLocation id() {
                    return Miapi.id("use_item_ability");
                }

                @Override
                public MapCodec<UseItemAbilityHandler> codec() {
                    return CODEC;
                }
            });
    public static final ModernNetworking.ClientToServerManager<ResourceLocation> UPDATE_BIND = new ModernNetworking.ClientToServerManager<>(
            Miapi.id("update_abiltiy_bind"),
            ResourceLocation.CODEC,
            (id, player, access) -> {
                boolean isRelease = id.equals(KeyBindManager.NONE);
                if (isRelease) {
                    ItemAbilityManager.serverKeyBindID.remove(player);
                } else {
                    ItemAbilityManager.serverKeyBindID.put(player, id);
                }
            }
    );

    public static final Codec<InteractionHand> HAND_CODEC =
            Codec.STRING.xmap(
                    s -> InteractionHand.valueOf(s.toUpperCase()),
                    InteractionHand::name
            );

    public static final MapCodec<UseItemAbilityHandler> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            HAND_CODEC.listOf()
                                    .optionalFieldOf("hands",
                                            List.of(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND))
                                    .forGetter(h -> h.hands),
                            Codec.BOOL.optionalFieldOf("item_interaction", true)
                                    .forGetter(h -> h.itemInteraction),
                            Codec.BOOL.optionalFieldOf("block_interaction", true)
                                    .forGetter(h -> h.blockInteraction),
                            Codec.BOOL.optionalFieldOf("entity_interaction", true)
                                    .forGetter(h -> h.entityInteraction)
                    ).apply(instance, UseItemAbilityHandler::new)
            );

    public final List<InteractionHand> hands;
    public final boolean itemInteraction;
    public final boolean blockInteraction;
    public final boolean entityInteraction;

    public UseItemAbilityHandler(
            List<InteractionHand> hands,
            boolean itemInteraction,
            boolean blockInteraction,
            boolean entityInteraction
    ) {
        this.hands = new ArrayList<>(hands);
        this.itemInteraction = itemInteraction;
        this.blockInteraction = blockInteraction;
        this.entityInteraction = entityInteraction;
    }

    @Override
    public KeybindHandlerType<?> type() {
        return TYPE;
    }

    @Override
    public void onPress(Minecraft mc, LocalPlayer player, MiapiBinding binding) {
        isUsing = tryUse(mc, player, binding.id, this.hands, this.entityInteraction, this.blockInteraction, this.itemInteraction);
    }

    @Override
    public void whileHeld(Minecraft mc, LocalPlayer player, MiapiBinding binding) {
        if (((MinecraftAccessor) mc).getMiapiRightClickDelay() == 0 && !player.isUsingItem()) {
            isUsing = tryUse(mc, player, binding.id, this.hands, this.entityInteraction, this.blockInteraction, this.itemInteraction);
        } else {
            isUsing = false;
        }
    }

    public void onRelease(
            Minecraft minecraft,
            LocalPlayer player,
            MiapiBinding binding
    ) {
        UPDATE_BIND.sendServer(KeyBindManager.NONE, player.registryAccess());
    }

    public static boolean tryUse(Minecraft mc, LocalPlayer player, @Nullable ResourceLocation abilityId, List<InteractionHand> hands, boolean entityInteraction, boolean blockInteraction, boolean itemInteraction) {
        if (mc.gameMode.isDestroying()) return false;
        if (player.isHandsBusy()) return false;

        ((MinecraftAccessor) mc).setMiapiRightClickDelay(4);

        for (InteractionHand hand : hands) {
            ItemStack stack = player.getItemInHand(hand);

            if (stack.isEmpty()) continue;

            if (KeyBindAbilityManagerProperty.property.getData(stack).isEmpty() && abilityId != null) {
                //full prevent execution if there is no ability. reduces networking
                return false;
            }
            if (abilityId != null) {
                ItemAbilityManager.clientKeyBindID.put(player, abilityId);
                UPDATE_BIND.sendServer(abilityId, player.registryAccess());
            }

            if (mc.hitResult != null) {
                switch (mc.hitResult.getType()) {
                    case ENTITY -> {
                        if (!entityInteraction) break;

                        EntityHitResult hit = (EntityHitResult) mc.hitResult;
                        Entity entity = hit.getEntity();

                        InteractionResult result =
                                mc.gameMode.interactAt(player, entity, hit, hand);

                        if (!result.consumesAction()) {
                            result = mc.gameMode.interact(player, entity, hand);
                        }

                        if (result.consumesAction()) {
                            if (result.shouldSwing()) {
                                player.swing(hand);
                            }
                            return true;
                        }
                    }
                    case BLOCK -> {
                        if (!blockInteraction) break;

                        BlockHitResult hit = (BlockHitResult) mc.hitResult;

                        InteractionResult result =
                                mc.gameMode.useItemOn(player, hand, hit);

                        if (result.consumesAction()) {
                            if (result.shouldSwing()) {
                                player.swing(hand);
                            }
                            mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
                            return true;
                        }
                    }
                }
            }

            if (itemInteraction) {
                InteractionResult result =
                        mc.gameMode.useItem(player, hand);

                if (result.consumesAction()) {
                    if (result.shouldSwing()) {
                        player.swing(hand);
                    }
                    mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
                    return true;
                }
            }
        }
        return false;
    }
}