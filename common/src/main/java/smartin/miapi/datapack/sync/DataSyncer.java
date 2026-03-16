package smartin.miapi.datapack.sync;

import net.minecraft.network.FriendlyByteBuf;
import smartin.miapi.datapack.ReloadEvents;

/**
 * This interface can be used to sync custom data from server to client within Truly Modular reload logic to ensure the sync happens at a predictable time
 */
public interface DataSyncer<T> {
    /**
     * This will be called when truly modular syncs its data to the client
     *
     * @return the PacketBuffer to be synced
     */
    FriendlyByteBuf createDataServer();

    /**
     * Be aware that this will trigger between the
     * {@link ReloadEvents#START} and {@link ReloadEvents#MAIN}
     * This should be used to set up data and not process the data.
     * For processing the data {@link ReloadEvents#MAIN} should be used
     * <p>
     * !Be aware this is executed on the Networking thread!
     *
     * @param buf the buffer received from the server
     */
    void interpretDataClient(FriendlyByteBuf buf);
}
