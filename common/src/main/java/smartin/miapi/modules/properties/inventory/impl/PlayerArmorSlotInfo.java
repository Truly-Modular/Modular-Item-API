package smartin.miapi.modules.properties.inventory.impl;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.inventory.SlotInfo;

public class PlayerArmorSlotInfo implements SlotInfo {
    private final EquipmentSlot slot;
    private final ResourceLocation id;
    private final Color color;
    private final Component name;

    public PlayerArmorSlotInfo(EquipmentSlot slot, ResourceLocation id, Color color, Component name) {
        this.slot = slot;
        this.id = id;
        this.color = color;
        this.name = name;
    }

    @Override
    public ItemStack getStack(Player player) {
        return player.getItemBySlot(slot);
    }

    @Override
    public void setStack(ItemStack saved, Player player) {
        player.setItemSlot(slot, saved);
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public double priority() {
        return 6 - slot.getIndex();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void renderIcon(int x, int y, int width, int height) {
        // skipped for now
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerArmorSlotInfo armorSlotInfo) {
            return armorSlotInfo.slot.equals(this.slot) && armorSlotInfo.id.equals(this.id);
        }
        return super.equals(obj);
    }
}
