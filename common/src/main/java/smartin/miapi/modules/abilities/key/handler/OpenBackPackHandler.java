package smartin.miapi.modules.abilities.key.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.key.MiapiBinding;
import smartin.miapi.modules.properties.inventory.screen.DefaultInventoryScreenHandler;
import smartin.miapi.network.modern.ModernNetworking;

import java.util.List;
import java.util.Optional;

public final class OpenBackPackHandler implements KeybindHandler {
    public static final ModernNetworking.ClientToServerManager<ResourceLocation> UI_OPEN =
            new ModernNetworking.ClientToServerManager<ResourceLocation>(
                    Miapi.id("ui_open"),
                    ByteBufCodecs.fromCodecWithRegistries(ResourceLocation.CODEC),
                    (packet, player, access) -> {
                        openBackPackUI(packet, player);
                    }
            );

    private static void openBackPackUI(ResourceLocation packet, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("UI: " + packet);
            }

            @Override
            public AbstractContainerMenu createMenu(
                    int id,
                    Inventory inv,
                    Player player
            ) {
                return new DefaultInventoryScreenHandler(id, inv);
            }
        });
    }

    public static final KeybindHandlerType<OpenBackPackHandler> TYPE =
            KeybindHandlerTypes.register(new KeybindHandlerType<>() {

                @Override
                public ResourceLocation id() {
                    return Miapi.id("open_ui");
                }

                @Override
                public MapCodec<OpenBackPackHandler> codec() {
                    return CODEC;
                }
            });

    public enum Mode {
        WHITELIST,
        BLACKLIST
    }

    public static final Codec<Mode> MODE_CODEC =
            Codec.STRING.xmap(
                    s -> Mode.valueOf(s.toUpperCase()),
                    Mode::name
            );

    public static final MapCodec<OpenBackPackHandler> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .optionalFieldOf("ui_id")
                                    .forGetter(h -> Optional.ofNullable(h.uiId)),
                            MODE_CODEC
                                    .optionalFieldOf("mode", Mode.WHITELIST)
                                    .forGetter(h -> h.mode),
                            ResourceLocation.CODEC.listOf()
                                    .optionalFieldOf("ids")
                                    .forGetter(h -> Optional.ofNullable(h.ids))
                    ).apply(instance, OpenBackPackHandler::new)
            );

    public final ResourceLocation uiId;
    public final Mode mode;
    public final List<ResourceLocation> ids;

    public OpenBackPackHandler(
            Optional<ResourceLocation> uiId,
            Mode mode,
            Optional<List<ResourceLocation>> ids
    ) {
        this.uiId = uiId.orElse(null);
        this.mode = mode;
        this.ids = ids.orElse(null);
    }

    @Override
    public KeybindHandlerType<?> type() {
        return TYPE;
    }

    @Override
    public void onPress(Minecraft mc, LocalPlayer player, MiapiBinding binding) {
        if (!allowed(binding.id)) return;

        if (mc.level == null) return;

        UI_OPEN.sendServer(
                binding.id,
                player.registryAccess()
        );
    }

    private boolean allowed(ResourceLocation id) {
        if (ids == null) return true;

        boolean contains = ids.contains(id);

        return mode == Mode.WHITELIST ? contains : !contains;
    }
}