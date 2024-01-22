package smartin.miapi.client.gui.crafting.slotdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import smartin.miapi.client.gui.InteractAbleWidget;

@Environment(EnvType.CLIENT)
public class SmithDisplay extends InteractAbleWidget {
    public static final Quaternionf ARMOR_STAND_ROTATION = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float) Math.PI);
    @Nullable
    private ArmorStandEntity armorStand;

    public SmithDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.armorStand = new ArmorStandEntity(MinecraftClient.getInstance().world, 0.0, 0.0, 0.0);
        this.armorStand.setHideBasePlate(true);
        this.armorStand.setShowArms(true);
        this.armorStand.bodyYaw = 210.0f;
        this.armorStand.setPitch(25.0f);
        this.armorStand.headYaw = this.armorStand.getYaw();
        this.armorStand.prevHeadYaw = this.armorStand.getYaw();
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
            this.armorStand.equipStack(equipmentSlot, ItemStack.EMPTY);
        }
        if (!stack.isEmpty()) {
            ItemStack itemStack = stack;
            Item item = stack.getItem();
            if (item instanceof ArmorItem) {
                ArmorItem armorItem = (ArmorItem) item;
                this.armorStand.equipStack(armorItem.getSlotType(), itemStack);
            } else {
                this.armorStand.equipStack(EquipmentSlot.OFFHAND, itemStack);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //context.enableScissor(getX(),getY(),getX()+getWidth(),getY()+getHeight());
        InventoryScreen.drawEntity(context, this.getX() + getWidth() / 2 + 3, this.getY() + this.height - 10, 30, ARMOR_STAND_ROTATION, null, (LivingEntity) this.armorStand);
        //context.disableScissor();
        super.render(context, mouseX, mouseY, delta);
    }
}
