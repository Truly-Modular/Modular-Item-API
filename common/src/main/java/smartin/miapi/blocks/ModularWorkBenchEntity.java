package smartin.miapi.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import net.minecraft.item.ItemStack;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

public class ModularWorkBenchEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private ItemStack stack;
    public final CraftingStat.Map<?> blockStats = new CraftingStat.Map<>();
    public final CraftingStat.Map<?> itemStats = new CraftingStat.Map<>();

    public ModularWorkBenchEntity(BlockPos pos, BlockState state) {
        super(RegistryInventory.modularWorkBenchEntityType, pos, state);
        this.stack = ItemStack.EMPTY;
    }

    public void setItem(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }
    public ItemStack getItem() {
        return stack;
    }

    public <T> T getBlockStat(CraftingStat<T> stat) {
        return blockStats.getOrDefault(stat);
    }
    public <T> T getItemStat(CraftingStat<T> stat) {
        return itemStats.getOrDefault(stat);
    }
    public <T> T getStat(CraftingStat<T> stat) {
        return stat.merge(this, getItemStat(stat), getBlockStat(stat));
    }
    public <T> void setBlockStat(CraftingStat<T> stat, T val) {
        T old = getBlockStat(stat);
        blockStats.set(stat, stat.merge(this, old, val));
    }
    public <T> void setItemStat(CraftingStat<T> stat, T val) {
        T old = getItemStat(stat);
        itemStats.set(stat, stat.merge(this, old, val));
    }
    public <T> void overrideBlockStat(CraftingStat<T> stat, T val) {
        blockStats.set(stat, val);
    }
    public <T> void overrideItemStat(CraftingStat<T> stat, T val) {
        itemStats.set(stat, val);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("Item", stack.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (!tag.contains("Item", 10)) this.stack = ItemStack.EMPTY;
        else this.stack = ItemStack.fromNbt(tag.getCompound("Item"));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this, be -> be.createNbt());
    }

    public void saveAndSync() {
        this.markDirty();
        if (this.hasWorld())
            this.world.updateListeners(pos, world.getBlockState(pos), this.getCachedState(), 3);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("test");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (hasWorld() && !world.isClient) world.emitGameEvent(RegistryInventory.statUpdateEvent, pos, new GameEvent.Emitter(player, getCachedState()));
        return new CraftingScreenHandler(syncId, playerInventory, this);
    }
}