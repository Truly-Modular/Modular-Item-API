package smartin.miapi.fabric;


import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.client.atlas.ArmorModelManager;
import smartin.miapi.item.modular.ModularItem;

public class ModularArmorRenderer implements ArmorRenderer {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        if (stack.getItem() instanceof ModularItem) {
            ArmorModelManager.renderArmorPiece(
                    matrices,
                    vertexConsumers,
                    light,
                    slot,
                    stack,
                    entity,
                    contextModel,
                    contextModel,
                    null);
        }
    }
}
