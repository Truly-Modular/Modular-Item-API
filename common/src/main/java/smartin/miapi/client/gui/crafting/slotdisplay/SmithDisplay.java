package smartin.miapi.client.gui.crafting.slotdisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.ModuleModel;
import smartin.miapi.client.model.collision.Ray;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SmithDisplay extends InteractAbleWidget {
    public static final Quaternionf ARMOR_STAND_ROTATION = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float) Math.PI);
    @Nullable
    private final ArmorStand armorStand;
    public SlotProperty.ModuleSlot baseSlot;
    Consumer<SlotProperty.ModuleSlot> selected;
    private ModuleInstance currentHover = null;

    public SmithDisplay(int x, int y, int width, int height, Consumer<SlotProperty.ModuleSlot> selected) {
        super(x, y, width, height, Component.empty());
        this.armorStand = new ArmorStand(Minecraft.getInstance().level, 0.0, 0.0, 0.0);
        this.armorStand.setNoBasePlate(true);
        this.armorStand.setShowArms(true);
        this.armorStand.yBodyRot = 210.0f;
        this.armorStand.setXRot(25.0f);
        this.armorStand.yHeadRot = this.armorStand.getYRot();
        this.armorStand.yHeadRotO = this.armorStand.getYRot();
        this.equipArmorStand(ItemStack.EMPTY);
        this.selected = selected;
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
            if (item instanceof Equipable armorItem && armorItem.getEquipmentSlot() != EquipmentSlot.MAINHAND) {
                this.armorStand.setItemSlot(armorItem.getEquipmentSlot(), itemStack);
            } else {
                this.armorStand.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            if (currentHover != null) {
                if (currentHover.getParent() == null) {
                    selected.accept(baseSlot);
                } else {
                    selected.accept(SlotProperty.getSlotIn(currentHover));
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.pose().pushPose();
        Ray ray = createMouseRay(mouseX, mouseY);
        MiapiItemModel.startRayCastMode(ray, new PoseStack(), Screen.hasAltDown());
        context.pose().pushPose();
        InventoryScreen.renderEntityInInventory(
                context,
                this.getX() + getWidth() / 2 + 3,
                this.getY() + this.height - 10,
                30,
                new Vector3f(),
                ARMOR_STAND_ROTATION,
                null,
                this.armorStand);
        context.pose().popPose();
        var result = MiapiItemModel.collectAndEndRayCastMode();
        context.pose().popPose();
        currentHover = result.map(ModuleModel.ModuleRayHit::module).orElse(null);
        if (Screen.hasAltDown()) {
            if (result.isPresent()) {
                Miapi.LOGGER.info(result.get().module().cache().getModuleName().getString());
            } else {
                Miapi.LOGGER.info("nothing");
            }
        }
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    private Ray createMouseRay(double mouseX, double mouseY) {
        Vector3f origin = new Vector3f((float) mouseX, (float) mouseY, 500.0F);
        Vector3f direction = new Vector3f(0.0F, 0.0F, -1.0F);
        return new Ray(origin, direction);
    }

    public int getSize() {
        int size = Math.min(width, height);
        size = Math.max(5, size - 10);
        return size;
    }

    private Vector3f position() {
        return new Vector3f(getX() + (float) (width - 16) / 2, getY() + (float) (height - 16) / 2, (100.0F + 50));
    }
}
