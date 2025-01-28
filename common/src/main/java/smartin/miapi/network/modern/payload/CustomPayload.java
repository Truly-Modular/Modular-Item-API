package smartin.miapi.network.modern.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record CustomPayload(String id, UUID serverPlayer, byte[] data) {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static CustomPayload decode(FriendlyByteBuf friendlyByteBuf) {
        String id = friendlyByteBuf.readUtf();
        UUID uuid = friendlyByteBuf.readUUID();
        byte[] rawData = friendlyByteBuf.readByteArray();
        return new CustomPayload(id, uuid, rawData);
    }

    public void encode(FriendlyByteBuf data) {
        data.writeUtf(id());
        data.writeUUID(serverPlayer() != null ? serverPlayer() : noPlayerUUID);
        data.writeByteArray(data());
    }

    public ResourceLocation parseId() {
        return ResourceLocation.parse(id);
    }
}
