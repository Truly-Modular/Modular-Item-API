package smartin.miapi.datapack.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import smartin.miapi.network.Networking;

/**
 * A simple codec based Implementation of {@link DataSyncer}
 * @param <T>
 */
public abstract class StreamCodecSyncer<T> implements DataSyncer<T> {
    public StreamCodec<ByteBuf, T> streamCodec;

    public StreamCodecSyncer(StreamCodec<ByteBuf, T> streamCodec) {
        this.streamCodec = streamCodec;
    }

    public abstract T getDataServer();

    public abstract void interpretData(T data);

    public FriendlyByteBuf createDataServer() {
        FriendlyByteBuf buf = Networking.createBuffer();
        streamCodec.encode(buf, getDataServer());
        return buf;
    }

    public void interpretDataClient(FriendlyByteBuf buf) {
        interpretData(streamCodec.decode(buf));
    }
}
