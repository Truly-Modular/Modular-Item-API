package smartin.miapi.client.gui.crafting.slotdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import smartin.miapi.client.gui.InteractAbleWidget;

@Environment(EnvType.CLIENT)
public class SmithDisplay extends InteractAbleWidget {
    public static final Quaternionf ARMOR_STAND_ROTATION = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float) Math.PI);
    @Nullable
    private final ArmorStand armorStand;

    public SmithDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.armorStand = new ArmorStand(Minecraft.getInstance().level, 0.0, 0.0, 0.0);
        this.armorStand.setNoBasePlate(true);
        this.armorStand.setShowArms(true);
        this.armorStand.yBodyRot = 210.0f;
        this.armorStand.setXRot(25.0f);
        this.armorStand.yHeadRot = this.armorStand.getYRot();
        this.armorStand.yHeadRotO = this.armorStand.getYRot();
        this.equipArmorStand(ItemStack.EMPTY);
    }

    public void setPreview(ItemStack itemStack) {
        equipArmorStand(itemStack);
    }

    private void equipArmorStand(ItemStack stack) {
        if (this.armorStand == null) {
            return;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            this.armorStand.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
        if (!stack.isEmpty()) {
            ItemStack itemStack = stack;
            Item item = stack.getItem();
            if (item instanceof ArmorItem armorItem) {
                this.armorStand.setItemSlot(armorItem.getEquipmentSlot(), itemStack);
            } else {
                this.armorStand.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        //context.enableScissor(getX(),getY(),getX()+getWidth(),getY()+getHeight());
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                context,
                this.getX() + getWidth() / 2 + 3,
                this.getY() + this.height - 10,
                30,
                50,
                2,
                0,
                mouseX,
                mouseY,
                this.armorStand);
        //context.disableScissor();
        super.renderWidget(context, mouseX, mouseY, delta);
    }
}
