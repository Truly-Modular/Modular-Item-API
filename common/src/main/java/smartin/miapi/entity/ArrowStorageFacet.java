package smartin.miapi.entity;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrowStorageFacet implements EntityFacet<CompoundTag> {

    private final LivingEntity entity;

    public static final ResourceLocation facetIdentifier =
            Miapi.id("arrow_storage");

    public static FacetKey<ArrowStorageFacet> KEY =
            FacetRegistry.register(facetIdentifier, ArrowStorageFacet.class);

    /**
     * Expiration time (ticks). Currently 30 minutes
     */
    private static final int MAX_AGE_TICKS = 20 * 60 * 30;

    private final List<StoredArrow> storedArrows = new ArrayList<>();

    public ArrowStorageFacet(LivingEntity entity) {
        this.entity = entity;
    }

    public void addArrow(ItemStack stack) {
        if (stack.isEmpty()) return;

        int currentTick = entity.tickCount;

        // Try to merge with existing
        for (StoredArrow stored : storedArrows) {
            if (ItemStack.isSameItemSameComponents(stored.stack, stack)) {
                stored.stack.grow(stack.getCount());
                return;
            }
        }

        // Otherwise add new entry
        storedArrows.add(new StoredArrow(stack.copy(), currentTick));
    }

    public List<ItemStack> getStoredArrows() {
        List<ItemStack> result = new ArrayList<>();
        for (StoredArrow entry : storedArrows) {
            result.add(entry.stack.copy());
        }
        return result;
    }

    private void removeExpired() {
        int currentTick = entity.tickCount;

        Iterator<StoredArrow> iterator = storedArrows.iterator();
        while (iterator.hasNext()) {
            StoredArrow entry = iterator.next();
            if (currentTick - entry.addedTick > MAX_AGE_TICKS) {
                iterator.remove();
            }
        }
    }

    @Override
    public CompoundTag toNbt() {
        removeExpired(); // prune before saving

        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();

        for (StoredArrow entry : storedArrows) {
            CompoundTag tag = new CompoundTag();
            tag.put("stack", entry.stack.save(entity.registryAccess()));
            tag.putInt("addedTick", entry.addedTick);
            list.add(tag);
        }

        root.put("arrows", list);
        return root;
    }

    @Override
    public void loadNbt(CompoundTag nbt) {
        storedArrows.clear();

        if (!nbt.contains("arrows", Tag.TAG_LIST)) return;

        ListTag list = nbt.getList("arrows", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            var dataResult = ItemStack.CODEC.decode(
                    RegistryOps.create(NbtOps.INSTANCE, entity.registryAccess()),
                    tag.get("stack"));
            if (dataResult.isSuccess()) {
                ItemStack stack = dataResult.getOrThrow().getFirst();
                int addedTick = tag.getInt("addedTick");
                storedArrows.add(new StoredArrow(stack, addedTick));
            } else {
                Miapi.LOGGER.error(dataResult.error().get().message());
            }
        }
    }

    @Override
    public void sendToClient(Entity capHolder, ServerPlayer player) {
        if (player != null && player.connection != null && player.level() != null) {
            try {
                createPacket(capHolder).send(player);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("ArrowStorageFacet sync issue", e);
            }
        }
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        return new FacetSyncPacket<>(target, KEY, this);
    }

    private static class StoredArrow {
        ItemStack stack;
        int addedTick;

        StoredArrow(ItemStack stack, int addedTick) {
            this.stack = stack;
            this.addedTick = addedTick;
        }
    }
}