package smartin.miapi.modules.abilities.key.handler.backpack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.abilities.key.MiapiBinding;
import smartin.miapi.modules.abilities.key.handler.KeybindHandler;
import smartin.miapi.modules.abilities.key.handler.KeybindHandlerType;
import smartin.miapi.modules.abilities.key.handler.KeybindHandlerTypes;
import smartin.miapi.network.modern.ModernNetworking;

/**
 * Common class and networking for usage from backpack logic.
 * the logic to use from backpack needs clear separation of server and client logic to prevent bugs.
 */
public final class UseFromBackpackHandler implements KeybindHandler {
    public static final MapCodec<UseFromBackpackHandler> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.BOOL.optionalFieldOf("item_interaction", true)
                                    .forGetter(h -> h.itemInteraction),

                            Codec.BOOL.optionalFieldOf("block_interaction", true)
                                    .forGetter(h -> h.blockInteraction),

                            Codec.BOOL.optionalFieldOf("entity_interaction", true)
                                    .forGetter(h -> h.entityInteraction),

                            ResourceLocation.CODEC.optionalFieldOf("action", KeyBindManager.NONE)
                                    .forGetter(h -> h.openId)
                    ).apply(instance, UseFromBackpackHandler::new)
            );

    public static final KeybindHandlerType<UseFromBackpackHandler> TYPE =
            KeybindHandlerTypes.register(
                    new KeybindHandlerType.SimpleType<>(Miapi.id("use_from_backpack"), CODEC)
            );

    public static final ModernNetworking.ClientToServerManager<ResourceLocation> REQUEST_START_USE =
            new ModernNetworking.ClientToServerManager<>(
                    Miapi.id("backpack_start_use"),
                    ResourceLocation.CODEC,
                    UseFromBackpackServer::onStartUseRequest
            );

    public static final ModernNetworking.ClientToServerManager<Integer> REQUEST_ABORT_USE =
            new ModernNetworking.ClientToServerManager<>(
                    Miapi.id("backpack_abort_use"),
                    Codec.INT,
                    UseFromBackpackServer::onAbortUseRequest
            );

    public static final ModernNetworking.ServerToClientManager<ResourceLocation> START_USE_ACK =
            new ModernNetworking.ServerToClientManager<>(
                    Miapi.id("backpack_start_use_ack"),
                    ResourceLocation.CODEC,
                    UseFromBackpackHandler::onStartAckClientStub
            );

    public static final ModernNetworking.ServerToClientManager<Boolean> ABORT_USE_ACK =
            new ModernNetworking.ServerToClientManager<>(
                    Miapi.id("backpack_abort_use_ack"),
                    Codec.BOOL,
                    UseFromBackpackHandler::onAbortAckClientStub
            );
    public static DataComponentType<UseFromBackpackServer.UseFromBackpackData> CURRENTLY_FROM_BACKPACK_COMPONENT = DataComponentType.<UseFromBackpackServer.UseFromBackpackData>builder()
            .persistent(UseFromBackpackServer.UseFromBackpackData.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(UseFromBackpackServer.UseFromBackpackData.CODEC))
            .build();

    public final boolean itemInteraction;
    public final boolean blockInteraction;
    public final boolean entityInteraction;
    public final ResourceLocation openId;

    public UseFromBackpackHandler(
            boolean itemInteraction,
            boolean blockInteraction,
            boolean entityInteraction,
            ResourceLocation openAction
    ) {
        this.itemInteraction = itemInteraction;
        this.blockInteraction = blockInteraction;
        this.entityInteraction = entityInteraction;
        this.openId = openAction;
    }

    @Override
    public KeybindHandlerType<?> type() {
        return TYPE;
    }

    @Override
    public void onPress(net.minecraft.client.Minecraft mc, net.minecraft.client.player.LocalPlayer player, MiapiBinding binding) {
        UseFromBackpackClient.onPress(this, mc, player);
    }

    @Override
    public void whileHeld(net.minecraft.client.Minecraft mc, net.minecraft.client.player.LocalPlayer player, MiapiBinding binding) {
        UseFromBackpackClient.onHeld(this, mc, player);
    }

    @Override
    public void onRelease(net.minecraft.client.Minecraft mc, net.minecraft.client.player.LocalPlayer player, MiapiBinding binding) {
        UseFromBackpackClient.onRelease(this, mc, player);
    }

    public static void onStartAckClientStub(ResourceLocation id, Player player, net.minecraft.core.RegistryAccess access) {
        UseFromBackpackClient.handleStartAck(id);
    }

    public static void onAbortAckClientStub(boolean data, Player player, net.minecraft.core.RegistryAccess access) {
        UseFromBackpackClient.handleAbortAck(data);
    }
}