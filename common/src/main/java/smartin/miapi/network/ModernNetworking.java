package smartin.miapi.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class ModernNetworking {
    public static <T> void registerS2CReciever(ResourceLocation location, StreamCodec<FriendlyByteBuf, T> codec, Consumer<T> onRecieve) {

    }

    public static <T> void registerC2SReciever(ResourceLocation location, StreamCodec<FriendlyByteBuf, T> codec, Consumer<T> onRecieve) {

    }

    public static <T> void sendToServer(ResourceLocation location, StreamCodec<FriendlyByteBuf, T> codec, T data) {

    }

    public static <T> void registerToPlayer(ResourceLocation location, Player player, StreamCodec<FriendlyByteBuf, T> codec, T data) {

    }
}
