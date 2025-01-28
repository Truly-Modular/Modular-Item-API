package smartin.miapi.key;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import smartin.miapi.Miapi;

public class KeyBindFacet implements EntityFacet<CompoundTag> {
    public static ResourceLocation ID = Miapi.id("key_bind");
    public static FacetKey<KeyBindFacet> KEY = FacetRegistry.register(ID, KeyBindFacet.class);

    public static KeyBindFacet get(ServerPlayer entity) {
        return KEY.get(entity);
    }

    public ResourceLocation bindingID = null;

    public KeyBindFacet(Entity entity) {
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", bindingID == null ? "none" : bindingID.toString());
        return tag;
    }

    @Override
    public void loadNbt(CompoundTag tag) {
        String idString = tag.getString("key");
        if (idString.equals("none")) {
            bindingID = null;
        } else {
            bindingID = Miapi.id(idString);
        }
    }

    public void set(ResourceLocation key, ServerPlayer facetHolder) {
        this.bindingID = key;
        sendToTrackers(facetHolder);
        sendToClient(facetHolder);
    }

    public void reset(ServerPlayer facetHolder) {
        this.bindingID = null;
        sendToTrackers(facetHolder);
        sendToClient(facetHolder);
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        // The FacetSyncPacket requires 3 things: the entity holding the facet, the facet key, and the facet instance.
        return new FacetSyncPacket<>(target, KEY, this);
    }
}