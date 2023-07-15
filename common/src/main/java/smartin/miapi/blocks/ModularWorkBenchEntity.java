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
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.registries.RegistryInventory;

public class ModularWorkBenchEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private ItemStack stack;

    public ModularWorkBenchEntity(BlockPos pos, BlockState state) {
        super(RegistryInventory.modularWorkBenchEntityType, pos, state);
        this.stack = ItemStack.EMPTY;
        this.markDirty();
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("Item", stack.writeNbt(new NbtCompound()));
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound compound = new NbtCompound();
        writeNbt(compound);
        return compound;
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

    public void setItem(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public ItemStack getItem() {
        return stack;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("test");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CraftingScreenHandler(syncId, playerInventory, this);
    }
}